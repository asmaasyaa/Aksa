package com.aksa.app.domain.repository

import com.aksa.app.domain.model.WeeklyAchievement
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    suspend fun awardMilestoneIfEligible(
        childId: String,
        weekStartDate: String,
        milestoneDays: Int
    ): Boolean

    suspend fun getAchievements(childId: String): List<WeeklyAchievement>
    fun observeAchievements(childId: String): Flow<List<WeeklyAchievement>>
}
