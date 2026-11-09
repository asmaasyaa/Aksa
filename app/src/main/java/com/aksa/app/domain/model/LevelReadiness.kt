package com.aksa.app.domain.model

/**
 * Status of educational readiness evaluation to unlock the next learning level.
 */
enum class LevelReadinessStatus {
    INSUFFICIENT_SESSIONS,
    INSUFFICIENT_EVIDENCE,
    LOW_ACCURACY,
    HIGH_ERROR_RATE,
    READY_TO_UNLOCK
}

/**
 * Performance metrics for a single evaluated target (e.g., a specific letter, syllable, or word).
 */
data class TargetPerformance(
    val target: String,
    val totalAttempts: Int,
    val incorrectAttempts: Int,
    val errorRate: Float
) {
    val isSufficientEvidence: Boolean get() = totalAttempts >= 3
    val isErrorRateAcceptable: Boolean get() = errorRate <= 0.40f + 1e-6f
}

/**
 * Detailed evaluation outcome returned by EvaluateLevelReadinessUseCase.
 */
data class LevelReadinessResult(
    val childId: String,
    val moduleId: String,
    val status: LevelReadinessStatus,
    val sessionCount: Int,
    val validSessions: List<SessionResult>,
    val averageAccuracy: Float, // Range 0.0f .. 1.0f
    val targetPerformances: Map<String, TargetPerformance>,
    val explanation: String,
    val isReadyToUnlock: Boolean = (status == LevelReadinessStatus.READY_TO_UNLOCK)
)

