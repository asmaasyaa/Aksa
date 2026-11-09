package com.aksa.app.domain.model

enum class LevelUnlockReason {
    DEFAULT_UNLOCKED,        // Level 1: Pulau Alfabet is unlocked by default
    PREVIOUS_LEVEL_MASTERED, // Unlocked by demonstrating performance mastery on previous level
    ALREADY_EARNED,          // Previously unlocked; retained even if rolling average later drops
    PREVIEW_OVERRIDE,        // Developer Preview Mode override (debug only)
    LOCKED_PREREQUISITE      // Locked because prerequisite level is not yet mastered
}

/**
 * Representation of a level node's progress and readiness state on the adventure map.
 */
data class LevelItemProgress(
    val levelNumber: Int,
    val moduleId: String,
    val title: String,
    val isUnlocked: Boolean,
    val isMastered: Boolean,
    val unlockReason: LevelUnlockReason,
    val readinessResult: LevelReadinessResult? = null
)

