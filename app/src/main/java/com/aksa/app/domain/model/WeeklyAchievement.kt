package com.aksa.app.domain.model

data class WeeklyAchievement(
    val achievementId: String,
    val childId: String,
    val weekStartDate: String,
    val milestoneDays: Int,   
    val awardedAt: Long = System.currentTimeMillis(),
    val userId: String = ""
) {
    val badgeId: String get() = achievementId
    val profileId: String get() = childId
}

typealias Badge = WeeklyAchievement

data class MilestoneBadgeInfo(
    val milestoneDays: Int,
    val title: String,
    val description: String,
    val isEarnedThisWeek: Boolean,
    val cumulativeEarnedCount: Int
)
