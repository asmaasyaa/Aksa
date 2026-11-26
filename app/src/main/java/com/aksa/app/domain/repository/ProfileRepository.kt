package com.aksa.app.domain.repository

import com.aksa.app.domain.model.Profile
import kotlinx.coroutines.flow.Flow
interface ProfileRepository {
    fun observeProfiles(userId: String): Flow<List<Profile>>
    suspend fun getProfiles(userId: String): List<Profile>

    suspend fun getProfile(userId: String, profileId: String): Profile?

      suspend fun createProfile(
        userId: String,
        name: String,
        avatarId: String,
        age: Int = 0
    ): Result<Profile>

    suspend fun updateProfile(
        userId: String,
        profileId: String,
        name: String,
        avatarId: String,
        age: Int = 0
    ): Result<Profile>

    suspend fun deleteProfile(
        userId: String,
        profileId: String
    ): Result<Unit>

    suspend fun getActiveChildProfile(guardianUid: String, activeProfileId: String? = null): Profile?

    suspend fun saveChildProfile(profile: Profile): Result<Unit>
    fun observeActiveChildProfile(guardianUid: String): Flow<Profile?>
}
