package com.aksa.app.data.repository.demo

import com.aksa.app.domain.model.ChildProfile
import com.aksa.app.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory child profile repository for demo/offline usage.
 */
class DemoProfileRepository : ProfileRepository {
    private val profiles = MutableStateFlow<Map<String, ChildProfile>>(
        mapOf(
            "demo_guardian_default" to ChildProfile(
                childId = "child_demo_guardian_default",
                guardianUid = "demo_guardian_default",
                displayName = "Marvin",
                age = 8,
                avatarId = "mascot_classic"
            )
        )
    )

    override suspend fun getActiveChildProfile(guardianUid: String): ChildProfile? {
        return profiles.value[guardianUid]
    }

    override suspend fun saveChildProfile(profile: ChildProfile): Result<Unit> {
        val current = profiles.value.toMutableMap()
        current[profile.guardianUid] = profile
        profiles.value = current
        return Result.success(Unit)
    }

    override fun observeActiveChildProfile(guardianUid: String): Flow<ChildProfile?> {
        return profiles.map { it[guardianUid] }
    }
}
