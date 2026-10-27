package com.aksa.app.domain.model

import java.time.LocalDate

/**
 * Computed weekly learning activity for current week starting on Monday.
 */
data class WeeklyProgress(
    val weekStartDate: LocalDate,
    val activeDates: Set<LocalDate>,
    val totalActiveDays: Int,
    val milestonesEarned: Set<Int>, // Set of 2, 3, 7
    val currentStreak: Int
) {
    fun isActiveDay(date: LocalDate): Boolean = activeDates.contains(date)
}
