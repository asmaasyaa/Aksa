package com.aksa.app.data.model

import com.aksa.app.domain.model.AnswerLog

data class AnswerLogDto(
    val answerId: String = "",
    val sessionId: String = "",
    val childId: String = "",
    val profileId: String = childId,
    val userId: String = "",
    val moduleId: String = "",
    val activityId: String = "",
    val questionId: String = "",
    val targetChar: String = "",
    val chosenChar: String = "",
    val isCorrect: Boolean = false,
    val responseTimeMs: Long = 0L,
    val timestamp: Long = 0L
) {
    fun toDomain() = AnswerLog(
        answerId = answerId,
        sessionId = sessionId,
        childId = if (childId.isNotBlank()) childId else profileId,
        moduleId = moduleId,
        activityId = activityId,
        questionId = questionId,
        targetChar = targetChar,
        chosenChar = chosenChar,
        isCorrect = isCorrect,
        responseTimeMs = responseTimeMs,
        timestamp = timestamp,
        userId = userId
    )

    companion object {
        fun fromDomain(domain: AnswerLog, parentUid: String = domain.userId) = AnswerLogDto(
            answerId = domain.answerId,
            sessionId = domain.sessionId,
            childId = domain.childId,
            profileId = domain.profileId,
            userId = if (parentUid.isNotBlank()) parentUid else domain.userId,
            moduleId = domain.moduleId,
            activityId = domain.activityId,
            questionId = domain.questionId,
            targetChar = domain.targetChar,
            chosenChar = domain.chosenChar,
            isCorrect = domain.isCorrect,
            responseTimeMs = domain.responseTimeMs,
            timestamp = domain.timestamp
        )
    }
}
