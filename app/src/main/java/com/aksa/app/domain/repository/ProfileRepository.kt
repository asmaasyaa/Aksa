package com.aksa.app.domain.repository

import com.aksa.app.domain.model.ChildProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getActiveChildProfile(guardianUid: String): ChildProfile?
    suspend fun saveChildProfile(profile: ChildProfile): Result<Unit>
    fun observeActiveChildProfile(guardianUid: String): Flow<ChildProfile?>
}
