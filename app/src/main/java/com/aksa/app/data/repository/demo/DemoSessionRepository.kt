package com.aksa.app.data.repository.demo

import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory session tracking repository for demo/offline usage.
 * Preserves session history and prevents duplicate session entries.
 */
class DemoSessionRepository : SessionRepository {
    private val answers = MutableStateFlow<List<AnswerLog>>(emptyList())
    private val sessions = MutableStateFlow<Map<String, SessionResult>>(emptyMap())

    override suspend fun recordAnswer(answer: AnswerLog): Result<Unit> {
        // Prevent duplicate AnswerLog with identical answerId or identical (sessionId, questionId)
        val current = answers.value
        val alreadyExists = current.any {
            it.answerId == answer.answerId || (it.sessionId == answer.sessionId && it.questionId == answer.questionId)
        }
        if (!alreadyExists) {
            answers.value = current + answer
        }
        return Result.success(Unit)
    }

    override suspend fun getAnswersForSession(sessionId: String): List<AnswerLog> {
        return answers.value.filter { it.sessionId == sessionId }
    }

    override suspend fun saveSessionResult(result: SessionResult): Result<Unit> {
        val current = sessions.value.toMutableMap()
        current[result.sessionId] = result
        sessions.value = current
        return Result.success(Unit)
    }

    override suspend fun getCompletedSessions(childId: String): List<SessionResult> {
        return sessions.value.values.filter { it.childId == childId && it.isCompleted }
    }

    override fun observeCompletedSessions(childId: String): Flow<List<SessionResult>> {
        return sessions.map { map ->
            map.values.filter { it.childId == childId && it.isCompleted }
        }
    }

    // Helper for tests/demo to preload completed sessions if needed
    fun seedSessions(seedList: List<SessionResult>) {
        val current = sessions.value.toMutableMap()
        for (s in seedList) {
            current[s.sessionId] = s
        }
        sessions.value = current
    }
}
