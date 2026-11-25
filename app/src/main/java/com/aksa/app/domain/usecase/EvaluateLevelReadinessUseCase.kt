package com.aksa.app.domain.usecase

import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.LevelReadinessResult
import com.aksa.app.domain.model.LevelReadinessStatus
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.model.TargetPerformance
import com.aksa.app.domain.repository.SessionRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Evaluates whether a child has achieved sufficient learning mastery to unlock the next level.
 *
 * UNLOCK RULES:
 * 1. At least 3 valid completed sessions for the evaluated module.
 * 2. Average accuracy across the latest 3 valid sessions >= 80% (0.80f).
 * 3. Relevant learning targets have sufficient evidence (>= 3 recorded attempts per target).
 * 4. Error rate of every evaluated target must not exceed 40% (0.40f).
 *
 * Note: Distinct practice days / weekly consistency belong to F4 and never gate level unlock.
 */
class EvaluateLevelReadinessUseCase(
    private val sessionRepository: SessionRepository = AksaRepositoryProvider.sessionRepository,
    private val clock: Clock = Clock.system(ZoneId.of("Asia/Jakarta")),
    private val zoneId: ZoneId = ZoneId.of("Asia/Jakarta")
) {
    suspend operator fun invoke(
        childId: String,
        moduleId: String
    ): LevelReadinessResult {
        require(childId.isNotBlank()) { "childId must not be blank" }
        require(moduleId.isNotBlank()) { "moduleId must not be blank" }

        // 1. Fetch completed sessions strictly (propagate Firestore read failures)
        val completedSessions = sessionRepository.getCompletedSessionsResult(childId).getOrThrow()

        // 2. Filter for strictly valid sessions for this module
        val validSessions = completedSessions
            .filter { isValidSession(it, childId, moduleId) }
            .distinctBy { it.sessionId }
            .sortedByDescending { it.completedAt }

        val sessionCount = validSessions.size

        // Informational practice days count only (not an unlock condition)
        val today = LocalDate.now(clock.withZone(zoneId))
        val sevenDaysCutoff = today.minusDays(6)
        val distinctDaysCount = validSessions.mapNotNull { session ->
            val completedAt = session.completedAt ?: return@mapNotNull null
            val sessionDate = Instant.ofEpochMilli(completedAt)
                .atZone(zoneId)
                .toLocalDate()
            if (!sessionDate.isBefore(sevenDaysCutoff) && !sessionDate.isAfter(today)) {
                sessionDate
            } else {
                null
            }
        }.toSet().size

        // CONDITION 1: At least three valid completed sessions
        if (sessionCount < 3) {
            val remaining = 3 - sessionCount
            return LevelReadinessResult(
                childId = childId,
                moduleId = moduleId,
                status = LevelReadinessStatus.INSUFFICIENT_SESSIONS,
                sessionCount = sessionCount,
                validSessions = validSessions,
                averageAccuracy = 0f,
                targetPerformances = emptyMap(),
                explanation = "Selesaikan $remaining sesi latihan lagi, yuk!",
                distinctDaysInLast7Days = distinctDaysCount
            )
        }

        // Take latest three valid sessions
        val latestThree = validSessions.take(3)

        // CONDITION 2: Average accuracy of latest three sessions >= 80% (0.80f)
        val averageAccuracy = (latestThree[0].accuracy + latestThree[1].accuracy + latestThree[2].accuracy) / 3f

        if (averageAccuracy < 0.80f - 1e-6f) {
            return LevelReadinessResult(
                childId = childId,
                moduleId = moduleId,
                status = LevelReadinessStatus.LOW_ACCURACY,
                sessionCount = sessionCount,
                validSessions = latestThree,
                averageAccuracy = averageAccuracy,
                targetPerformances = emptyMap(),
                explanation = "Ayo latihan lagi agar kamu semakin siap melanjutkan petualangan!",
                distinctDaysInLast7Days = distinctDaysCount
            )
        }

        // Retrieve AnswerLogs from direct child subcollection (avoids collectionGroup index fragility
        // and propagates Firestore read errors rather than returning a false INSUFFICIENT_EVIDENCE).
        val sessionIds = latestThree.map { it.sessionId }.toSet()
        val allChildAnswers = sessionRepository.getAnswersForChildResult(childId).getOrThrow()
        val allAnswers = allChildAnswers
            .filter {
                it.sessionId in sessionIds &&
                    it.childId == childId &&
                    it.moduleId == moduleId &&
                    it.targetChar.isNotBlank()
            }
            .distinctBy { it.answerId }

        val sessionsWithAnswers = allAnswers.map { it.sessionId }.toSet()

        // Missing AnswerLog MUST NOT be interpreted as zero errors
        if (allAnswers.isEmpty() || sessionsWithAnswers.size < latestThree.size) {
            return LevelReadinessResult(
                childId = childId,
                moduleId = moduleId,
                status = LevelReadinessStatus.INSUFFICIENT_EVIDENCE,
                sessionCount = sessionCount,
                validSessions = latestThree,
                averageAccuracy = averageAccuracy,
                targetPerformances = emptyMap(),
                explanation = "Yuk, coba beberapa latihan lagi supaya kemampuanmu bisa dinilai!",
                distinctDaysInLast7Days = distinctDaysCount
            )
        }

        // Group answer evidence by target character / syllable / word
        val targetGroups = allAnswers.groupBy { it.targetChar.trim() }
        val targetPerformances = mutableMapOf<String, TargetPerformance>()
        var hasInsufficientEvidenceTarget = false

        for ((target, answers) in targetGroups) {
            val totalAttempts = answers.size
            val incorrectAttempts = answers.count { !it.isCorrect }
            val errorRate = incorrectAttempts.toFloat() / totalAttempts.toFloat()

            val perf = TargetPerformance(
                target = target,
                totalAttempts = totalAttempts,
                incorrectAttempts = incorrectAttempts,
                errorRate = errorRate
            )
            targetPerformances[target] = perf

            // CONDITION 3: Require at least 3 recorded attempts per evaluated target
            if (totalAttempts < 3) {
                hasInsufficientEvidenceTarget = true
            }
        }

        if (hasInsufficientEvidenceTarget) {
            return LevelReadinessResult(
                childId = childId,
                moduleId = moduleId,
                status = LevelReadinessStatus.INSUFFICIENT_EVIDENCE,
                sessionCount = sessionCount,
                validSessions = latestThree,
                averageAccuracy = averageAccuracy,
                targetPerformances = targetPerformances,
                explanation = "Yuk, coba beberapa latihan lagi supaya kemampuanmu bisa dinilai!",
                distinctDaysInLast7Days = distinctDaysCount
            )
        }

        // CONDITION 4: Error rate of every sufficiently evaluated target <= 40% (0.40f)
        val failingTargets = targetPerformances.values.filter { it.errorRate > 0.40f + 1e-6f }
        if (failingTargets.isNotEmpty()) {
            val targetsString = if (failingTargets.size >= 2) {
                val allButLast = failingTargets.dropLast(1).joinToString(", ") { it.target }
                "$allButLast dan ${failingTargets.last().target}"
            } else {
                failingTargets.first().target
            }
            val explanation = if (moduleId == "pulau_alfabet") {
                "Yuk, latihan lagi membedakan huruf $targetsString!"
            } else {
                "Yuk, latihan lagi untuk materi $targetsString!"
            }
            return LevelReadinessResult(
                childId = childId,
                moduleId = moduleId,
                status = LevelReadinessStatus.HIGH_ERROR_RATE,
                sessionCount = sessionCount,
                validSessions = latestThree,
                averageAccuracy = averageAccuracy,
                targetPerformances = targetPerformances,
                explanation = explanation,
                distinctDaysInLast7Days = distinctDaysCount
            )
        }

        // All requirements satisfied
        return LevelReadinessResult(
            childId = childId,
            moduleId = moduleId,
            status = LevelReadinessStatus.READY_TO_UNLOCK,
            sessionCount = sessionCount,
            validSessions = latestThree,
            averageAccuracy = averageAccuracy,
            targetPerformances = targetPerformances,
            explanation = "Hebat! Petualangan berikutnya sudah terbuka!",
            distinctDaysInLast7Days = distinctDaysCount
        )
    }

    private fun isValidSession(session: SessionResult, childId: String, moduleId: String): Boolean {
        if (session.childId != childId) return false
        if (session.moduleId != moduleId) return false
        if (session.status != SessionStatus.COMPLETED) return false
        val completedAt = session.completedAt ?: return false
        if (completedAt <= 0) return false
        if (session.totalQuestions <= 0) return false
        if (session.correctAnswers < 0 || session.incorrectAnswers < 0) return false
        if (session.correctAnswers + session.incorrectAnswers != session.totalQuestions) return false
        if (session.accuracy !in 0.0f..1.0f) return false
        // Mini-games do not evaluate learning module progression
        if (session.activityId.contains("tangkap_huruf", ignoreCase = true) ||
            session.activityId.contains("minigame", ignoreCase = true) ||
            session.activityId.contains("mini_game", ignoreCase = true)) {
            return false
        }
        return true
    }
}
