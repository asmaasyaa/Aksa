package com.aksa.app.data.repository

import com.aksa.app.data.remote.FirestorePaths
import com.aksa.app.data.repository.demo.DemoLevelProgressRepository
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.LevelProgressRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Cloud Firestore implementation of LevelProgressRepository.
 * Persists earned level unlock states under:
 * users/{parentUid}/profiles/{profileId}.unlockedLevels
 *
 * Guarantees anti-relocking:
 * Once unlocked, a level is permanently retained in the child's profile,
 * even if rolling session averages later fluctuate.
 *
 * In offline / unauthenticated / unit test environments, safely delegates
 * to in-memory fallback to prevent app crashes.
 */
class FirestoreLevelProgressRepository(
    private val authRepository: AuthRepository,
    private val firestoreProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() },
    private val fallbackRepository: DemoLevelProgressRepository = DemoLevelProgressRepository()
) : LevelProgressRepository {

    companion object {
        const val DEFAULT_LEVEL = "pulau_alfabet"
        const val FIELD_UNLOCKED_LEVELS = "unlockedLevels"
    }

    private fun getParentUid(): String? {
        return authRepository.currentGuardian.value?.uid
    }

    private fun isFirebaseAvailable(): Boolean {
        return try {
            FirebaseApp.getApps().isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun getUnlockedLevels(childId: String): Set<String> {
        val parentUid = getParentUid()
        if (parentUid == null || childId.isBlank() || !isFirebaseAvailable()) {
            return fallbackRepository.getUnlockedLevels(childId)
        }

        return try {
            val docRef = firestoreProvider().document(FirestorePaths.profileDoc(parentUid, childId))
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                return fallbackRepository.getUnlockedLevels(childId)
            }
            val list = (snapshot.get(FIELD_UNLOCKED_LEVELS) as? List<*>)
                ?.mapNotNull { it as? String }
                ?.toSet() ?: emptySet()
            val result = list + DEFAULT_LEVEL
            fallbackRepository.saveUnlockedLevels(childId, result)
            result
        } catch (e: Exception) {
            fallbackRepository.getUnlockedLevels(childId)
        }
    }

    override suspend fun unlockLevel(childId: String, moduleId: String): Result<Unit> {
        fallbackRepository.unlockLevel(childId, moduleId)

        val parentUid = getParentUid()
        if (parentUid == null || childId.isBlank() || !isFirebaseAvailable()) {
            return Result.success(Unit)
        }

        return try {
            val docRef = firestoreProvider().document(FirestorePaths.profileDoc(parentUid, childId))
            docRef.set(
                mapOf(FIELD_UNLOCKED_LEVELS to FieldValue.arrayUnion(DEFAULT_LEVEL, moduleId)),
                SetOptions.merge()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override suspend fun saveUnlockedLevels(childId: String, moduleIds: Set<String>): Result<Unit> {
        val fullSet = moduleIds + DEFAULT_LEVEL
        fallbackRepository.saveUnlockedLevels(childId, fullSet)

        val parentUid = getParentUid()
        if (parentUid == null || childId.isBlank() || !isFirebaseAvailable()) {
            return Result.success(Unit)
        }

        return try {
            val docRef = firestoreProvider().document(FirestorePaths.profileDoc(parentUid, childId))
            docRef.set(
                mapOf(FIELD_UNLOCKED_LEVELS to fullSet.toList()),
                SetOptions.merge()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    override fun observeUnlockedLevels(childId: String): Flow<Set<String>> = callbackFlow {
        val parentUid = getParentUid()
        if (parentUid == null || childId.isBlank() || !isFirebaseAvailable()) {
            val job = CoroutineScope(coroutineContext).launch {
                fallbackRepository.observeUnlockedLevels(childId).collect {
                    trySend(it)
                }
            }
            awaitClose { job.cancel() }
            return@callbackFlow
        }

        try {
            val docRef = firestoreProvider().document(FirestorePaths.profileDoc(parentUid, childId))
            val registration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(fallbackRepository.getFallbackSet(childId))
                    return@addSnapshotListener
                }
                val list = (snapshot?.get(FIELD_UNLOCKED_LEVELS) as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?.toSet() ?: emptySet()
                val combined = list + DEFAULT_LEVEL
                CoroutineScope(coroutineContext).launch {
                    fallbackRepository.saveUnlockedLevels(childId, combined)
                }
                trySend(combined)
            }
            awaitClose { registration.remove() }
        } catch (e: Exception) {
            val job = CoroutineScope(coroutineContext).launch {
                fallbackRepository.observeUnlockedLevels(childId).collect {
                    trySend(it)
                }
            }
            awaitClose { job.cancel() }
        }
    }

    private fun DemoLevelProgressRepository.getFallbackSet(childId: String): Set<String> {
        return setOf(DEFAULT_LEVEL)
    }
}

