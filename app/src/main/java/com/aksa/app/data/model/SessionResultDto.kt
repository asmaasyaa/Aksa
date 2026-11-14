package com.aksa.app.data.model

import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus

data class SessionResultDto(
    val sessionId: String = "",
    val childId: String = "",
    val profileId: String = childId,
    val userId: String = "",
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
        childId = if (childId.isNotBlank()) childId else profileId,
        moduleId = moduleId,
        activityId = activityId,
        startedAt = startedAt,
        completedAt = completedAt,
        totalQuestions = totalQuestions,
        correctAnswers = correctAnswers,
        incorrectAnswers = incorrectAnswers,
        accuracy = accuracy,
        status = try { SessionStatus.valueOf(status) } catch (e: Exception) { SessionStatus.IN_PROGRESS },
        userId = userId
    )

    companion object {
        fun fromDomain(domain: SessionResult, parentUid: String = domain.userId) = SessionResultDto(
            sessionId = domain.sessionId,
            childId = domain.childId,
            profileId = domain.profileId,
            userId = if (parentUid.isNotBlank()) parentUid else domain.userId,
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
