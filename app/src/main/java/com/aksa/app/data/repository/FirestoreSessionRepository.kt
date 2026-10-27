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
 * Real Firestore implementation of SessionRepository.
 * Stores sessions and answer logs under guardian/child subcollections.
 */
class FirestoreSessionRepository(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SessionRepository {

    private fun getGuardianUid(): String {
        return authRepository.currentGuardian.value?.uid
            ?: throw IllegalStateException("Guardian must be authenticated to persist sessions")
    }

    override suspend fun recordAnswer(answer: AnswerLog): Result<Unit> {
        return try {
            val guardianUid = getGuardianUid()
            val dto = AnswerLogDto.fromDomain(answer)
            firestore.document(
                "${FirestorePaths.answersCollection(guardianUid, answer.childId, answer.sessionId)}/${answer.answerId}"
            ).set(dto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAnswersForSession(sessionId: String): List<AnswerLog> {
        return try {
            // Query across answers collection group or directly if child is known
            val snapshot = firestore.collectionGroup(FirestorePaths.ANSWERS)
                .whereEqualTo("sessionId", sessionId)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(AnswerLogDto::class.java)?.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun saveSessionResult(result: SessionResult): Result<Unit> {
        return try {
            val guardianUid = getGuardianUid()
            val dto = SessionResultDto.fromDomain(result)
            firestore.document(
                FirestorePaths.sessionDoc(guardianUid, result.childId, result.sessionId)
            ).set(dto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCompletedSessions(childId: String): List<SessionResult> {
        return try {
            val guardianUid = getGuardianUid()
            val snapshot = firestore.collection(FirestorePaths.sessionsCollection(guardianUid, childId))
                .whereEqualTo("status", SessionStatus.COMPLETED.name)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(SessionResultDto::class.java)?.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun observeCompletedSessions(childId: String): Flow<List<SessionResult>> = callbackFlow {
        val guardianUid = try { getGuardianUid() } catch (e: Exception) { null }
        if (guardianUid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(FirestorePaths.sessionsCollection(guardianUid, childId))
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
