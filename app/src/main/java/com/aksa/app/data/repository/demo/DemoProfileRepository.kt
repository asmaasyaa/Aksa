package com.aksa.app.data.repository.demo

import com.aksa.app.domain.model.Profile
import com.aksa.app.domain.model.ProfileConfig
import com.aksa.app.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * In-memory ProfileRepository implementation used as a test double in unit tests.
 */
class DemoProfileRepository : ProfileRepository {
    // Map of profileId to Profile
    private val profiles = MutableStateFlow<Map<String, Profile>>(
        mapOf(
            "child_default" to Profile(
                profileId = "child_default",
                userId = "demo_guardian_default",
                name = "Marvin",
                avatarId = "mascot_classic",
                age = 8
            )
        )
    )

    override fun observeProfiles(userId: String): Flow<List<Profile>> {
        return profiles.map { map ->
            map.values.filter { it.userId == userId }.sortedBy { it.createdAt }
        }
    }

    override suspend fun getProfiles(userId: String): List<Profile> {
        return profiles.value.values.filter { it.userId == userId }.sortedBy { it.createdAt }
    }

    override suspend fun getProfile(userId: String, profileId: String): Profile? {
        val profile = profiles.value[profileId]
        return if (profile?.userId == userId) profile else null
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

        val maxLimit = ProfileConfig.maxProfilesPerAccount
        if (maxLimit != null && getProfiles(userId).size >= maxLimit) {
            return Result.failure(IllegalStateException("Batas maksimum $maxLimit profil telah tercapai"))
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

        profiles.value = profiles.value + (profileId to newProfile)
        return Result.success(newProfile)
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

        val existing = profiles.value[profileId]
            ?: return Result.failure(SecurityException("Profil tidak ditemukan"))

        if (existing.userId != userId) {
            return Result.failure(SecurityException("Tidak memiliki izin untuk mengubah profil akun lain"))
        }

        val updated = existing.copy(
            name = trimmedName,
            avatarId = trimmedAvatar,
            age = if (age > 0) age else existing.age
        )
        profiles.value = profiles.value + (profileId to updated)
        return Result.success(updated)
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
        profiles.value = profiles.value + (profile.profileId to profile)
        return Result.success(Unit)
    }

    override fun observeActiveChildProfile(guardianUid: String): Flow<Profile?> {
        return observeProfiles(guardianUid).map { it.firstOrNull() }
    }
}
