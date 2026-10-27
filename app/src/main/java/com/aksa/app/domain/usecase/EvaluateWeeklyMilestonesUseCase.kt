package com.aksa.app.domain.usecase

import com.aksa.app.domain.repository.AchievementRepository
import java.time.LocalDate

/**
 * Evaluates whether active days in a week earn milestones (2, 3, and 7 distinct active days).
 * Each milestone is awarded independently once per calendar week.
 */
class EvaluateWeeklyMilestonesUseCase(
    private val achievementRepository: AchievementRepository
) {
    suspend operator fun invoke(
        childId: String,
        weekStartDate: LocalDate,
        activeDaysCount: Int
    ): List<Int> {
        val weekKey = weekStartDate.toString() // YYYY-MM-DD
        val awarded = mutableListOf<Int>()

        val milestonesToCheck = listOf(2, 3, 7)
        for (milestone in milestonesToCheck) {
            if (activeDaysCount >= milestone) {
                val wasNewlyAwarded = achievementRepository.awardMilestoneIfEligible(
                    childId = childId,
                    weekStartDate = weekKey,
                    milestoneDays = milestone
                )
                if (wasNewlyAwarded) {
                    awarded.add(milestone)
                }
            }
        }

        return awarded
    }
}
