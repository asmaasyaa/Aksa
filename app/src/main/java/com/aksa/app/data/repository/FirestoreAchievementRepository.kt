package com.aksa.app.data.repository

import com.aksa.app.data.model.WeeklyAchievementDto
import com.aksa.app.data.remote.FirestorePaths
import com.aksa.app.domain.model.WeeklyAchievement
import com.aksa.app.domain.repository.AchievementRepository
import com.aksa.app.domain.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Cloud Firestore implementation of AchievementRepository (Badges).
 * Stores badges under users/{parentUid}/profiles/{profileId}/badges/{badgeId}.
 * Uses idempotent document IDs "${weekStartDate}_${milestoneDays}" to guarantee single award per week.
 */
class FirestoreAchievementRepository(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AchievementRepository {

    private fun getParentUid(): String {
        return authRepository.currentGuardian.value?.uid
            ?: throw IllegalStateException("Guardian must be authenticated to access badges")
    }

    override suspend fun awardMilestoneIfEligible(
        childId: String,
        weekStartDate: String,
        milestoneDays: Int
    ): Boolean {
        return try {
            val parentUid = getParentUid()
            val docId = "${weekStartDate}_$milestoneDays"
            val docRef = firestore.document(FirestorePaths.badgeDoc(parentUid, childId, docId))

            val existing = docRef.get().await()
            if (existing.exists()) {
                // Already earned for this calendar week
                return false
            }

            val achievement = WeeklyAchievement(
                achievementId = "badge_${UUID.randomUUID()}",
                childId = childId,
                weekStartDate = weekStartDate,
                milestoneDays = milestoneDays,
                awardedAt = System.currentTimeMillis(),
                userId = parentUid
            )

            val dto = WeeklyAchievementDto.fromDomain(achievement, parentUid)
            docRef.set(dto).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getAchievements(childId: String): List<WeeklyAchievement> {
        return try {
            val parentUid = getParentUid()
            val snapshot = firestore.collection(FirestorePaths.badgesCollection(parentUid, childId))
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(WeeklyAchievementDto::class.java)?.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun observeAchievements(childId: String): Flow<List<WeeklyAchievement>> = callbackFlow {
        val parentUid = try { getParentUid() } catch (e: Exception) { null }
        if (parentUid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(FirestorePaths.badgesCollection(parentUid, childId))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(WeeklyAchievementDto::class.java)?.toDomain()
                } ?: emptyList()
                trySend(list)
            }

        awaitClose { registration.remove() }
    }
}
