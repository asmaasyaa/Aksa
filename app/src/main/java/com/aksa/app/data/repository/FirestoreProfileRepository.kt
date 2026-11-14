package com.aksa.app.data.repository

import com.aksa.app.data.model.ProfileDto
import com.aksa.app.data.remote.FirestorePaths
import com.aksa.app.domain.model.Profile
import com.aksa.app.domain.model.ProfileConfig
import com.aksa.app.domain.repository.ProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Cloud Firestore implementation of ProfileRepository.
 * Manages child profiles under users/{parentUid}/profiles/{profileId}.
 */
class FirestoreProfileRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProfileRepository {

    override fun observeProfiles(userId: String): Flow<List<Profile>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(FirestorePaths.profilesCollection(userId))
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ProfileDto::class.java)?.toDomain()
                } ?: emptyList()
                trySend(list)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getProfiles(userId: String): List<Profile> {
        if (userId.isBlank()) return emptyList()
        return try {
            val snapshot = firestore.collection(FirestorePaths.profilesCollection(userId))
                .orderBy("createdAt")
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(ProfileDto::class.java)?.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getProfile(userId: String, profileId: String): Profile? {
        if (userId.isBlank() || profileId.isBlank()) return null
        return try {
            val doc = firestore.document(FirestorePaths.profileDoc(userId, profileId))
                .get()
                .await()

            doc.toObject(ProfileDto::class.java)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createProfile(
        userId: String,
        name: String,
        avatarId: String,
        age: Int
    ): Result<Profile> {
        val trimmedName = name.trim()
        val trimmedAvatar = avatarId.trim()

        if (userId.isBlank()) return Result.failure(IllegalArgumentException("User ID tidak boleh kosong"))
        if (trimmedName.isBlank()) return Result.failure(IllegalArgumentException("Nama profil tidak boleh kosong"))
        if (trimmedAvatar.isBlank()) return Result.failure(IllegalArgumentException("Avatar profil tidak boleh kosong"))

        return try {
            val maxLimit = ProfileConfig.maxProfilesPerAccount
            if (maxLimit != null) {
                val currentProfiles = getProfiles(userId)
                if (currentProfiles.size >= maxLimit) {
                    return Result.failure(IllegalStateException("Batas maksimum $maxLimit profil telah tercapai"))
                }
            }

            val profileId = "profile_${UUID.randomUUID()}"
            val newProfile = Profile(
                profileId = profileId,
                userId = userId,
                name = trimmedName,
                avatarId = trimmedAvatar,
                age = age,
                createdAt = System.currentTimeMillis()
            )

            val dto = ProfileDto.fromDomain(newProfile)
            firestore.document(FirestorePaths.profileDoc(userId, profileId))
                .set(dto)
                .await()

            Result.success(newProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(
        userId: String,
        profileId: String,
        name: String,
        avatarId: String,
        age: Int
    ): Result<Profile> {
        val trimmedName = name.trim()
        val trimmedAvatar = avatarId.trim()

        if (userId.isBlank()) return Result.failure(IllegalArgumentException("User ID tidak boleh kosong"))
        if (profileId.isBlank()) return Result.failure(IllegalArgumentException("Profile ID tidak boleh kosong"))
        if (trimmedName.isBlank()) return Result.failure(IllegalArgumentException("Nama profil tidak boleh kosong"))
        if (trimmedAvatar.isBlank()) return Result.failure(IllegalArgumentException("Avatar profil tidak boleh kosong"))

        return try {
            val docRef = firestore.document(FirestorePaths.profileDoc(userId, profileId))
            val snapshot = docRef.get().await()

            if (!snapshot.exists()) {
                return Result.failure(SecurityException("Profil tidak ditemukan"))
            }

            val existing = snapshot.toObject(ProfileDto::class.java)?.toDomain()
                ?: return Result.failure(IllegalStateException("Gagal memproses data profil"))

            // Verify ownership
            if (existing.userId != userId) {
                return Result.failure(SecurityException("Tidak memiliki izin untuk mengubah profil akun lain"))
            }

            val updated = existing.copy(
                name = trimmedName,
                avatarId = trimmedAvatar,
                age = if (age > 0) age else existing.age
            )

            docRef.set(ProfileDto.fromDomain(updated)).await()
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActiveChildProfile(guardianUid: String, activeProfileId: String?): Profile? {
        val allProfiles = getProfiles(guardianUid)
        if (allProfiles.isEmpty()) return null
        if (!activeProfileId.isNullOrBlank()) {
            val matched = allProfiles.find { it.profileId == activeProfileId }
            if (matched != null) return matched
        }
        return allProfiles.firstOrNull()
    }

    override suspend fun saveChildProfile(profile: Profile): Result<Unit> {
        if (profile.userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Profile userId tidak boleh kosong"))
        }
        if (profile.profileId.isBlank()) {
            return Result.failure(IllegalArgumentException("Profile profileId tidak boleh kosong"))
        }
        return try {
            val dto = ProfileDto.fromDomain(profile)
            firestore.document(FirestorePaths.profileDoc(profile.userId, profile.profileId))
                .set(dto)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeActiveChildProfile(guardianUid: String): Flow<Profile?> {
        return observeProfiles(guardianUid).map { it.firstOrNull() }
    }
}
