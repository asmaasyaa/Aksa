package com.aksa.app.data.repository

import com.aksa.app.data.model.AnswerLogDto
import com.aksa.app.data.model.SessionResultDto
import com.aksa.app.data.remote.FirestorePaths
import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.SessionRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Cloud Firestore implementation of SessionRepository.
 * Stores sessions and answer logs under:
 * users/{parentUid}/profiles/{profileId}/sessionResults/{sessionId}
 * users/{parentUid}/profiles/{profileId}/answerLogs/{logId}
 */
class FirestoreSessionRepository(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SessionRepository {

    private fun getParentUid(): String {
        return authRepository.currentGuardian.value?.uid
            ?: throw IllegalStateException("Guardian must be authenticated to persist sessions")
    }

    override suspend fun recordAnswer(answer: AnswerLog): Result<Unit> {
        return try {
            require(answer.childId.isNotBlank()) { "childId must not be blank" }
            val parentUid = getParentUid()
            val dto = AnswerLogDto.fromDomain(answer, parentUid)
            firestore.document(
                FirestorePaths.answerLogDoc(parentUid, answer.childId, answer.answerId)
            ).set(dto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAnswersForSession(sessionId: String): List<AnswerLog> {
        return try {
            val snapshot = firestore.collectionGroup(FirestorePaths.ANSWER_LOGS)
                .whereEqualTo("sessionId", sessionId)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(AnswerLogDto::class.java)?.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAnswersForChildResult(childId: String): Result<List<AnswerLog>> {
        return try {
            require(childId.isNotBlank()) { "childId must not be blank" }
            val parentUid = getParentUid()
            val snapshot = firestore.collection(FirestorePaths.answerLogsCollection(parentUid, childId))
                .get()
                .await()

            Result.success(
                snapshot.documents.mapNotNull { it.toObject(AnswerLogDto::class.java)?.toDomain() }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAnswersForChild(childId: String): List<AnswerLog> {
        return getAnswersForChildResult(childId).getOrDefault(emptyList())
    }

    override suspend fun saveSessionResult(result: SessionResult): Result<Unit> {
        return try {
            require(result.childId.isNotBlank()) { "childId must not be blank" }
            val parentUid = getParentUid()
            val dto = SessionResultDto.fromDomain(result, parentUid)
            firestore.document(
                FirestorePaths.sessionResultDoc(parentUid, result.childId, result.sessionId)
            ).set(dto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCompletedSessionsResult(childId: String): Result<List<SessionResult>> {
        return try {
            require(childId.isNotBlank()) { "childId must not be blank" }
            val parentUid = getParentUid()
            val snapshot = firestore.collection(FirestorePaths.sessionResultsCollection(parentUid, childId))
                .whereEqualTo("status", SessionStatus.COMPLETED.name)
                .get()
                .await()

            Result.success(
                snapshot.documents.mapNotNull { it.toObject(SessionResultDto::class.java)?.toDomain() }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCompletedSessions(childId: String): List<SessionResult> {
        return getCompletedSessionsResult(childId).getOrDefault(emptyList())
    }

    override fun observeCompletedSessions(childId: String): Flow<List<SessionResult>> = callbackFlow {
        val parentUid = try { getParentUid() } catch (e: Exception) { null }
        if (parentUid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(FirestorePaths.sessionResultsCollection(parentUid, childId))
            .whereEqualTo("status", SessionStatus.COMPLETED.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(SessionResultDto::class.java)?.toDomain()
                } ?: emptyList()
                trySend(list)
            }

        awaitClose { registration.remove() }
    }
}
