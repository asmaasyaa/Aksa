package com.aksa.app.domain.model

/**
 * Metric tracking a child's performance on a specific learning target (e.g. letter, syllable, or word).
 */
data class TargetErrorMetric(
    val target: String,
    val totalAttempts: Int,
    val incorrectAttempts: Int,
    val errorRate: Float,
    val isWeakTarget: Boolean
) {
    val correctAttempts: Int get() = (totalAttempts - incorrectAttempts).coerceAtLeast(0)
    val accuracy: Float get() = if (totalAttempts > 0) correctAttempts.toFloat() / totalAttempts.toFloat() else 0f
}

/**
 * Aggregated error analytics report for a child profile within a learning module.
 */
data class ErrorAnalyticsReport(
    val childId: String,
    val moduleId: String,
    val targetMetrics: Map<String, TargetErrorMetric>,
    val weakTargets: List<String>,
    val totalSessionsAnalyzed: Int,
    val totalQuestionsAnalyzed: Int
)

