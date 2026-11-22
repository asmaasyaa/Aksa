package com.aksa.app.domain.usecase

import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.ErrorAnalyticsReport
import com.aksa.app.domain.model.TargetErrorMetric
import com.aksa.app.domain.repository.SessionRepository

/**
 * Analyzes historical answer logs for a specific child profile to identify weak learning targets.
 *
 * Weak Target Criteria:
 * - Error rate >= 30% (0.30f) with at least 1 incorrect attempt, OR
 * - At least 2 incorrect attempts recorded for that target.
 *
 * Weak targets are ranked by highest error rate first, then highest incorrect count, then alphabetical.
 */
class AnalyzeTargetErrorsUseCase(
    private val sessionRepository: SessionRepository = AksaRepositoryProvider.sessionRepository
) {
    suspend operator fun invoke(
        childId: String,
        moduleId: String = "pulau_alfabet",
        activityId: String? = null
    ): ErrorAnalyticsReport {
        if (childId.isBlank()) {
            return ErrorAnalyticsReport(
                childId = childId,
                moduleId = moduleId,
                targetMetrics = emptyMap(),
                weakTargets = emptyList(),
                totalSessionsAnalyzed = 0,
                totalQuestionsAnalyzed = 0
            )
        }

        // 1. Fetch completed sessions for the child in this module
        val completedSessions = sessionRepository.getCompletedSessions(childId)
            .filter { it.moduleId == moduleId && it.isCompleted }
            .let { sessions ->
                if (activityId != null) sessions.filter { it.activityId == activityId } else sessions
            }

        if (completedSessions.isEmpty()) {
            return ErrorAnalyticsReport(
                childId = childId,
                moduleId = moduleId,
                targetMetrics = emptyMap(),
                weakTargets = emptyList(),
                totalSessionsAnalyzed = 0,
                totalQuestionsAnalyzed = 0
            )
        }

        val completedSessionIds = completedSessions.map { it.sessionId }.toSet()

        // 2. Fetch answer logs belonging to completed sessions for this child
        val childAnswers = sessionRepository.getAnswersForChild(childId)
            .filter { log ->
                log.moduleId == moduleId &&
                (activityId == null || log.activityId == activityId) &&
                completedSessionIds.contains(log.sessionId)
            }

        // Fallback/supplement with session-specific answers if needed
        val allAnswers = if (childAnswers.isEmpty()) {
            completedSessionIds.flatMap { sid ->
                sessionRepository.getAnswersForSession(sid)
            }.filter { it.childId == childId && it.moduleId == moduleId }
        } else {
            childAnswers
        }.distinctBy { it.answerId }

        if (allAnswers.isEmpty()) {
            return ErrorAnalyticsReport(
                childId = childId,
                moduleId = moduleId,
                targetMetrics = emptyMap(),
                weakTargets = emptyList(),
                totalSessionsAnalyzed = completedSessions.size,
                totalQuestionsAnalyzed = 0
            )
        }

        // 3. Group by target character
        val targetGroups = allAnswers.groupBy { it.targetChar }
        val targetMetrics = mutableMapOf<String, TargetErrorMetric>()

        for ((target, answers) in targetGroups) {
            val total = answers.size
            val incorrect = answers.count { !it.isCorrect }
            val errorRate = if (total > 0) incorrect.toFloat() / total.toFloat() else 0f
            val isWeak = (errorRate >= 0.30f && incorrect > 0) || (incorrect >= 2)

            targetMetrics[target] = TargetErrorMetric(
                target = target,
                totalAttempts = total,
                incorrectAttempts = incorrect,
                errorRate = errorRate,
                isWeakTarget = isWeak
            )
        }

        // 4. Rank weak targets
        val weakTargets = targetMetrics.values
            .filter { it.isWeakTarget }
            .sortedWith(
                compareByDescending<TargetErrorMetric> { it.errorRate }
                    .thenByDescending { it.incorrectAttempts }
                    .thenBy { it.target }
            )
            .map { it.target }

        return ErrorAnalyticsReport(
            childId = childId,
            moduleId = moduleId,
            targetMetrics = targetMetrics,
            weakTargets = weakTargets,
            totalSessionsAnalyzed = completedSessions.size,
            totalQuestionsAnalyzed = allAnswers.size
        )
    }
}

