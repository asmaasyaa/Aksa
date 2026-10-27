package com.aksa.app.data.repository.demo

import com.aksa.app.domain.model.WeeklyAchievement
import com.aksa.app.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * In-memory achievement repository for demo/offline usage.
 * Idempotently awards milestones once per child per calendar week.
 */
class DemoAchievementRepository : AchievementRepository {
    // Map key: "${childId}_${weekStartDate}_${milestoneDays}"
    private val achievements = MutableStateFlow<Map<String, WeeklyAchievement>>(emptyMap())

    override suspend fun awardMilestoneIfEligible(
        childId: String,
        weekStartDate: String,
        milestoneDays: Int
    ): Boolean {
        val compositeKey = "${childId}_${weekStartDate}_${milestoneDays}"
        val current = achievements.value.toMutableMap()

        if (current.containsKey(compositeKey)) {
            // Already awarded for this week
            return false
        }

        val achievement = WeeklyAchievement(
            achievementId = "ach_${UUID.randomUUID()}",
            childId = childId,
            weekStartDate = weekStartDate,
            milestoneDays = milestoneDays,
            awardedAt = System.currentTimeMillis()
        )
        current[compositeKey] = achievement
        achievements.value = current
        return true
    }

    override suspend fun getAchievements(childId: String): List<WeeklyAchievement> {
        return achievements.value.values.filter { it.childId == childId }
    }

    override fun observeAchievements(childId: String): Flow<List<WeeklyAchievement>> {
        return achievements.map { map ->
            map.values.filter { it.childId == childId }
        }
    }
}
