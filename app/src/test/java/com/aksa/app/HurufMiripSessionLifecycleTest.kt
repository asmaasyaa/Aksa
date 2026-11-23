package com.aksa.app

import com.aksa.app.data.repository.demo.DemoAchievementRepository
import com.aksa.app.data.repository.demo.DemoAuthRepository
import com.aksa.app.data.repository.demo.DemoProfileRepository
import com.aksa.app.data.repository.demo.DemoSessionRepository
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.usecase.AnalyzeErrorPatternsUseCase
import com.aksa.app.domain.usecase.GenerateHurufMiripQuestionsUseCase
import com.aksa.app.ui.viewmodel.HurufMiripViewModel
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class HurufMiripSessionLifecycleTest {

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

    private fun createViewModel(): HurufMiripViewModel {
        return HurufMiripViewModel(
            audioSpeaker = null,
            sessionRepository = sessionRepository,
            achievementRepository = achievementRepository,
            authRepository = authRepository,
            profileRepository = profileRepository,
            questionGenerator = GenerateHurufMiripQuestionsUseCase(Random(77)),
            analyzeErrorPatternsUseCase = AnalyzeErrorPatternsUseCase(sessionRepository)
        )
    }

    @Test
    fun sessionCompletesOnlyAfter10thQuestion() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(10, vm.uiState.value.questions.size)
        assertFalse(vm.uiState.value.showCompletionDialog)

        // Answer questions 1 through 9
        for (i in 0 until 9) {
            val q = vm.uiState.value.currentQuestion!!
            vm.selectChoice(q.targetLetter)
            advanceUntilIdle()
            assertTrue(vm.uiState.value.isAnswered)
            assertFalse("Should not be completed at question index $i", vm.uiState.value.showCompletionDialog)
            vm.nextQuestion()
            advanceUntilIdle()
        }

        assertEquals(9, vm.uiState.value.currentQuestionIndex)
        assertFalse(vm.uiState.value.showCompletionDialog)

        // Answer 10th question
        val lastQuestion = vm.uiState.value.currentQuestion!!
        vm.selectChoice(lastQuestion.targetLetter)
        advanceUntilIdle()
        vm.nextQuestion()
        advanceUntilIdle()

        // Now session must be completed!
        val state = vm.uiState.value
        assertTrue("Session must be completed after 10th question", state.showCompletionDialog)
        assertNotNull(state.sessionResult)
        assertEquals(SessionStatus.COMPLETED, state.sessionResult?.status)
        assertEquals(10, state.sessionResult?.totalQuestions)
        assertEquals(10, state.sessionResult?.correctAnswers)
        assertEquals(1.0f, state.sessionResult?.accuracy ?: 0f, 0.01f)

        // Verify committed in repository
        val sessionResult = state.sessionResult!!
        val completed = sessionRepository.getCompletedSessions(sessionResult.childId)
        assertEquals(1, completed.size)
        assertEquals(sessionResult.sessionId, completed[0].sessionId)
    }

    @Test
    fun earlyExitDoesNotMarkSessionAsCompleted() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        // Answer only 3 questions
        for (i in 0 until 3) {
            val q = vm.uiState.value.currentQuestion!!
            vm.selectChoice(q.targetLetter)
            advanceUntilIdle()
            vm.nextQuestion()
            advanceUntilIdle()
        }

        // Child exits early
        vm.abandonSession()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.showCompletionDialog)

        // Check repository: must NOT contain any COMPLETED session
        val completed = sessionRepository.getCompletedSessions("child_demo_guardian_default")
        assertTrue("Abandoned session must not count as completed", completed.isEmpty())
    }

    @Test
    fun noDuplicateAnswerLogForSameQuestion() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val q = vm.uiState.value.currentQuestion!!

        // First choice
        vm.selectChoice(q.targetLetter)
        advanceUntilIdle()

        // Attempting to select a different choice for the same question should be blocked
        vm.selectChoice(q.pairLetter)
        advanceUntilIdle()

        assertEquals("First selected choice must be preserved", q.targetLetter, vm.uiState.value.selectedChoice)

        val answers = sessionRepository.getAnswersForSession(vm.uiState.value.sessionId)
        assertEquals("Only one AnswerLog recorded for question 1", 1, answers.size)
        assertEquals(q.targetLetter, answers[0].chosenChar)
    }

    @Test
    fun accuracyBelow80PercentStillAllowsCompletedSession() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        // Intentionally answer 4 correct and 6 incorrect (40% accuracy)
        for (i in 0 until 10) {
            val q = vm.uiState.value.currentQuestion!!
            if (i < 4) {
                vm.selectChoice(q.targetLetter) // correct
            } else {
                vm.selectChoice(q.pairLetter)   // incorrect
            }
            advanceUntilIdle()
            vm.nextQuestion()
            advanceUntilIdle()
        }

        val state = vm.uiState.value
        assertTrue("Session must complete even with low accuracy", state.showCompletionDialog)
        val result = state.sessionResult
        assertNotNull(result)
        assertEquals(SessionStatus.COMPLETED, result?.status)
        assertEquals(4, result?.correctAnswers)
        assertEquals(6, result?.incorrectAnswers)
        assertEquals(0.40f, result?.accuracy ?: 0f, 0.01f)
    }

    @Test
    fun inGameAdaptiveDifficultyAdjustsOnConsecutiveAnswers() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.currentDifficulty)
        assertEquals(0, vm.uiState.value.consecutiveCorrect)

        // 1st correct
        val q1 = vm.uiState.value.currentQuestion!!
        vm.selectChoice(q1.targetLetter)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.currentDifficulty)
        assertEquals(1, vm.uiState.value.consecutiveCorrect)
        vm.nextQuestion()
        advanceUntilIdle()

        // 2nd correct
        val q2 = vm.uiState.value.currentQuestion!!
        vm.selectChoice(q2.targetLetter)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.currentDifficulty)
        assertEquals(2, vm.uiState.value.consecutiveCorrect)
        vm.nextQuestion()
        advanceUntilIdle()

        // 3rd correct -> Level up to 2!
        val q3 = vm.uiState.value.currentQuestion!!
        vm.selectChoice(q3.targetLetter)
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.currentDifficulty)
        assertEquals(0, vm.uiState.value.consecutiveCorrect)
        vm.nextQuestion()
        advanceUntilIdle()

        // 4th correct, 5th correct, 6th correct -> Level up to 3!
        val q4 = vm.uiState.value.currentQuestion!!
        vm.selectChoice(q4.targetLetter)
        advanceUntilIdle()
        vm.nextQuestion()
        advanceUntilIdle()

        val q5 = vm.uiState.value.currentQuestion!!
        vm.selectChoice(q5.targetLetter)
        advanceUntilIdle()
        vm.nextQuestion()
        advanceUntilIdle()

        val q6 = vm.uiState.value.currentQuestion!!
        vm.selectChoice(q6.targetLetter)
        advanceUntilIdle()
        assertEquals(3, vm.uiState.value.currentDifficulty)
        vm.nextQuestion()
        advanceUntilIdle()

        // 1st incorrect -> difficulty stays 3, consecutiveIncorrect = 1
        val q7 = vm.uiState.value.currentQuestion!!
        vm.selectChoice(q7.pairLetter) // wrong
        advanceUntilIdle()
        assertEquals(3, vm.uiState.value.currentDifficulty)
        assertEquals(1, vm.uiState.value.consecutiveIncorrect)
        vm.nextQuestion()
        advanceUntilIdle()

        // 2nd incorrect -> Level drops to 2!
        val q8 = vm.uiState.value.currentQuestion!!
        vm.selectChoice(q8.pairLetter) // wrong
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.currentDifficulty)
        assertEquals(0, vm.uiState.value.consecutiveIncorrect)
    }

    @Test
    fun adaptiveQuestionsLoadedWhenWeakTargetsExist() = runTest {
        val childId = "child_demo_guardian_default"
        val pastSessionId = "session_past_adaptive_test"

        // Seed 1 past completed session with errors on 'b'
        sessionRepository.saveSessionResult(
            com.aksa.app.domain.model.SessionResult(
                sessionId = pastSessionId,
                childId = childId,
                moduleId = "pulau_alfabet",
                activityId = "huruf_mirip",
                startedAt = 1000L,
                completedAt = 2000L,
                totalQuestions = 10,
                correctAnswers = 7,
                incorrectAnswers = 3,
                accuracy = 0.7f,
                status = SessionStatus.COMPLETED
            )
        )

        // Seed past answers: 3 attempts on 'b', 2 incorrect (errorRate = 66.7%)
        val pastAnswers = listOf(
            com.aksa.app.domain.model.AnswerLog("a1", pastSessionId, childId, "pulau_alfabet", "huruf_mirip", "q1", "b", "d", false, 500L),
            com.aksa.app.domain.model.AnswerLog("a2", pastSessionId, childId, "pulau_alfabet", "huruf_mirip", "q2", "b", "d", false, 500L),
            com.aksa.app.domain.model.AnswerLog("a3", pastSessionId, childId, "pulau_alfabet", "huruf_mirip", "q3", "b", "b", true, 500L),
            com.aksa.app.domain.model.AnswerLog("a4", pastSessionId, childId, "pulau_alfabet", "huruf_mirip", "q4", "d", "d", true, 500L),
            com.aksa.app.domain.model.AnswerLog("a5", pastSessionId, childId, "pulau_alfabet", "huruf_mirip", "q5", "p", "p", true, 500L),
            com.aksa.app.domain.model.AnswerLog("a6", pastSessionId, childId, "pulau_alfabet", "huruf_mirip", "q6", "q", "q", true, 500L)
        )
        sessionRepository.seedAnswers(pastAnswers)

        val vm = createViewModel()
        advanceUntilIdle()

        // Verify that priority letter 'b' was detected and questions were adaptively generated with 6 'b' questions (60:40 rule)!
        val state = vm.uiState.value
        assertEquals(listOf("b"), state.priorityLetters)
        val bCount = state.questions.count { it.targetLetter == "b" }
        assertEquals("Priority letter 'b' must have 6 questions (60%) allocated", 6, bCount)
        val otherCount = state.questions.count { it.targetLetter != "b" }
        assertEquals("Other letters must have 4 questions (40%) allocated", 4, otherCount)
    }

    @Test
    fun sessionChildIdIsPinnedAndAnswerLogsPersistedBeforeCompletion() = runTest {
        com.aksa.app.ui.state.AksaUserState.currentProfileId = "profile_alpha"

        val vm = createViewModel()
        advanceUntilIdle()

        // Answer questions 1 through 5
        for (i in 0 until 5) {
            val q = vm.uiState.value.currentQuestion!!
            vm.selectChoice(q.targetLetter)
            advanceUntilIdle()
            vm.nextQuestion()
            advanceUntilIdle()
        }

        // Simulate concurrent profile switch mid-session
        com.aksa.app.ui.state.AksaUserState.currentProfileId = "profile_beta"

        // Answer remaining questions 6 through 10
        for (i in 5 until 9) {
            val q = vm.uiState.value.currentQuestion!!
            vm.selectChoice(q.targetLetter)
            advanceUntilIdle()
            vm.nextQuestion()
            advanceUntilIdle()
        }
        val q10 = vm.uiState.value.currentQuestion!!
        vm.selectChoice(q10.targetLetter)
        advanceUntilIdle()
        vm.nextQuestion()
        advanceUntilIdle()

        // Verify session was committed under original pinned profile_alpha
        val state = vm.uiState.value
        assertTrue(state.showCompletionDialog)
        assertNotNull(state.sessionResult)
        assertEquals("profile_alpha", state.sessionResult?.childId)

        // Verify all 10 answer logs were saved with profile_alpha, no profile_beta leakage
        val alphaAnswers = sessionRepository.getAnswersForSession(state.sessionId)
        assertEquals(10, alphaAnswers.size)
        assertTrue(alphaAnswers.all { it.childId == "profile_alpha" })

        // Clean up
        com.aksa.app.ui.state.AksaUserState.clearSession()
    }
}
