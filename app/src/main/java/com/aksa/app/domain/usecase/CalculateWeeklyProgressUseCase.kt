package com.aksa.app.domain.usecase

import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.WeeklyProgress
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Calculates weekly activity progress for the current week starting on Monday.
 * Uses Asia/Jakarta timezone by default and supports an injectable [Clock] for tests.
 */
class CalculateWeeklyProgressUseCase(
    private val clock: Clock = Clock.system(ZoneId.of("Asia/Jakarta")),
    private val zoneId: ZoneId = ZoneId.of("Asia/Jakarta")
) {
    operator fun invoke(
        completedSessions: List<SessionResult>,
        milestonesEarned: Set<Int> = emptySet()
    ): WeeklyProgress {
        val today = LocalDate.now(clock.withZone(zoneId))
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.plusDays(6)

        // Filter valid completed sessions that have a completedAt timestamp
        val completedInWeek = completedSessions.filter { session ->
            session.isCompleted && session.completedAt != null
        }

        // Group completed sessions by local date in the configured timezone
        val activeDatesInWeek = completedInWeek.mapNotNull { session ->
            val timestamp = session.completedAt ?: return@mapNotNull null
            val sessionDate = Instant.ofEpochMilli(timestamp)
                .atZone(zoneId)
                .toLocalDate()

            if (!sessionDate.isBefore(monday) && !sessionDate.isAfter(sunday)) {
                sessionDate
            } else {
                null
            }
        }.toSet()

        // Calculate consecutive active day streak leading up to today
        val allActiveDates = completedSessions
            .filter { it.isCompleted && it.completedAt != null }
            .mapNotNull {
                Instant.ofEpochMilli(it.completedAt!!)
                    .atZone(zoneId)
                    .toLocalDate()
            }.toSet()

        var streak = 0
        var checkDate = today
        // If child hasn't practiced yet today, check if yesterday was active
        if (!allActiveDates.contains(today) && allActiveDates.contains(today.minusDays(1))) {
            checkDate = today.minusDays(1)
        }

        while (allActiveDates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return WeeklyProgress(
            weekStartDate = monday,
            activeDates = activeDatesInWeek,
            totalActiveDays = activeDatesInWeek.size,
            milestonesEarned = milestonesEarned,
            currentStreak = streak
        )
    }
}
