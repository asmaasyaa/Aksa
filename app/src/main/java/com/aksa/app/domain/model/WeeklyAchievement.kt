package com.aksa.app.domain.model

/**
 * Weekly achievement award for active days milestone (2, 3, or 7 active days in a calendar week).
 */
data class WeeklyAchievement(
    val achievementId: String,
    val childId: String,
    val weekStartDate: String, // ISO-8601 date string e.g. "2026-10-26" (Monday)
    val milestoneDays: Int,    // 2, 3, or 7
    val awardedAt: Long = System.currentTimeMillis()
)

data class MilestoneBadgeInfo(
    val milestoneDays: Int,
    val title: String,
    val description: String,
    val isEarnedThisWeek: Boolean,
    val cumulativeEarnedCount: Int
)
