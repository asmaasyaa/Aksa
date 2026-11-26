package com.aksa.app.data.model

import com.aksa.app.domain.model.WeeklyAchievement

/**
 * Firestore DTO for badges / weekly achievements.
 */
data class WeeklyAchievementDto(
    val achievementId: String = "",
    val badgeId: String = achievementId,
    val childId: String = "",
    val profileId: String = childId,
    val userId: String = "",
    val weekStartDate: String = "",
    val milestoneDays: Int = 0,
    val awardedAt: Long = 0L
) {
    fun toDomain() = WeeklyAchievement(
        achievementId = if (achievementId.isNotBlank()) achievementId else badgeId,
        childId = if (childId.isNotBlank()) childId else profileId,
        weekStartDate = weekStartDate,
        milestoneDays = milestoneDays,
        awardedAt = awardedAt,
        userId = userId
    )

    companion object {
        fun fromDomain(domain: WeeklyAchievement, parentUid: String = domain.userId) = WeeklyAchievementDto(
            achievementId = domain.achievementId,
            badgeId = domain.badgeId,
            childId = domain.childId,
            profileId = domain.profileId,
            userId = if (parentUid.isNotBlank()) parentUid else domain.userId,
            weekStartDate = domain.weekStartDate,
            milestoneDays = domain.milestoneDays,
            awardedAt = domain.awardedAt
        )
    }
}

typealias BadgeDto = WeeklyAchievementDto
