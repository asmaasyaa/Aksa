package com.aksa.app.domain.repository

import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.model.SessionResult
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun recordAnswer(answer: AnswerLog): Result<Unit>
    suspend fun getAnswersForSession(sessionId: String): List<AnswerLog>
    suspend fun saveSessionResult(result: SessionResult): Result<Unit>
    suspend fun getCompletedSessions(childId: String): List<SessionResult>
    fun observeCompletedSessions(childId: String): Flow<List<SessionResult>>
}
