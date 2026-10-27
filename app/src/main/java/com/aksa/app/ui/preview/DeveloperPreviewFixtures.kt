package com.aksa.app.ui.preview

import com.aksa.app.domain.model.ChildProfile
import com.aksa.app.domain.model.MilestoneBadgeInfo
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.model.WeeklyProgress
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Deterministic preview fixtures for Developer Preview Mode and Figma screenshot generation.
 * Completely isolated from real runtime data.
 */
object DeveloperPreviewFixtures {

    val previewChildProfile = ChildProfile(
        childId = "preview_child_marvin",
        guardianUid = "preview_guardian",
        displayName = "Marvin",
        age = 8,
        avatarId = "mascot_classic"
    )

    fun createWeeklyProgress(activeDays: Int): WeeklyProgress {
        val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val activeDates = (0 until activeDays.coerceIn(0, 7)).map { monday.plusDays(it.toLong()) }.toSet()
        val milestones = mutableSetOf<Int>()
        if (activeDays >= 2) milestones.add(2)
        if (activeDays >= 3) milestones.add(3)
        if (activeDays >= 7) milestones.add(7)

        return WeeklyProgress(
            weekStartDate = monday,
            activeDates = activeDates,
            totalActiveDays = activeDates.size,
            milestonesEarned = milestones,
            currentStreak = activeDays
        )
    }

    fun createMilestoneBadges(activeDays: Int): List<MilestoneBadgeInfo> {
        return listOf(
            MilestoneBadgeInfo(
                milestoneDays = 2,
                title = "Penjelajah 2 Hari",
                description = "Selesaikan latihan di 2 hari berbeda dalam satu minggu",
                isEarnedThisWeek = activeDays >= 2,
                cumulativeEarnedCount = if (activeDays >= 2) 1 else 0
            ),
            MilestoneBadgeInfo(
                milestoneDays = 3,
                title = "Juara Rajin 3 Hari",
                description = "Selesaikan latihan di 3 hari berbeda dalam satu minggu",
                isEarnedThisWeek = activeDays >= 3,
                cumulativeEarnedCount = if (activeDays >= 3) 1 else 0
            ),
            MilestoneBadgeInfo(
                milestoneDays = 7,
                title = "Bintang Sempurna Seminggu",
                description = "Luar biasa! Berlatih setiap hari dari Senin sampai Minggu",
                isEarnedThisWeek = activeDays >= 7,
                cumulativeEarnedCount = if (activeDays >= 7) 1 else 0
            )
        )
    }

    val sampleCompletedSession = SessionResult(
        sessionId = "session_preview_sample",
        childId = "preview_child_marvin",
        moduleId = "pulau_alfabet",
        activityId = "huruf_mirip",
        startedAt = System.currentTimeMillis() - 120_000,
        completedAt = System.currentTimeMillis(),
        totalQuestions = 10,
        correctAnswers = 10,
        incorrectAnswers = 0,
        accuracy = 1.0f,
        status = SessionStatus.COMPLETED
    )
}
