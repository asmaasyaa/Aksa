package com.aksa.app.domain.repository

import com.aksa.app.domain.model.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing child profiles under a parent account.
 */
interface ProfileRepository {
    /**
     * Observes all profiles belonging to the given parent [userId] in real time.
     */
    fun observeProfiles(userId: String): Flow<List<Profile>>

    /**
     * Gets all profiles belonging to the given parent [userId].
     */
    suspend fun getProfiles(userId: String): List<Profile>

    /**
     * Gets a specific profile by [profileId] belonging to [userId].
     */
    suspend fun getProfile(userId: String, profileId: String): Profile?

    /**
     * Creates a new child profile for [userId].
     * Fails if name or avatar is blank, or if max profile limit is reached.
     */
    suspend fun createProfile(
        userId: String,
        name: String,
        avatarId: String,
        age: Int = 0
    ): Result<Profile>

    /**
     * Updates an existing profile, verifying that [userId] is the legitimate owner.
     */
    suspend fun updateProfile(
        userId: String,
        profileId: String,
        name: String,
        avatarId: String,
        age: Int = 0
    ): Result<Profile>

    // --- Backwards-compatible methods ---

    /**
     * Gets the first/active child profile for the given [guardianUid].
     * If [activeProfileId] is supplied, attempts to load that specific profile first.
     */
    suspend fun getActiveChildProfile(guardianUid: String, activeProfileId: String? = null): Profile?

    /**
     * Saves or overwrites a profile document.
     */
    suspend fun saveChildProfile(profile: Profile): Result<Unit>

    /**
     * Observes the first/active child profile in real-time.
     */
    fun observeActiveChildProfile(guardianUid: String): Flow<Profile?>
}
