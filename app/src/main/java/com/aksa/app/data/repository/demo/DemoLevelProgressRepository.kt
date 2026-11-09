package com.aksa.app.data.repository.demo

import com.aksa.app.domain.repository.LevelProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory level progression repository for demo/offline usage.
 * Level 1 ("pulau_alfabet") is always unlocked by default for any child.
 * Unlocked levels persist in-memory across the session and never relock on subsequent low scores.
 */
class DemoLevelProgressRepository : LevelProgressRepository {

    companion object {
        const val DEFAULT_LEVEL = "pulau_alfabet"
    }

    private val unlockedMap = MutableStateFlow<Map<String, Set<String>>>(emptyMap())

    override suspend fun getUnlockedLevels(childId: String): Set<String> {
        val childSet = unlockedMap.value[childId] ?: emptySet()
        return childSet + DEFAULT_LEVEL
    }

    override suspend fun unlockLevel(childId: String, moduleId: String): Result<Unit> {
        val current = unlockedMap.value.toMutableMap()
        val existing = (current[childId] ?: emptySet()).toMutableSet()
        existing.add(DEFAULT_LEVEL)
        existing.add(moduleId)
        current[childId] = existing
        unlockedMap.value = current
        return Result.success(Unit)
    }

    override suspend fun saveUnlockedLevels(childId: String, moduleIds: Set<String>): Result<Unit> {
        val current = unlockedMap.value.toMutableMap()
        current[childId] = moduleIds + DEFAULT_LEVEL
        unlockedMap.value = current
        return Result.success(Unit)
    }

    override fun observeUnlockedLevels(childId: String): Flow<Set<String>> {
        return unlockedMap.map { map ->
            (map[childId] ?: emptySet()) + DEFAULT_LEVEL
        }
    }

    fun reset() {
        unlockedMap.value = emptyMap()
    }
}

