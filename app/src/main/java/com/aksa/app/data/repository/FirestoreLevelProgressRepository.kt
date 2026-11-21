package com.aksa.app.data.repository

import com.aksa.app.data.remote.FirestorePaths
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.LevelProgressRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Durable, monotonic unlock record per actual child profile. */
class FirestoreLevelProgressRepository(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : LevelProgressRepository {
    private val defaultLevel = "pulau_alfabet"

    private fun parentUid(): String = authRepository.currentGuardian.value?.uid
        ?: throw IllegalStateException("Guardian not authenticated")

    private fun path(profileId: String): String {
        require(profileId.isNotBlank()) { "profileId must not be blank" }
        return FirestorePaths.levelProgressDoc(parentUid(), profileId)
    }

    private fun unlockedFrom(data: Any?): Set<String> =
        ((data as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()) + defaultLevel

    override suspend fun getUnlockedLevels(childId: String): Set<String> {
        val snapshot = firestore.document(path(childId)).get().await()
        return unlockedFrom(snapshot.get("unlockedLevels"))
    }

    override suspend fun unlockLevel(childId: String, moduleId: String): Result<Unit> {
        return saveUnlockedLevels(childId, setOf(moduleId))
    }

    /** Array union is monotonic: past earned levels cannot be revoked by later low scores. */
    override suspend fun saveUnlockedLevels(childId: String, moduleIds: Set<String>): Result<Unit> {
        return runCatching {
            val parent = parentUid()
            val additions = (moduleIds + defaultLevel).toTypedArray()
            firestore.document(FirestorePaths.levelProgressDoc(parent, childId))
                .set(mapOf(
                    "userId" to parent,
                    "profileId" to childId,
                    "unlockedLevels" to FieldValue.arrayUnion(*additions)
                ), com.google.firebase.firestore.SetOptions.merge())
                .await()
            Unit
        }
    }

    override fun observeUnlockedLevels(childId: String): Flow<Set<String>> = callbackFlow {
        val ref = try { firestore.document(path(childId)) }
                  catch (e: Exception) { close(e); return@callbackFlow }
        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            trySend(unlockedFrom(snapshot?.get("unlockedLevels")))
        }
        awaitClose { registration.remove() }
    }
}
