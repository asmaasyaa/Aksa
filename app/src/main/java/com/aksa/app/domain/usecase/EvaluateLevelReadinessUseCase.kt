package com.aksa.app.domain.usecase

import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.LevelReadinessResult
import com.aksa.app.domain.model.LevelReadinessStatus
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.model.TargetPerformance
import com.aksa.app.domain.repository.SessionRepository

/**
 * Evaluates whether a child has achieved sufficient learning mastery to unlock the next level.
 *
 * UNLOCK RULES:
 * 1. At least 3 valid completed sessions for the evaluated module.
 * 2. Average accuracy across the latest 3 valid sessions >= 80% (0.80f).
 * 3. Relevant learning targets have sufficient evidence (>= 3 recorded attempts per target).
 * 4. Error rate of every evaluated target must not exceed 40% (0.40f).
 *
 * Note: Distinct learning day counts, streaks, and calendar requirements are deliberately excluded.
 */
class EvaluateLevelReadinessUseCase(
    private val sessionRepository: SessionRepository = AksaRepositoryProvider.sessionRepository
) {
    suspend operator fun invoke(
        childId: String,
        moduleId: String
    ): LevelReadinessResult {
        // 1. Fetch completed sessions for the target child
        val completedSessions = sessionRepository.getCompletedSessions(childId)

        // 2. Filter for strictly valid sessions for this module
        val validSessions = completedSessions
            .filter { isValidSession(it, childId, moduleId) }
            .distinctBy { it.sessionId } // Prevent duplicate session records from skewing count
            .sortedByDescending { it.completedAt }

        val sessionCount = validSessions.size

        // CONDITION 1: At least three valid completed sessions
        if (sessionCount < 3) {
            val remaining = 3 - sessionCount
            val explanation = if (sessionCount == 0) {
                "Selesaikan 3 sesi latihan lagi, yuk!"
            } else {
                "Selesaikan $remaining sesi latihan lagi, yuk!"
            }
            return LevelReadinessResult(
                childId = childId,
                moduleId = moduleId,
                status = LevelReadinessStatus.INSUFFICIENT_SESSIONS,
                sessionCount = sessionCount,
                validSessions = validSessions,
                averageAccuracy = 0f,
                targetPerformances = emptyMap(),
                explanation = explanation
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
                explanation = "Ayo latihan lagi supaya hasil akurasimu mencapai 80% atau lebih!"
            )
        }

        // Retrieve AnswerLogs for these latest three sessions
        val sessionIds = latestThree.map { it.sessionId }.toSet()
        val allAnswers = sessionIds.flatMap { sid ->
            sessionRepository.getAnswersForSession(sid)
        }
            .filter { it.childId == childId && it.moduleId == moduleId }
            .distinctBy { it.answerId }

        // Missing AnswerLog MUST NOT be interpreted as zero errors
        if (allAnswers.isEmpty()) {
            return LevelReadinessResult(
                childId = childId,
                moduleId = moduleId,
                status = LevelReadinessStatus.INSUFFICIENT_EVIDENCE,
                sessionCount = sessionCount,
                validSessions = latestThree,
                averageAccuracy = averageAccuracy,
                targetPerformances = emptyMap(),
                explanation = "Kita perlu beberapa jawaban lagi untuk memastikan kamu siap."
            )
        }

        // Group answer evidence by target character / syllable / word
        val targetGroups = allAnswers.groupBy { it.targetChar }
        if (targetGroups.isEmpty()) {
            return LevelReadinessResult(
                childId = childId,
                moduleId = moduleId,
                status = LevelReadinessStatus.INSUFFICIENT_EVIDENCE,
                sessionCount = sessionCount,
                validSessions = latestThree,
                averageAccuracy = averageAccuracy,
                targetPerformances = emptyMap(),
                explanation = "Kita perlu beberapa jawaban lagi untuk memastikan kamu siap."
            )
        }

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

            // Require at least 3 recorded attempts per evaluated target
            if (totalAttempts < 3) {
                hasInsufficientEvidenceTarget = true
            }
        }

        // CONDITION 3: Sufficient answer evidence per evaluated target
        if (hasInsufficientEvidenceTarget) {
            return LevelReadinessResult(
                childId = childId,
                moduleId = moduleId,
                status = LevelReadinessStatus.INSUFFICIENT_EVIDENCE,
                sessionCount = sessionCount,
                validSessions = latestThree,
                averageAccuracy = averageAccuracy,
                targetPerformances = targetPerformances,
                explanation = "Kita perlu beberapa jawaban lagi untuk memastikan kamu siap."
            )
        }

        // CONDITION 4: Error rate of every sufficiently evaluated target <= 40% (0.40f)
        val failingTargets = targetPerformances.values.filter { it.errorRate > 0.40f + 1e-6f }
        if (failingTargets.isNotEmpty()) {
            val targetsString = failingTargets.joinToString(", ") { it.target }
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
                explanation = explanation
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
            explanation = "Hebat! Petualangan berikutnya sudah terbuka!"
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

