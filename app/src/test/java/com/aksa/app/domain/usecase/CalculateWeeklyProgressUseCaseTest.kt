package com.aksa.app.domain.usecase

import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class CalculateWeeklyProgressUseCaseTest {

    private val zoneId = ZoneId.of("Asia/Jakarta")

    private fun createSession(date: LocalDate, completed: Boolean = true): SessionResult {
        val millis = date.atTime(10, 0).atZone(zoneId).toInstant().toEpochMilli()
        return SessionResult(
            sessionId = "sess_${date}",
            childId = "child_123",
            moduleId = "PULAU_ALFABET",
            activityId = "HURUF_MIRIP",
            startedAt = millis - 60000,
            completedAt = if (completed) millis else null,
            totalQuestions = 10,
            correctAnswers = 8,
            incorrectAnswers = 2,
            accuracy = 80f,
            status = if (completed) SessionStatus.COMPLETED else SessionStatus.IN_PROGRESS
        )
    }

    @Test
    fun `sesi ganda pada hari yang sama hanya dihitung 1 hari aktif`() {
        val today = LocalDate.of(2026, 11, 16) // Senin
        val fixedClock = Clock.fixed(today.atStartOfDay(zoneId).toInstant(), zoneId)
        val useCase = CalculateWeeklyProgressUseCase(fixedClock, zoneId)

        val sessions = listOf(
            createSession(today),
            createSession(today) // Sesi ke-2 di hari yang sama
        )

        val progress = useCase(sessions)
        assertEquals(1, progress.totalActiveDays)
        assertTrue(progress.isActiveDay(today))
    }

    @Test
    fun `sesi minggu lalu tidak masuk ke dalam totalActiveDays minggu ini tapi masuk streak`() {
        val today = LocalDate.of(2026, 11, 16) // Senin
        val yesterdaySunday = today.minusDays(1) // Minggu lalu
        val fixedClock = Clock.fixed(today.atStartOfDay(zoneId).toInstant(), zoneId)
        val useCase = CalculateWeeklyProgressUseCase(fixedClock, zoneId)

        val sessions = listOf(
            createSession(yesterdaySunday),
            createSession(today)
        )

        val progress = useCase(sessions)
        assertEquals(1, progress.totalActiveDays)
        assertFalse(progress.isActiveDay(yesterdaySunday))
        assertEquals(2, progress.currentStreak) // Berkelanjutan
    }

    @Test
    fun `streak tetap bertahan jika hari ini belum belajar tapi kemarin aktif`() {
        val today = LocalDate.of(2026, 11, 17) // Selasa
        val yesterday = today.minusDays(1)      // Senin
        val fixedClock = Clock.fixed(today.atStartOfDay(zoneId).toInstant(), zoneId)
        val useCase = CalculateWeeklyProgressUseCase(fixedClock, zoneId)

        val sessions = listOf(createSession(yesterday))

        val progress = useCase(sessions)
        assertEquals(1, progress.currentStreak)
    }

    @Test
    fun `streak reset jadi 0 jika kemarin dan hari ini tidak ada latihan`() {
        val today = LocalDate.of(2026, 11, 17)
        val twoDaysAgo = today.minusDays(2)
        val fixedClock = Clock.fixed(today.atStartOfDay(zoneId).toInstant(), zoneId)
        val useCase = CalculateWeeklyProgressUseCase(fixedClock, zoneId)

        val sessions = listOf(createSession(twoDaysAgo))

        val progress = useCase(sessions)
        assertEquals(0, progress.currentStreak)
    }
}