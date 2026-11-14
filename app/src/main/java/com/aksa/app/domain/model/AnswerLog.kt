package com.aksa.app.domain.model

/**
 * Detailed log of a single answered question within a learning session.
 * Associated with a specific child profile (profileId / childId) and parent account (userId).
 */
data class AnswerLog(
    val answerId: String,
    val sessionId: String,
    val childId: String,
    val moduleId: String,
    val activityId: String,
    val questionId: String,
    val targetChar: String,
    val chosenChar: String,
    val isCorrect: Boolean,
    val responseTimeMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
) {
    val profileId: String get() = childId
}
