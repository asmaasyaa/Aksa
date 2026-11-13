package com.aksa.app

import com.aksa.app.data.repository.demo.DemoAchievementRepository
import com.aksa.app.data.repository.demo.DemoAuthRepository
import com.aksa.app.data.repository.demo.DemoProfileRepository
import com.aksa.app.data.repository.demo.DemoSessionRepository
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.ui.viewmodel.MiniGameStage
import com.aksa.app.ui.viewmodel.TangkapHurufViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class TangkapHurufSessionLifecycleTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var sessionRepository: DemoSessionRepository
    private lateinit var achievementRepository: DemoAchievementRepository
    private lateinit var authRepository: DemoAuthRepository
    private lateinit var profileRepository: DemoProfileRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sessionRepository = DemoSessionRepository()
        achievementRepository = DemoAchievementRepository()
        authRepository = DemoAuthRepository()
        profileRepository = DemoProfileRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TangkapHurufViewModel {
        return TangkapHurufViewModel(
            audioSpeaker = null,
            sessionRepository = sessionRepository,
            achievementRepository = achievementRepository,
            authRepository = authRepository,
            random = Random(123)
        )
    }

    @Test
    fun sessionCompletesOnlyAfter15thRound() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.startGame()
        advanceUntilIdle()

        assertEquals(15, vm.uiState.value.totalRounds)
        assertEquals(1, vm.uiState.value.currentRound)

        // Play rounds 1 through 14
        for (round in 1..14) {
            val targetItem = vm.uiState.value.fallingLetters.first { it.isTarget }

            vm.onLetterCaught(targetItem, responseTimeMs = 2000)
            advanceUntilIdle()

            assertTrue(vm.uiState.value.roundResolved)
            vm.nextRoundOrFinish()
            advanceUntilIdle()
        }

        assertEquals(15, vm.uiState.value.currentRound)

        // Catch 15th round target
        val lastTarget = vm.uiState.value.fallingLetters.first { it.isTarget }
        vm.onLetterCaught(lastTarget, responseTimeMs = 2000)
        advanceUntilIdle()

        vm.nextRoundOrFinish()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNotNull(state.sessionResult)
        assertEquals(SessionStatus.COMPLETED, state.sessionResult?.status)
        assertEquals(15, state.sessionResult?.totalQuestions)
        assertEquals(15, state.sessionResult?.correctAnswers)
        assertEquals(1.0f, state.accuracy, 0.01f)
        assertEquals(3, state.starRating) // 100% = 3 stars

        // Verify session saved in repository
        val completed = sessionRepository.getCompletedSessions(state.sessionResult!!.childId)
        assertEquals(1, completed.size)
        assertEquals(state.sessionResult!!.sessionId, completed[0].sessionId)
    }

    @Test
    fun earlyExitDoesNotMarkSessionAsCompleted() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.startGame()
        advanceUntilIdle()

        // Complete 5 rounds
        for (round in 1..5) {
            val targetItem = vm.uiState.value.fallingLetters.first { it.isTarget }
            vm.onLetterCaught(targetItem, responseTimeMs = 2000)
            advanceUntilIdle()
            vm.nextRoundOrFinish()
            advanceUntilIdle()
        }

        // Child exits early
        vm.abandonSession()
        advanceUntilIdle()

        // Check repository: must NOT contain any COMPLETED session
        val completed = sessionRepository.getCompletedSessions("child_demo_guardian_default")
        assertTrue("Abandoned session must not count as completed", completed.isEmpty())
    }

    @Test
    fun noDuplicateAnswerLogForSameRound() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.startGame()
        advanceUntilIdle()

        val targetItem = vm.uiState.value.fallingLetters.first { it.isTarget }
        val distractorItem = vm.uiState.value.fallingLetters.first { !it.isTarget }

        // Catch target letter first
        vm.onLetterCaught(targetItem, responseTimeMs = 2500)
        advanceUntilIdle()

        // Attempting to catch distractor or miss in the same round should be ignored
        vm.onLetterCaught(distractorItem, responseTimeMs = 2800)
        advanceUntilIdle()

        vm.onLetterMissed(responseTimeMs = 3000)
        advanceUntilIdle()

        val answers = sessionRepository.getAnswersForSession(vm.uiState.value.sessionId)
        assertEquals("Only one AnswerLog recorded for round 1", 1, answers.size)
        assertEquals(targetItem.letter, answers[0].chosenChar)
        assertTrue(answers[0].isCorrect)
    }

    @Test
    fun scoringAndSpeedBonusCalculation() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.startGame()
        advanceUntilIdle()

        // Level 1: 10 points base. Fast (<3000ms) = +5 bonus = 15 total
        val targetItem = vm.uiState.value.fallingLetters.first { it.isTarget }
        vm.onLetterCaught(targetItem, responseTimeMs = 2000) // fast
        advanceUntilIdle()

        assertEquals(15, vm.uiState.value.score)
        assertEquals(15, vm.uiState.value.lastScoreEarned)
        assertTrue(vm.uiState.value.lastWasFast)

        vm.nextRoundOrFinish()
        advanceUntilIdle()

        // Slow correct (>3000ms): 10 points base, 0 bonus = 10 points
        val targetItem2 = vm.uiState.value.fallingLetters.first { it.isTarget }
        vm.onLetterCaught(targetItem2, responseTimeMs = 3500) // slow
        advanceUntilIdle()

        assertEquals(25, vm.uiState.value.score) // 15 + 10 = 25
        assertEquals(10, vm.uiState.value.lastScoreEarned)
        assertFalse(vm.uiState.value.lastWasFast)
    }

    @Test
    fun adaptiveDifficultyLevelProgression() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.startGame()
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.difficultyLevel)

        // 3 consecutive correct -> level up to 2
        for (i in 1..3) {
            val targetItem = vm.uiState.value.fallingLetters.first { it.isTarget }
            vm.onLetterCaught(targetItem, responseTimeMs = 2000)
            advanceUntilIdle()
            if (i < 3) {
                vm.nextRoundOrFinish()
                advanceUntilIdle()
            }
        }
        assertEquals(2, vm.uiState.value.difficultyLevel)
        assertEquals(0, vm.uiState.value.consecutiveCorrect)

        vm.nextRoundOrFinish()
        advanceUntilIdle()

        // 3 consecutive correct -> level up to 3
        for (i in 1..3) {
            val targetItem = vm.uiState.value.fallingLetters.first { it.isTarget }
            vm.onLetterCaught(targetItem, responseTimeMs = 2000)
            advanceUntilIdle()
            if (i < 3) {
                vm.nextRoundOrFinish()
                advanceUntilIdle()
            }
        }
        assertEquals(3, vm.uiState.value.difficultyLevel)

        vm.nextRoundOrFinish()
        advanceUntilIdle()

        // 2 consecutive wrong -> level down to 2
        for (i in 1..2) {
            val distractor = vm.uiState.value.fallingLetters.first { !it.isTarget }
            vm.onLetterCaught(distractor, responseTimeMs = 2000)
            advanceUntilIdle()
            if (i < 2) {
                vm.nextRoundOrFinish()
                advanceUntilIdle()
            }
        }
        assertEquals(2, vm.uiState.value.difficultyLevel)
    }

    @Test
    fun starRatingCalculation() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.startGame()
        advanceUntilIdle()

        // 14 correct (93.3% accuracy -> 3 stars)
        for (round in 1..15) {
            val falling = vm.uiState.value.fallingLetters
            if (round <= 14) {
                vm.onLetterCaught(falling.first { it.isTarget }, 2000)
            } else {
                vm.onLetterCaught(falling.first { !it.isTarget }, 2000)
            }
            advanceUntilIdle()
            vm.nextRoundOrFinish()
            advanceUntilIdle()
        }

        val state = vm.uiState.value
        assertEquals(14, state.correctAnswersCount)
        assertEquals(3, state.starRating)
        assertEquals(SessionStatus.COMPLETED, state.sessionResult?.status)
    }

    @Test
    fun staggeredSpawnDelaysAreAssignedAcrossAllLetters() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.startGame()
        advanceUntilIdle()

        val letters = vm.uiState.value.fallingLetters
        assertTrue("Round should have at least 2 letters", letters.size >= 2)

        // Verify strictly increasing spawn delays with controlled gap
        for (i in 0 until letters.size - 1) {
            val current = letters[i]
            val next = letters[i + 1]
            assertTrue(
                "Letter ${i + 1} delay (${next.spawnDelayMs}) should be greater than Letter $i delay (${current.spawnDelayMs})",
                next.spawnDelayMs > current.spawnDelayMs
            )
            val gap = next.spawnDelayMs - current.spawnDelayMs
            assertTrue("Gap between letters should be controlled (>= 1500ms), was $gap", gap >= 1500L)
        }
    }

    @Test
    fun variedHorizontalPositionsWithinBounds() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.startGame()
        advanceUntilIdle()

        val letters = vm.uiState.value.fallingLetters
        assertTrue(letters.isNotEmpty())

        letters.forEach { item ->
            assertTrue("normX (${item.normX}) must be >= 0.15f", item.normX >= 0.15f)
            assertTrue("normX (${item.normX}) must be <= 0.85f", item.normX <= 0.85f)
        }

        // Check horizontal separation between distinct items
        for (i in 0 until letters.size) {
            for (j in i + 1 until letters.size) {
                val diff = kotlin.math.abs(letters[i].normX - letters[j].normX)
                assertTrue("Different letters must not spawn at same X (diff >= 0.15f), was $diff", diff >= 0.15f)
            }
        }
    }

    @Test
    fun distinctStateAndItemsPerRound() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.startGame()
        advanceUntilIdle()

        val round1Letters = vm.uiState.value.fallingLetters
        val round1Target = vm.uiState.value.targetLetter
        assertTrue(round1Letters.all { it.id.startsWith("item_1_") })

        val round1TargetItem = round1Letters.first { it.isTarget }
        vm.onLetterCaught(round1TargetItem, 2000)
        advanceUntilIdle()
        vm.nextRoundOrFinish()
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.currentRound)
        assertFalse(vm.uiState.value.roundResolved)

        val round2Letters = vm.uiState.value.fallingLetters
        assertTrue(round2Letters.all { it.id.startsWith("item_2_") })

        // Check that item IDs don't overlap with round 1
        val round1Ids = round1Letters.map { it.id }.toSet()
        val round2Ids = round2Letters.map { it.id }.toSet()
        assertTrue("Round 2 letters must be brand new items", round1Ids.intersect(round2Ids).isEmpty())
    }

    @Test
    fun full15RoundsProgressionWithStaggeredLetters() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.startGame()
        advanceUntilIdle()

        for (round in 1..15) {
            assertEquals(round, vm.uiState.value.currentRound)
            val letters = vm.uiState.value.fallingLetters
            assertTrue("Round $round must have falling letters", letters.isNotEmpty())
            assertTrue("Round $round must have exactly one target", letters.count { it.isTarget } == 1)

            // Letters must have valid delays and positions
            letters.forEach { item ->
                assertTrue(item.normX in 0.15f..0.85f)
                assertTrue(item.spawnDelayMs >= 0L)
                assertTrue(item.fallDurationMs in 3000L..5000L)
            }

            val target = letters.first { it.isTarget }
            vm.onLetterCaught(target, 2000)
            advanceUntilIdle()
            vm.nextRoundOrFinish()
            advanceUntilIdle()
        }

        assertEquals(MiniGameStage.RESULTS, vm.uiState.value.stage)
        assertEquals(SessionStatus.COMPLETED, vm.uiState.value.sessionResult?.status)
    }
}
