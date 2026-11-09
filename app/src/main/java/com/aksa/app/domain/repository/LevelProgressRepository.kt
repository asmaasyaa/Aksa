package com.aksa.app.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository tracking earned level progression across the four AKSA adventure levels.
 * Guarantees that earned unlocks are never lost even if rolling session averages later fluctuate.
 */
interface LevelProgressRepository {
    suspend fun getUnlockedLevels(childId: String): Set<String>
    suspend fun unlockLevel(childId: String, moduleId: String): Result<Unit>
    suspend fun saveUnlockedLevels(childId: String, moduleIds: Set<String>): Result<Unit>
    fun observeUnlockedLevels(childId: String): Flow<Set<String>>
}

