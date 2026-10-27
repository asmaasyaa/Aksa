package com.aksa.app

import com.aksa.app.data.repository.demo.DemoAchievementRepository
import com.aksa.app.domain.usecase.EvaluateWeeklyMilestonesUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class WeeklyAchievementMilestonesTest {

    private lateinit var achievementRepository: DemoAchievementRepository
    private lateinit var evaluateWeeklyMilestones: EvaluateWeeklyMilestonesUseCase

    @Before
    fun setUp() {
        achievementRepository = DemoAchievementRepository()
        evaluateWeeklyMilestones = EvaluateWeeklyMilestonesUseCase(achievementRepository)
    }

    @Test
    fun milestone2DaysAwardedWhenActiveDaysIs2() = runTest {
        val week1Monday = LocalDate.of(2026, 10, 26)
        val childId = "child_123"

        // Day 1: activeDays = 1 -> no milestone
        val award1 = evaluateWeeklyMilestones(childId, week1Monday, activeDaysCount = 1)
        assertTrue(award1.isEmpty())

        // Day 2: activeDays = 2 -> milestone 2 awarded!
        val award2 = evaluateWeeklyMilestones(childId, week1Monday, activeDaysCount = 2)
        assertEquals(listOf(2), award2)

        val stored = achievementRepository.getAchievements(childId)
        assertEquals(1, stored.size)
        assertEquals(2, stored[0].milestoneDays)
    }

    @Test
    fun milestone3DaysAwardedWhenActiveDaysIs3() = runTest {
        val week1Monday = LocalDate.of(2026, 10, 26)
        val childId = "child_123"

        // Reach 2 days first
        evaluateWeeklyMilestones(childId, week1Monday, activeDaysCount = 2)

        // Then reach 3 days
        val award = evaluateWeeklyMilestones(childId, week1Monday, activeDaysCount = 3)
        assertEquals("Milestone 3 must be newly awarded", listOf(3), award)

        val stored = achievementRepository.getAchievements(childId)
        assertEquals(2, stored.size)
        assertTrue(stored.any { it.milestoneDays == 2 })
        assertTrue(stored.any { it.milestoneDays == 3 })
    }

    @Test
    fun allThreeMilestonesEarnedIndependentlyWhenReaching7Days() = runTest {
        val week1Monday = LocalDate.of(2026, 10, 26)
        val childId = "child_super"

        // Child jumps directly or achieves 7 active days in the week
        val awarded = evaluateWeeklyMilestones(childId, week1Monday, activeDaysCount = 7)

        assertEquals("Reaching 7 active days must earn all three milestones", listOf(2, 3, 7), awarded)

        val stored = achievementRepository.getAchievements(childId)
        assertEquals(3, stored.size)
        val earnedDays = stored.map { it.milestoneDays }.toSet()
        assertEquals(setOf(2, 3, 7), earnedDays)
    }

    @Test
    fun noDuplicateAwardInTheSameWeek() = runTest {
        val week1Monday = LocalDate.of(2026, 10, 26)
        val childId = "child_repeat"

        // Award milestone 2 on Day 2
        val firstAward = evaluateWeeklyMilestones(childId, week1Monday, activeDaysCount = 2)
        assertEquals(listOf(2), firstAward)

        // Evaluate again with activeDaysCount = 2 in the same week
        val secondAward = evaluateWeeklyMilestones(childId, week1Monday, activeDaysCount = 2)
        assertTrue("Milestone 2 must not be awarded twice in the same week", secondAward.isEmpty())

        val stored = achievementRepository.getAchievements(childId)
        assertEquals("Only one achievement stored for milestone 2", 1, stored.size)
    }

    @Test
    fun repeatedBadgeAwardsInDifferentWeeks() = runTest {
        val week1Monday = LocalDate.of(2026, 10, 19)
        val week2Monday = LocalDate.of(2026, 10, 26)
        val childId = "child_champion"

        // Week 1: reaches 2 active days
        val week1Award = evaluateWeeklyMilestones(childId, week1Monday, activeDaysCount = 2)
        assertEquals(listOf(2), week1Award)

        // Week 2: reaches 2 active days again
        val week2Award = evaluateWeeklyMilestones(childId, week2Monday, activeDaysCount = 2)
        assertEquals("Milestone 2 can be earned again in a new calendar week", listOf(2), week2Award)

        val stored = achievementRepository.getAchievements(childId)
        assertEquals("Total achievements across weeks must be 2", 2, stored.size)
        assertEquals(2, stored.count { it.milestoneDays == 2 })
        assertTrue(stored.any { it.weekStartDate == week1Monday.toString() })
        assertTrue(stored.any { it.weekStartDate == week2Monday.toString() })
    }
}
