package com.aksa.app

import com.aksa.app.data.repository.demo.DemoAchievementRepository
import com.aksa.app.data.repository.demo.DemoAuthRepository
import com.aksa.app.data.repository.demo.DemoProfileRepository
import com.aksa.app.data.repository.demo.DemoSessionRepository
import com.aksa.app.domain.model.SessionStatus
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
            questionGenerator = GenerateHurufMiripQuestionsUseCase(Random(77))
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
        val completed = sessionRepository.getCompletedSessions(state.sessionResult!!.childId)
        assertEquals(1, completed.size)
        assertEquals(state.sessionResult!!.sessionId, completed[0].sessionId)
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
}
