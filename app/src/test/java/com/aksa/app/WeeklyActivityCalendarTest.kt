package com.aksa.app

import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.usecase.CalculateWeeklyProgressUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class WeeklyActivityCalendarTest {

    private val jakartaZone = ZoneId.of("Asia/Jakarta")

    // Fixed Wednesday, 2026-10-28 10:00:00 WIB
    // Monday of this week is 2026-10-26, Sunday is 2026-11-01
    private val fixedInstant = Instant.parse("2026-10-28T03:00:00Z") // 10:00 WIB
    private val fixedClock = Clock.fixed(fixedInstant, jakartaZone)

    private val calculateWeeklyProgress = CalculateWeeklyProgressUseCase(
        clock = fixedClock,
        zoneId = jakartaZone
    )

    private fun makeSession(
        id: String,
        date: LocalDate,
        hour: Int = 10,
        status: SessionStatus = SessionStatus.COMPLETED,
        correctAnswers: Int = 10
    ): SessionResult {
        val sessionInstant = date.atTime(hour, 0).atZone(jakartaZone).toInstant()
        return SessionResult(
            sessionId = id,
            childId = "child_1",
            moduleId = "pulau_alfabet",
            activityId = "huruf_mirip",
            startedAt = sessionInstant.toEpochMilli() - 300_000,
            completedAt = if (status == SessionStatus.COMPLETED) sessionInstant.toEpochMilli() else null,
            totalQuestions = 10,
            correctAnswers = correctAnswers,
            incorrectAnswers = 10 - correctAnswers,
            accuracy = correctAnswers / 10f,
            status = status
        )
    }

    @Test
    fun weekStartsOnMonday() {
        val progress = calculateWeeklyProgress(emptyList())
        assertEquals(DayOfWeek.MONDAY, progress.weekStartDate.dayOfWeek)
        assertEquals(LocalDate.of(2026, 10, 26), progress.weekStartDate)
    }

    @Test
    fun oneCompletedSessionCountsAsOneActiveDay() {
        val mondaySession = makeSession("s1", LocalDate.of(2026, 10, 26))
        val progress = calculateWeeklyProgress(listOf(mondaySession))

        assertEquals(1, progress.totalActiveDays)
        assertTrue(progress.isActiveDay(LocalDate.of(2026, 10, 26)))
        assertFalse(progress.isActiveDay(LocalDate.of(2026, 10, 27)))
    }

    @Test
    fun multipleSessionsOnSameDateCountAsSingleActiveDay() {
        val morningSession = makeSession("s1", LocalDate.of(2026, 10, 27), hour = 9)
        val afternoonSession = makeSession("s2", LocalDate.of(2026, 10, 27), hour = 14)
        val eveningSession = makeSession("s3", LocalDate.of(2026, 10, 27), hour = 19)

        val progress = calculateWeeklyProgress(listOf(morningSession, afternoonSession, eveningSession))

        assertEquals("Multiple sessions on Tuesday must equal 1 active day", 1, progress.totalActiveDays)
        assertTrue(progress.isActiveDay(LocalDate.of(2026, 10, 27)))
    }

    @Test
    fun abandonedOrIncompleteSessionsDoNotCountAsActiveDays() {
        val abandoned = makeSession("s1", LocalDate.of(2026, 10, 26), status = SessionStatus.ABANDONED)
        val inProgress = makeSession("s2", LocalDate.of(2026, 10, 27), status = SessionStatus.IN_PROGRESS)

        val progress = calculateWeeklyProgress(listOf(abandoned, inProgress))

        assertEquals(0, progress.totalActiveDays)
        assertFalse(progress.isActiveDay(LocalDate.of(2026, 10, 26)))
        assertFalse(progress.isActiveDay(LocalDate.of(2026, 10, 27)))
    }

    @Test
    fun incorrectAnswersDoNotCancelAnActiveDay() {
        // Child completed session with low accuracy (2 / 10 = 20%)
        val lowAccuracySession = makeSession(
            "s1",
            LocalDate.of(2026, 10, 26),
            status = SessionStatus.COMPLETED,
            correctAnswers = 2
        )

        val progress = calculateWeeklyProgress(listOf(lowAccuracySession))

        assertEquals("Completed session with low accuracy still counts as active day", 1, progress.totalActiveDays)
        assertTrue(progress.isActiveDay(LocalDate.of(2026, 10, 26)))
    }

    @Test
    fun sessionsFromPreviousWeeksDoNotCountTowardsCurrentWeek() {
        // Session from Sunday of prior week (2026-10-25)
        val priorWeekSession = makeSession("s_old", LocalDate.of(2026, 10, 25))
        // Session from current week Wednesday (2026-10-28)
        val currentWeekSession = makeSession("s_cur", LocalDate.of(2026, 10, 28))

        val progress = calculateWeeklyProgress(listOf(priorWeekSession, currentWeekSession))

        assertEquals(1, progress.totalActiveDays)
        assertFalse(progress.isActiveDay(LocalDate.of(2026, 10, 25)))
        assertTrue(progress.isActiveDay(LocalDate.of(2026, 10, 28)))
    }

    @Test
    fun streakCountsConsecutiveDays() {
        // Active Monday (26), Tuesday (27), Wednesday (28 = today)
        val s1 = makeSession("s1", LocalDate.of(2026, 10, 26))
        val s2 = makeSession("s2", LocalDate.of(2026, 10, 27))
        val s3 = makeSession("s3", LocalDate.of(2026, 10, 28))

        val progress = calculateWeeklyProgress(listOf(s1, s2, s3))
        assertEquals(3, progress.currentStreak)
    }
}
