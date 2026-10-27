package com.aksa.app.domain.model

enum class SessionStatus {
    IN_PROGRESS,
    COMPLETED,
    ABANDONED
}

/**
 * Summary result of an educational activity session.
 * COMPLETED = the child finished the exercise.
 * Mastered = separate evaluation of learning performance.
 */
data class SessionResult(
    val sessionId: String,
    val childId: String,
    val moduleId: String,
    val activityId: String,
    val startedAt: Long,
    val completedAt: Long? = null,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val accuracy: Float,
    val status: SessionStatus
) {
    val isCompleted: Boolean get() = status == SessionStatus.COMPLETED
}
