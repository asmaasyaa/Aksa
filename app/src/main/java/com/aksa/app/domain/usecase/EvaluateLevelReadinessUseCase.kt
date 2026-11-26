package com.aksa.app.domain.usecase

import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.LevelReadinessResult
import com.aksa.app.domain.model.LevelReadinessStatus
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.model.TargetPerformance
import com.aksa.app.domain.repository.SessionRepository
import java.util.Calendar
import java.util.TimeZone

/**
 * Evaluates whether a child has achieved sufficient learning mastery to unlock the next level.
 *
 * UNLOCK RULES:
 * 1. At least 3 valid completed sessions for the evaluated module.
 * 2. Average accuracy across the latest 3 valid sessions >= 80% (0.80f).
 * 3. (Pulau Alfabet only) Sufficient answer evidence (>= 9 recorded answers across the latest 3 sessions)
 *    and error rate of every repeated confusable letter (b, d, p, q, m, n, u, w with >= 2 attempts) <= 40%.
 */
class EvaluateLevelReadinessUseCase(
    private val sessionRepository: SessionRepository = AksaRepositoryProvider.sessionRepository,
    private val currentTimeMillisProvider: () -> Long = { System.currentTimeMillis() },
    private val timeZone: TimeZone = TimeZone.getTimeZone("Asia/Jakarta")
) {
    companion object {
        val CONFUSABLE_LETTERS = setOf("b", "d", "p", "q", "m", "n", "u", "w")
    }

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

        // Informational practice days count in last 7 days (API 24 compatible)
        val nowMillis = currentTimeMillisProvider()
        val endOfTodayMillis = getEndOfDayMillis(nowMillis)
        val startOf7DaysWindowMillis = getStartOfDayMillis(nowMillis, daysOffset = -6)

        val distinctDaysCount = validSessions.mapNotNull { session ->
            val completedAt = session.completedAt ?: return@mapNotNull null
            if (completedAt in startOf7DaysWindowMillis..endOfTodayMillis) {
                toDayKey(completedAt)
            } else {
                null
            }
        }.toSet().size

        // CONDITION 1: At least three valid completed sessions
        if (sessionCount < 3) {
            val remaining = 3 - sessionCount
            val currentAvg = if (validSessions.isNotEmpty()) {
                validSessions.map { it.accuracy }.average().toFloat()
            } else {
                0f
            }
            return LevelReadinessResult(
                childId = childId,
                moduleId = moduleId,
                status = LevelReadinessStatus.INSUFFICIENT_SESSIONS,
                sessionCount = sessionCount,
                validSessions = validSessions,
                averageAccuracy = currentAvg,
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
                explanation = "Rata-rata akurasi 3 sesi terakhir belum mencapai 80%. Ayo latihan lagi!",
                distinctDaysInLast7Days = distinctDaysCount
            )
        }

        // CONDITION 3 & 4: Per-letter confusable mastery check applies ONLY to Pulau Alfabet
        if (moduleId != "pulau_alfabet") {
            return LevelReadinessResult(
                childId = childId,
                moduleId = moduleId,
                status = LevelReadinessStatus.READY_TO_UNLOCK,
                sessionCount = sessionCount,
                validSessions = latestThree,
                averageAccuracy = averageAccuracy,
                targetPerformances = emptyMap(),
                explanation = "Hebat! Petualangan berikutnya sudah terbuka!",
                distinctDaysInLast7Days = distinctDaysCount
            )
        }

        // Retrieve AnswerLogs from direct child subcollection for Pulau Alfabet
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

        if (allAnswers.size < 9 || sessionsWithAnswers.size < latestThree.size) {
            return LevelReadinessResult(
                childId = childId,
                moduleId = moduleId,
                status = LevelReadinessStatus.INSUFFICIENT_EVIDENCE,
                sessionCount = sessionCount,
                validSessions = latestThree,
                averageAccuracy = averageAccuracy,
                targetPerformances = emptyMap(),
                explanation = "Yuk, selesaikan minimal 9 soal huruf agar penguasaan b/d/p/q/m/n/u/w bisa dinilai!",
                distinctDaysInLast7Days = distinctDaysCount
            )
        }

        // Group answer evidence by lowercase target character
        val targetGroups = allAnswers.groupBy { it.targetChar.trim().lowercase() }
        val targetPerformances = mutableMapOf<String, TargetPerformance>()

        for ((target, answers) in targetGroups) {
            val totalAttempts = answers.size
            val incorrectAttempts = answers.count { !it.isCorrect }
            val errorRate = incorrectAttempts.toFloat() / totalAttempts.toFloat()

            targetPerformances[target] = TargetPerformance(
                target = target,
                totalAttempts = totalAttempts,
                incorrectAttempts = incorrectAttempts,
                errorRate = errorRate
            )
        }

        // Check error rate <= 40% for repeated confusable letters (b, d, p, q, m, n, u, w)
        val failingTargets = targetPerformances.values
            .filter {
                it.target in CONFUSABLE_LETTERS &&
                        it.totalAttempts >= 2 &&
                        it.errorRate > 0.40f + 1e-6f
            }
            .sortedByDescending { it.errorRate }

        if (failingTargets.isNotEmpty()) {
            val targetsString = if (failingTargets.size >= 2) {
                val allButLast = failingTargets.dropLast(1).joinToString(", ") { "'${it.target}'" }
                "$allButLast dan '${failingTargets.last().target}'"
            } else {
                "'${failingTargets.first().target}'"
            }

            return LevelReadinessResult(
                childId = childId,
                moduleId = moduleId,
                status = LevelReadinessStatus.HIGH_ERROR_RATE,
                sessionCount = sessionCount,
                validSessions = latestThree,
                averageAccuracy = averageAccuracy,
                targetPerformances = targetPerformances,
                explanation = "Yuk, latihan lagi membedakan huruf $targetsString!",
                distinctDaysInLast7Days = distinctDaysCount
            )
        }

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

    private fun getStartOfDayMillis(baseMillis: Long, daysOffset: Int): Long {
        val cal = Calendar.getInstance(timeZone).apply {
            timeInMillis = baseMillis
            add(Calendar.DAY_OF_YEAR, daysOffset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getEndOfDayMillis(baseMillis: Long): Long {
        val cal = Calendar.getInstance(timeZone).apply {
            timeInMillis = baseMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    private fun toDayKey(millis: Long): Int {
        val cal = Calendar.getInstance(timeZone).apply {
            timeInMillis = millis
        }
        return cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
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
        if (session.activityId.contains("tangkap_huruf", ignoreCase = true) ||
            session.activityId.contains("minigame", ignoreCase = true) ||
            session.activityId.contains("mini_game", ignoreCase = true)) {
            return false
        }
        return true
    }
}