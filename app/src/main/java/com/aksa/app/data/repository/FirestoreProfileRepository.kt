package com.aksa.app.data.repository

import com.aksa.app.data.model.ChildProfileDto
import com.aksa.app.data.remote.FirestorePaths
import com.aksa.app.domain.model.ChildProfile
import com.aksa.app.domain.repository.ProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Real Firestore implementation of ProfileRepository.
 */
class FirestoreProfileRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProfileRepository {

    override suspend fun getActiveChildProfile(guardianUid: String): ChildProfile? {
        val snapshot = firestore.collection(FirestorePaths.childrenCollection(guardianUid))
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject(ChildProfileDto::class.java)?.toDomain()
    }

    override suspend fun saveChildProfile(profile: ChildProfile): Result<Unit> {
        return try {
            val dto = ChildProfileDto.fromDomain(profile)
            firestore.document(FirestorePaths.childDoc(profile.guardianUid, profile.childId))
                .set(dto)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeActiveChildProfile(guardianUid: String): Flow<ChildProfile?> = callbackFlow {
        val registration = firestore.collection(FirestorePaths.childrenCollection(guardianUid))
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val profile = snapshot?.documents?.firstOrNull()
                    ?.toObject(ChildProfileDto::class.java)
                    ?.toDomain()
                trySend(profile)
            }

        awaitClose { registration.remove() }
    }
}
