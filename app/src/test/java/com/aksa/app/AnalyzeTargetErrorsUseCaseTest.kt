package com.aksa.app

import com.aksa.app.data.repository.demo.DemoSessionRepository
import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.usecase.AnalyzeTargetErrorsUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnalyzeTargetErrorsUseCaseTest {

    private lateinit var sessionRepository: DemoSessionRepository
    private lateinit var analyzeTargetErrorsUseCase: AnalyzeTargetErrorsUseCase

    @Before
    fun setUp() {
        sessionRepository = DemoSessionRepository()
        analyzeTargetErrorsUseCase = AnalyzeTargetErrorsUseCase(sessionRepository)
    }

    private fun createCompletedSession(
        sessionId: String,
        childId: String,
        moduleId: String = "pulau_alfabet",
        activityId: String = "huruf_mirip"
    ): SessionResult {
        return SessionResult(
            sessionId = sessionId,
            childId = childId,
            moduleId = moduleId,
            activityId = activityId,
            startedAt = 1000L,
            completedAt = 2000L,
            totalQuestions = 10,
            correctAnswers = 8,
            incorrectAnswers = 2,
            accuracy = 0.8f,
            status = SessionStatus.COMPLETED
        )
    }

    private fun createAnswer(
        answerId: String,
        sessionId: String,
        childId: String,
        targetChar: String,
        isCorrect: Boolean,
        moduleId: String = "pulau_alfabet",
        activityId: String = "huruf_mirip"
    ): AnswerLog {
        return AnswerLog(
            answerId = answerId,
            sessionId = sessionId,
            childId = childId,
            moduleId = moduleId,
            activityId = activityId,
            questionId = "q_$answerId",
            targetChar = targetChar,
            chosenChar = if (isCorrect) targetChar else "x",
            isCorrect = isCorrect,
            responseTimeMs = 500L,
            timestamp = System.currentTimeMillis()
        )
    }

    @Test
    fun emptyHistoryReturnsEmptyReport() = runTest {
        val report = analyzeTargetErrorsUseCase("child_empty", "pulau_alfabet")
        assertEquals("child_empty", report.childId)
        assertEquals("pulau_alfabet", report.moduleId)
        assertTrue(report.weakTargets.isEmpty())
        assertTrue(report.targetMetrics.isEmpty())
        assertEquals(0, report.totalSessionsAnalyzed)
        assertEquals(0, report.totalQuestionsAnalyzed)
    }

    @Test
    fun blankChildIdReturnsEmptyReport() = runTest {
        val report = analyzeTargetErrorsUseCase("", "pulau_alfabet")
        assertTrue(report.weakTargets.isEmpty())
        assertEquals(0, report.totalSessionsAnalyzed)
    }

    @Test
    fun singleWeakTargetIdentifiedAndRanked() = runTest {
        val childId = "child_single_weak"
        val sessionId = "session_sw_1"

        sessionRepository.saveSessionResult(createCompletedSession(sessionId, childId))

        val answers = listOf(
            // Target b: 3 attempts, 2 incorrect -> errorRate = 66.7% (weak)
            createAnswer("a1", sessionId, childId, "b", isCorrect = false),
            createAnswer("a2", sessionId, childId, "b", isCorrect = false),
            createAnswer("a3", sessionId, childId, "b", isCorrect = true),
            // Target d: 3 attempts, 0 incorrect -> errorRate = 0%
            createAnswer("a4", sessionId, childId, "d", isCorrect = true),
            createAnswer("a5", sessionId, childId, "d", isCorrect = true),
            createAnswer("a6", sessionId, childId, "d", isCorrect = true),
            // Target p: 2 attempts, 0 incorrect
            createAnswer("a7", sessionId, childId, "p", isCorrect = true),
            createAnswer("a8", sessionId, childId, "p", isCorrect = true),
            // Target q: 2 attempts, 0 incorrect
            createAnswer("a9", sessionId, childId, "q", isCorrect = true),
            createAnswer("a10", sessionId, childId, "q", isCorrect = true)
        )
        sessionRepository.seedAnswers(answers)

        val report = analyzeTargetErrorsUseCase(childId, "pulau_alfabet")
        assertEquals(1, report.totalSessionsAnalyzed)
        assertEquals(10, report.totalQuestionsAnalyzed)
        assertEquals(listOf("b"), report.weakTargets)

        val bMetric = report.targetMetrics["b"]!!
        assertEquals(3, bMetric.totalAttempts)
        assertEquals(2, bMetric.incorrectAttempts)
        assertEquals(1, bMetric.correctAttempts)
        assertEquals(2f / 3f, bMetric.errorRate, 0.01f)
        assertEquals(1f / 3f, bMetric.accuracy, 0.01f)
        assertTrue(bMetric.isWeakTarget)

        val dMetric = report.targetMetrics["d"]!!
        assertFalse(dMetric.isWeakTarget)
        assertEquals(0f, dMetric.errorRate, 0.01f)
    }

    @Test
    fun multipleWeakTargetsSortedByErrorRate() = runTest {
        val childId = "child_multi_weak"
        val sessionId = "session_mw_1"

        sessionRepository.saveSessionResult(createCompletedSession(sessionId, childId))

        val answers = listOf(
            // Target b: 3 attempts, 1 incorrect -> errorRate = 33.3%
            createAnswer("a1", sessionId, childId, "b", isCorrect = false),
            createAnswer("a2", sessionId, childId, "b", isCorrect = true),
            createAnswer("a3", sessionId, childId, "b", isCorrect = true),
            // Target d: 3 attempts, 2 incorrect -> errorRate = 66.7%
            createAnswer("a4", sessionId, childId, "d", isCorrect = false),
            createAnswer("a5", sessionId, childId, "d", isCorrect = false),
            createAnswer("a6", sessionId, childId, "d", isCorrect = true),
            // Target p: 2 attempts, 0 incorrect -> 0%
            createAnswer("a7", sessionId, childId, "p", isCorrect = true),
            createAnswer("a8", sessionId, childId, "p", isCorrect = true),
            // Target q: 2 attempts, 0 incorrect -> 0%
            createAnswer("a9", sessionId, childId, "q", isCorrect = true),
            createAnswer("a10", sessionId, childId, "q", isCorrect = true)
        )
        sessionRepository.seedAnswers(answers)

        val report = analyzeTargetErrorsUseCase(childId, "pulau_alfabet")
        assertEquals(listOf("d", "b"), report.weakTargets)
        assertTrue(report.targetMetrics["d"]!!.errorRate > report.targetMetrics["b"]!!.errorRate)
    }

    @Test
    fun profileIsolationEnsuresIndependentErrorProfiles() = runTest {
        val childA = "child_profile_A"
        val childB = "child_profile_B"

        val sessionA = "session_A"
        val sessionB = "session_B"

        sessionRepository.saveSessionResult(createCompletedSession(sessionA, childA))
        sessionRepository.saveSessionResult(createCompletedSession(sessionB, childB))

        val answersA = listOf(
            createAnswer("a1", sessionA, childA, "p", isCorrect = false),
            createAnswer("a2", sessionA, childA, "p", isCorrect = false),
            createAnswer("a3", sessionA, childA, "q", isCorrect = true)
        )
        val answersB = listOf(
            createAnswer("b1", sessionB, childB, "b", isCorrect = false),
            createAnswer("b2", sessionB, childB, "b", isCorrect = false),
            createAnswer("b3", sessionB, childB, "d", isCorrect = true)
        )

        sessionRepository.seedAnswers(answersA + answersB)

        val reportA = analyzeTargetErrorsUseCase(childA, "pulau_alfabet")
        val reportB = analyzeTargetErrorsUseCase(childB, "pulau_alfabet")

        assertEquals(listOf("p"), reportA.weakTargets)
        assertEquals(listOf("b"), reportB.weakTargets)
    }

    @Test
    fun abandonedSessionsAreExcludedFromAnalytics() = runTest {
        val childId = "child_abandoned"
        val abandonedSessionId = "session_abandoned_1"

        // Save abandoned session result
        sessionRepository.saveSessionResult(
            SessionResult(
                sessionId = abandonedSessionId,
                childId = childId,
                moduleId = "pulau_alfabet",
                activityId = "huruf_mirip",
                startedAt = 1000L,
                completedAt = null,
                totalQuestions = 10,
                correctAnswers = 0,
                incorrectAnswers = 2,
                accuracy = 0f,
                status = SessionStatus.ABANDONED
            )
        )

        // Answers in abandoned session
        val answers = listOf(
            createAnswer("ab1", abandonedSessionId, childId, "b", isCorrect = false),
            createAnswer("ab2", abandonedSessionId, childId, "b", isCorrect = false)
        )
        sessionRepository.seedAnswers(answers)

        val report = analyzeTargetErrorsUseCase(childId, "pulau_alfabet")
        assertTrue("Abandoned sessions should not contribute to weak target reports", report.weakTargets.isEmpty())
        assertEquals(0, report.totalSessionsAnalyzed)
    }
}

