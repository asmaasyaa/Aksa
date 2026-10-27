package com.aksa.app.data.model

import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus

data class SessionResultDto(
    val sessionId: String = "",
    val childId: String = "",
    val moduleId: String = "",
    val activityId: String = "",
    val startedAt: Long = 0L,
    val completedAt: Long? = null,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val accuracy: Float = 0f,
    val status: String = SessionStatus.IN_PROGRESS.name
) {
    fun toDomain() = SessionResult(
        sessionId = sessionId,
        childId = childId,
        moduleId = moduleId,
        activityId = activityId,
        startedAt = startedAt,
        completedAt = completedAt,
        totalQuestions = totalQuestions,
        correctAnswers = correctAnswers,
        incorrectAnswers = incorrectAnswers,
        accuracy = accuracy,
        status = try { SessionStatus.valueOf(status) } catch (e: Exception) { SessionStatus.IN_PROGRESS }
    )

    companion object {
        fun fromDomain(domain: SessionResult) = SessionResultDto(
            sessionId = domain.sessionId,
            childId = domain.childId,
            moduleId = domain.moduleId,
            activityId = domain.activityId,
            startedAt = domain.startedAt,
            completedAt = domain.completedAt,
            totalQuestions = domain.totalQuestions,
            correctAnswers = domain.correctAnswers,
            incorrectAnswers = domain.incorrectAnswers,
            accuracy = domain.accuracy,
            status = domain.status.name
        )
    }
}
