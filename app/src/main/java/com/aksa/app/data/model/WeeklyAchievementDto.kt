package com.aksa.app.data.model

import com.aksa.app.domain.model.WeeklyAchievement

data class WeeklyAchievementDto(
    val achievementId: String = "",
    val childId: String = "",
    val weekStartDate: String = "",
    val milestoneDays: Int = 0,
    val awardedAt: Long = 0L
) {
    fun toDomain() = WeeklyAchievement(
        achievementId = achievementId,
        childId = childId,
        weekStartDate = weekStartDate,
        milestoneDays = milestoneDays,
        awardedAt = awardedAt
    )

    companion object {
        fun fromDomain(domain: WeeklyAchievement) = WeeklyAchievementDto(
            achievementId = domain.achievementId,
            childId = domain.childId,
            weekStartDate = domain.weekStartDate,
            milestoneDays = domain.milestoneDays,
            awardedAt = domain.awardedAt
        )
    }
}
