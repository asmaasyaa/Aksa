package com.aksa.app.domain.usecase

import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.LevelItemProgress
import com.aksa.app.domain.model.LevelUnlockReason
import com.aksa.app.domain.repository.LevelProgressRepository
import com.aksa.app.ui.components.LevelNodeStatus

data class LevelMapProgress(
    val level1: LevelItemProgress,
    val level2: LevelItemProgress,
    val level3: LevelItemProgress,
    val level4: LevelItemProgress
) {
    fun getNodeStatus(levelNumber: Int): LevelNodeStatus {
        val item = when (levelNumber) {
            1 -> level1
            2 -> level2
            3 -> level3
            4 -> level4
            else -> return LevelNodeStatus.LOCKED
        }
        return when {
            !item.isUnlocked -> LevelNodeStatus.LOCKED
            item.isMastered -> LevelNodeStatus.COMPLETED
            else -> LevelNodeStatus.ACTIVE
        }
    }
}

/**
 * Use case to evaluate sequential progression and reconcile earned unlocks.
 * Preserves previously unlocked levels even if the child's rolling average later fluctuates.
 */
class GetLevelProgressionUseCase(
    private val evaluateReadinessUseCase: EvaluateLevelReadinessUseCase = EvaluateLevelReadinessUseCase(),
    private val levelProgressRepository: LevelProgressRepository = AksaRepositoryProvider.levelProgressRepository
) {
    suspend operator fun invoke(childId: String): LevelMapProgress {
        val persistedUnlocked = levelProgressRepository.getUnlockedLevels(childId)

        // LEVEL 1 — Pulau Alfabet (Always unlocked)
        val r1 = evaluateReadinessUseCase(childId, "pulau_alfabet")
        val level2Earned = r1.isReadyToUnlock || ("dunia_suku_kata" in persistedUnlocked)
        if (r1.isReadyToUnlock && "dunia_suku_kata" !in persistedUnlocked) {
            levelProgressRepository.unlockLevel(childId, "dunia_suku_kata")
        }

        // LEVEL 2 — Dunia Suku Kata
        val r2 = if (level2Earned) {
            evaluateReadinessUseCase(childId, "dunia_suku_kata")
        } else null

        val level3Earned = (level2Earned && r2?.isReadyToUnlock == true) || ("gunung_kata" in persistedUnlocked)
        if (level2Earned && r2?.isReadyToUnlock == true && "gunung_kata" !in persistedUnlocked) {
            levelProgressRepository.unlockLevel(childId, "gunung_kata")
        }

        // LEVEL 3 — Gunung Kata
        val r3 = if (level3Earned) {
            evaluateReadinessUseCase(childId, "gunung_kata")
        } else null

        val level4Earned = (level3Earned && r3?.isReadyToUnlock == true) || ("sungai_cerita" in persistedUnlocked)
        if (level3Earned && r3?.isReadyToUnlock == true && "sungai_cerita" !in persistedUnlocked) {
            levelProgressRepository.unlockLevel(childId, "sungai_cerita")
        }

        // LEVEL 4 — Sungai Cerita
        val r4 = if (level4Earned) {
            evaluateReadinessUseCase(childId, "sungai_cerita")
        } else null

        val level1Item = LevelItemProgress(
            levelNumber = 1,
            moduleId = "pulau_alfabet",
            title = "Pulau Alfabet",
            isUnlocked = true,
            isMastered = level2Earned || r1.isReadyToUnlock,
            unlockReason = LevelUnlockReason.DEFAULT_UNLOCKED,
            readinessResult = r1
        )

        val level2Item = LevelItemProgress(
            levelNumber = 2,
            moduleId = "dunia_suku_kata",
            title = "Dunia Suku Kata",
            isUnlocked = level2Earned,
            isMastered = level3Earned || (r2?.isReadyToUnlock == true),
            unlockReason = when {
                !level2Earned -> LevelUnlockReason.LOCKED_PREREQUISITE
                "dunia_suku_kata" in persistedUnlocked && !r1.isReadyToUnlock -> LevelUnlockReason.ALREADY_EARNED
                else -> LevelUnlockReason.PREVIOUS_LEVEL_MASTERED
            },
            readinessResult = r2
        )

        val level3Item = LevelItemProgress(
            levelNumber = 3,
            moduleId = "gunung_kata",
            title = "Gunung Kata",
            isUnlocked = level3Earned,
            isMastered = level4Earned || (r3?.isReadyToUnlock == true),
            unlockReason = when {
                !level3Earned -> LevelUnlockReason.LOCKED_PREREQUISITE
                "gunung_kata" in persistedUnlocked && (r2?.isReadyToUnlock != true) -> LevelUnlockReason.ALREADY_EARNED
                else -> LevelUnlockReason.PREVIOUS_LEVEL_MASTERED
            },
            readinessResult = r3
        )

        val level4Item = LevelItemProgress(
            levelNumber = 4,
            moduleId = "sungai_cerita",
            title = "Sungai Cerita",
            isUnlocked = level4Earned,
            isMastered = (r4?.isReadyToUnlock == true),
            unlockReason = when {
                !level4Earned -> LevelUnlockReason.LOCKED_PREREQUISITE
                "sungai_cerita" in persistedUnlocked && (r3?.isReadyToUnlock != true) -> LevelUnlockReason.ALREADY_EARNED
                else -> LevelUnlockReason.PREVIOUS_LEVEL_MASTERED
            },
            readinessResult = r4
        )

        return LevelMapProgress(
            level1 = level1Item,
            level2 = level2Item,
            level3 = level3Item,
            level4 = level4Item
        )
    }
}

