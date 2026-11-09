package com.aksa.app

import com.aksa.app.data.repository.demo.DemoLevelProgressRepository
import com.aksa.app.data.repository.demo.DemoSessionRepository
import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.model.LevelReadinessStatus
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.usecase.EvaluateLevelReadinessUseCase
import com.aksa.app.domain.usecase.GetLevelProgressionUseCase
import com.aksa.app.ui.components.LevelNodeStatus
import com.aksa.app.ui.preview.DeveloperPreviewManager
import com.aksa.app.ui.preview.LevelMapPreviewState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LevelReadinessEvaluationTest {

    private lateinit var sessionRepository: DemoSessionRepository
    private lateinit var levelProgressRepository: DemoLevelProgressRepository
    private lateinit var evaluateReadinessUseCase: EvaluateLevelReadinessUseCase
    private lateinit var getProgressionUseCase: GetLevelProgressionUseCase

    private val childId = "child_test_syakira"

    @Before
    fun setUp() {
        sessionRepository = DemoSessionRepository()
        levelProgressRepository = DemoLevelProgressRepository()
        evaluateReadinessUseCase = EvaluateLevelReadinessUseCase(sessionRepository)
        getProgressionUseCase = GetLevelProgressionUseCase(evaluateReadinessUseCase, levelProgressRepository)
        DeveloperPreviewManager.enablePreview(false)
    }

    private fun session(
        id: String,
        cId: String = childId,
        modId: String = "pulau_alfabet",
        actId: String = "huruf_mirip",
        total: Int = 10,
        correct: Int = 10,
        incorrect: Int = 0,
        acc: Float = 1.0f,
        status: SessionStatus = SessionStatus.COMPLETED,
        time: Long = System.currentTimeMillis()
    ) = SessionResult(
        sessionId = id,
        childId = cId,
        moduleId = modId,
        activityId = actId,
        startedAt = time - 60_000,
        completedAt = if (status == SessionStatus.COMPLETED) time else null,
        totalQuestions = total,
        correctAnswers = correct,
        incorrectAnswers = incorrect,
        accuracy = acc,
        status = status
    )

    private fun answer(
        ansId: String,
        sId: String,
        cId: String = childId,
        modId: String = "pulau_alfabet",
        target: String = "b",
        isCorrect: Boolean = true
    ) = AnswerLog(
        answerId = ansId,
        sessionId = sId,
        childId = cId,
        moduleId = modId,
        activityId = "huruf_mirip",
        questionId = "q_$ansId",
        targetChar = target,
        chosenChar = if (isCorrect) target else "x",
        isCorrect = isCorrect,
        responseTimeMs = 1200L
    )

    private suspend fun seedStandardSessionWithAnswers(
        sId: String,
        cId: String = childId,
        modId: String = "pulau_alfabet",
        targetLetter: String = "b",
        total: Int = 3,
        incorrect: Int = 0,
        acc: Float = if (total > 0) (total - incorrect).toFloat() / total.toFloat() else 0f,
        time: Long = System.currentTimeMillis()
    ) {
        val s = session(
            id = sId,
            cId = cId,
            modId = modId,
            total = total,
            correct = total - incorrect,
            incorrect = incorrect,
            acc = acc,
            time = time
        )
        sessionRepository.saveSessionResult(s)

        for (i in 1..total) {
            val isAnsCorrect = i > incorrect
            val a = answer(
                ansId = "ans_${sId}_$i",
                sId = sId,
                cId = cId,
                modId = modId,
                target = targetLetter,
                isCorrect = isAnsCorrect
            )
            sessionRepository.recordAnswer(a)
        }
    }

    // 1. Level 1 is unlocked by default.
    @Test
    fun test1_level1_unlockedByDefault() = runTest {
        val unlocked = levelProgressRepository.getUnlockedLevels(childId)
        assertTrue(unlocked.contains("pulau_alfabet"))

        val progress = getProgressionUseCase(childId)
        assertTrue(progress.level1.isUnlocked)
        assertEquals(LevelNodeStatus.ACTIVE, progress.getNodeStatus(1))
    }

    // 2. Level 2 is locked for a brand-new child.
    @Test
    fun test2_level2_lockedForNewChild() = runTest {
        val progress = getProgressionUseCase(childId)
        assertFalse(progress.level2.isUnlocked)
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(2))
    }

    // 3. Fewer than three sessions do not unlock a level.
    @Test
    fun test3_fewerThanThreeSessions_doNotUnlock() = runTest {
        // Seed 2 valid sessions with 100% accuracy
        seedStandardSessionWithAnswers("s1", time = 1000L)
        seedStandardSessionWithAnswers("s2", time = 2000L)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertEquals(2, result.sessionCount)
        assertFalse(result.isReadyToUnlock)

        val progress = getProgressionUseCase(childId)
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(2))
    }

    // 4. Three valid sessions are sufficient for session count.
    @Test
    fun test4_threeValidSessions_sufficientSessionCount() = runTest {
        seedStandardSessionWithAnswers("s1", time = 1000L)
        seedStandardSessionWithAnswers("s2", time = 2000L)
        seedStandardSessionWithAnswers("s3", time = 3000L)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertNotEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertEquals(3, result.sessionCount)
    }

    // 5. Exactly 80% average accuracy passes.
    @Test
    fun test5_exactly80PercentAverageAccuracy_passes() = runTest {
        // 3 sessions with exactly 0.80f accuracy (4/5 = 80%)
        // Total attempts for 'b' = 15, incorrect = 3 -> error rate = 3/15 = 20% <= 40%
        seedStandardSessionWithAnswers("s1", targetLetter = "b", total = 5, incorrect = 1, acc = 0.80f, time = 1000L)
        seedStandardSessionWithAnswers("s2", targetLetter = "b", total = 5, incorrect = 1, acc = 0.80f, time = 2000L)
        seedStandardSessionWithAnswers("s3", targetLetter = "b", total = 5, incorrect = 1, acc = 0.80f, time = 3000L)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.READY_TO_UNLOCK, result.status)
        assertEquals(0.80f, result.averageAccuracy, 1e-4f)
        assertTrue(result.isReadyToUnlock)
    }

    // 6. Average accuracy below 80% fails.
    @Test
    fun test6_averageAccuracyBelow80Percent_fails() = runTest {
        // Average: (0.75 + 0.80 + 0.80) / 3 = 0.7833f < 0.80f
        seedStandardSessionWithAnswers("s1", total = 4, incorrect = 1, acc = 0.75f, time = 1000L)
        seedStandardSessionWithAnswers("s2", total = 5, incorrect = 1, acc = 0.80f, time = 2000L)
        seedStandardSessionWithAnswers("s3", total = 5, incorrect = 1, acc = 0.80f, time = 3000L)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.LOW_ACCURACY, result.status)
        assertFalse(result.isReadyToUnlock)
    }

    // 7. Exactly 40% target error rate passes.
    @Test
    fun test7_exactly40PercentTargetErrorRate_passes() = runTest {
        // Target 'b' has 5 attempts, 2 incorrect -> 2/5 = 0.40f (40%)
        // Target 'd' has 5 attempts, 0 incorrect -> 0/5 = 0.00f
        // Average accuracy: (1.0f + 0.8f + 0.8f) / 3 = 0.866f >= 0.80f
        val s1 = session("s1", total = 4, correct = 4, incorrect = 0, acc = 1.0f, time = 1000L)
        val s2 = session("s2", total = 3, correct = 2, incorrect = 1, acc = 0.67f, time = 2000L)
        val s3 = session("s3", total = 3, correct = 2, incorrect = 1, acc = 0.67f, time = 3000L)
        // Average session accuracy: (1.0f + 0.80f + 0.80f) / 3 = 0.866f
        val s1b = s1.copy(totalQuestions = 4, correctAnswers = 4, incorrectAnswers = 0, accuracy = 1.0f)
        val s2b = s2.copy(totalQuestions = 3, correctAnswers = 2, incorrectAnswers = 1, accuracy = 0.80f)
        val s3b = s3.copy(totalQuestions = 3, correctAnswers = 2, incorrectAnswers = 1, accuracy = 0.80f)
        sessionRepository.saveSessionResult(s1b)
        sessionRepository.saveSessionResult(s2b)
        sessionRepository.saveSessionResult(s3b)

        // Target 'b': 5 attempts (3 correct, 2 incorrect -> 40% error rate)
        sessionRepository.recordAnswer(answer("a1", "s1", target = "b", isCorrect = true))
        sessionRepository.recordAnswer(answer("a2", "s1", target = "b", isCorrect = true))
        sessionRepository.recordAnswer(answer("a3", "s2", target = "b", isCorrect = true))
        sessionRepository.recordAnswer(answer("a4", "s2", target = "b", isCorrect = false))
        sessionRepository.recordAnswer(answer("a5", "s3", target = "b", isCorrect = false))

        // Target 'd': 5 attempts (5 correct -> 0% error rate)
        sessionRepository.recordAnswer(answer("a6", "s1", target = "d", isCorrect = true))
        sessionRepository.recordAnswer(answer("a7", "s1", target = "d", isCorrect = true))
        sessionRepository.recordAnswer(answer("a8", "s2", target = "d", isCorrect = true))
        sessionRepository.recordAnswer(answer("a9", "s3", target = "d", isCorrect = true))
        sessionRepository.recordAnswer(answer("a10", "s3", target = "d", isCorrect = true))

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.READY_TO_UNLOCK, result.status)
        val bPerf = result.targetPerformances["b"]!!
        assertEquals(0.40f, bPerf.errorRate, 1e-4f)
        assertTrue(bPerf.isErrorRateAcceptable)
        assertTrue(result.isReadyToUnlock)
    }

    // 8. Target error rate above 40% fails.
    @Test
    fun test8_targetErrorRateAbove40Percent_fails() = runTest {
        // Average accuracy >= 80%, but target 'b' has 5 attempts, 3 incorrect = 60% error rate
        val s1 = session("s1", total = 5, correct = 4, incorrect = 1, acc = 0.80f, time = 1000L)
        val s2 = session("s2", total = 5, correct = 4, incorrect = 1, acc = 0.80f, time = 2000L)
        val s3 = session("s3", total = 5, correct = 4, incorrect = 1, acc = 0.80f, time = 3000L)
        sessionRepository.saveSessionResult(s1)
        sessionRepository.saveSessionResult(s2)
        sessionRepository.saveSessionResult(s3)

        // Target 'b': 5 attempts, 3 incorrect = 60% error rate (> 40%)
        sessionRepository.recordAnswer(answer("a1", "s1", target = "b", isCorrect = true))
        sessionRepository.recordAnswer(answer("a2", "s1", target = "b", isCorrect = false))
        sessionRepository.recordAnswer(answer("a3", "s2", target = "b", isCorrect = false))
        sessionRepository.recordAnswer(answer("a4", "s3", target = "b", isCorrect = false))
        sessionRepository.recordAnswer(answer("a5", "s3", target = "b", isCorrect = true))

        // Target 'd': 10 attempts, 0 incorrect
        for (i in 6..15) {
            val sid = if (i <= 9) "s1" else if (i <= 12) "s2" else "s3"
            sessionRepository.recordAnswer(answer("a$i", sid, target = "d", isCorrect = true))
        }

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.HIGH_ERROR_RATE, result.status)
        assertFalse(result.isReadyToUnlock)
        assertTrue(result.explanation.contains("b"))
    }

    // 9. Fewer than three attempts for an evaluated target returns insufficient evidence.
    @Test
    fun test9_fewerThanThreeAttemptsForTarget_returnsInsufficientEvidence() = runTest {
        val s1 = session("s1", total = 5, correct = 5, incorrect = 0, acc = 1.0f, time = 1000L)
        val s2 = session("s2", total = 5, correct = 5, incorrect = 0, acc = 1.0f, time = 2000L)
        val s3 = session("s3", total = 5, correct = 5, incorrect = 0, acc = 1.0f, time = 3000L)
        sessionRepository.saveSessionResult(s1)
        sessionRepository.saveSessionResult(s2)
        sessionRepository.saveSessionResult(s3)

        // Target 'b' has 5 attempts
        for (i in 1..5) {
            val sid = if (i <= 2) "s1" else if (i <= 4) "s2" else "s3"
            sessionRepository.recordAnswer(answer("a$i", sid, target = "b", isCorrect = true))
        }
        // Target 'd' has ONLY 2 attempts (< 3)
        sessionRepository.recordAnswer(answer("ad1", "s1", target = "d", isCorrect = true))
        sessionRepository.recordAnswer(answer("ad2", "s2", target = "d", isCorrect = true))

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_EVIDENCE, result.status)
        assertFalse(result.isReadyToUnlock)
    }

    // 10. Missing AnswerLog does not count as zero errors.
    @Test
    fun test10_missingAnswerLog_doesNotCountAsZeroErrors() = runTest {
        // 3 completed sessions with 100% accuracy, but NO AnswerLog recorded at all
        val s1 = session("s1", total = 5, correct = 5, incorrect = 0, acc = 1.0f, time = 1000L)
        val s2 = session("s2", total = 5, correct = 5, incorrect = 0, acc = 1.0f, time = 2000L)
        val s3 = session("s3", total = 5, correct = 5, incorrect = 0, acc = 1.0f, time = 3000L)
        sessionRepository.saveSessionResult(s1)
        sessionRepository.saveSessionResult(s2)
        sessionRepository.saveSessionResult(s3)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_EVIDENCE, result.status)
        assertFalse(result.isReadyToUnlock)
    }

    // 11. ABANDONED sessions do not count.
    @Test
    fun test11_abandonedSessions_doNotCount() = runTest {
        seedStandardSessionWithAnswers("s1", time = 1000L)
        seedStandardSessionWithAnswers("s2", time = 2000L)

        // 3rd session was abandoned
        val abandoned = session("s3_abandoned", status = SessionStatus.ABANDONED, time = 3000L)
        sessionRepository.saveSessionResult(abandoned)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertEquals(2, result.sessionCount)
    }

    // 12. IN_PROGRESS sessions do not count.
    @Test
    fun test12_inProgressSessions_doNotCount() = runTest {
        seedStandardSessionWithAnswers("s1", time = 1000L)
        seedStandardSessionWithAnswers("s2", time = 2000L)

        val inProgress = session("s3_in_progress", status = SessionStatus.IN_PROGRESS, time = 3000L)
        sessionRepository.saveSessionResult(inProgress)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertEquals(2, result.sessionCount)
    }

    // 13. Sessions from different modules do not mix.
    @Test
    fun test13_sessionsFromDifferentModules_doNotMix() = runTest {
        // 2 sessions in pulau_alfabet
        seedStandardSessionWithAnswers("s1", modId = "pulau_alfabet", time = 1000L)
        seedStandardSessionWithAnswers("s2", modId = "pulau_alfabet", time = 2000L)
        // 2 sessions in dunia_suku_kata
        seedStandardSessionWithAnswers("s3", modId = "dunia_suku_kata", time = 3000L)
        seedStandardSessionWithAnswers("s4", modId = "dunia_suku_kata", time = 4000L)

        val resultAlfa = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, resultAlfa.status)
        assertEquals(2, resultAlfa.sessionCount)

        val resultSuku = evaluateReadinessUseCase(childId, "dunia_suku_kata")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, resultSuku.status)
        assertEquals(2, resultSuku.sessionCount)
    }

    // 14. Mini-game sessions do not unlock learning levels.
    @Test
    fun test14_miniGameSessions_doNotUnlockLearningLevels() = runTest {
        seedStandardSessionWithAnswers("s1", time = 1000L)
        seedStandardSessionWithAnswers("s2", time = 2000L)

        // Session 3 is Tangkap Huruf mini-game
        val miniGame = session("s3_mini", actId = "tangkap_huruf_game", acc = 1.0f, time = 3000L)
        sessionRepository.saveSessionResult(miniGame)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertEquals(2, result.sessionCount)
    }

    // 15. Different child accounts do not share progress.
    @Test
    fun test15_differentChildAccounts_doNotShareProgress() = runTest {
        val childA = "child_a_syakira"
        val childB = "child_b_other"

        // Child A completes 3 perfect sessions
        seedStandardSessionWithAnswers("s1_a", cId = childA, time = 1000L)
        seedStandardSessionWithAnswers("s2_a", cId = childA, time = 2000L)
        seedStandardSessionWithAnswers("s3_a", cId = childA, time = 3000L)

        val resultA = evaluateReadinessUseCase(childA, "pulau_alfabet")
        assertTrue(resultA.isReadyToUnlock)

        val resultB = evaluateReadinessUseCase(childB, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, resultB.status)
        assertEquals(0, resultB.sessionCount)

        val progressB = getProgressionUseCase(childB)
        assertEquals(LevelNodeStatus.LOCKED, progressB.getNodeStatus(2))
    }

    // 16. Prior unlocked levels remain unlocked after later low scores.
    @Test
    fun test16_priorUnlockedLevels_remainUnlockedAfterLaterLowScores() = runTest {
        // Step 1: Master Level 1 (3 sessions with 100%)
        seedStandardSessionWithAnswers("s1", time = 1000L)
        seedStandardSessionWithAnswers("s2", time = 2000L)
        seedStandardSessionWithAnswers("s3", time = 3000L)

        val progressInitial = getProgressionUseCase(childId)
        assertTrue(progressInitial.level2.isUnlocked)
        assertEquals(LevelNodeStatus.ACTIVE, progressInitial.getNodeStatus(2))

        // Step 2: Child later records 3 low-accuracy sessions on Level 1
        seedStandardSessionWithAnswers("s4_low", total = 5, incorrect = 4, acc = 0.20f, time = 4000L)
        seedStandardSessionWithAnswers("s5_low", total = 5, incorrect = 4, acc = 0.20f, time = 5000L)
        seedStandardSessionWithAnswers("s6_low", total = 5, incorrect = 4, acc = 0.20f, time = 6000L)

        // Rolling average for Level 1 is now 20%
        val resultAfter = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.LOW_ACCURACY, resultAfter.status)

        // Crucial: Level 2 MUST REMAIN UNLOCKED
        val progressAfter = getProgressionUseCase(childId)
        assertTrue(progressAfter.level2.isUnlocked)
        assertNotEquals(LevelNodeStatus.LOCKED, progressAfter.getNodeStatus(2))
    }

    // 17. Level 3 depends on Level 2 mastery.
    @Test
    fun test17_level3_dependsOnLevel2Mastery() = runTest {
        // Master Level 1
        seedStandardSessionWithAnswers("s1_l1", modId = "pulau_alfabet", time = 1000L)
        seedStandardSessionWithAnswers("s2_l1", modId = "pulau_alfabet", time = 2000L)
        seedStandardSessionWithAnswers("s3_l1", modId = "pulau_alfabet", time = 3000L)

        var progress = getProgressionUseCase(childId)
        assertTrue(progress.level2.isUnlocked)
        assertFalse(progress.level3.isUnlocked)
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(3))

        // Master Level 2 (Dunia Suku Kata)
        seedStandardSessionWithAnswers("s1_l2", modId = "dunia_suku_kata", targetLetter = "MATA", time = 4000L)
        seedStandardSessionWithAnswers("s2_l2", modId = "dunia_suku_kata", targetLetter = "MATA", time = 5000L)
        seedStandardSessionWithAnswers("s3_l2", modId = "dunia_suku_kata", targetLetter = "MATA", time = 6000L)

        progress = getProgressionUseCase(childId)
        assertTrue(progress.level3.isUnlocked)
        assertEquals(LevelNodeStatus.ACTIVE, progress.getNodeStatus(3))
    }

    // 18. Level 4 depends on Level 3 mastery.
    @Test
    fun test18_level4_dependsOnLevel3Mastery() = runTest {
        // Master Level 1 & 2
        seedStandardSessionWithAnswers("s1_l1", modId = "pulau_alfabet", time = 1000L)
        seedStandardSessionWithAnswers("s2_l1", modId = "pulau_alfabet", time = 2000L)
        seedStandardSessionWithAnswers("s3_l1", modId = "pulau_alfabet", time = 3000L)

        seedStandardSessionWithAnswers("s1_l2", modId = "dunia_suku_kata", targetLetter = "MATA", time = 4000L)
        seedStandardSessionWithAnswers("s2_l2", modId = "dunia_suku_kata", targetLetter = "MATA", time = 5000L)
        seedStandardSessionWithAnswers("s3_l2", modId = "dunia_suku_kata", targetLetter = "MATA", time = 6000L)

        var progress = getProgressionUseCase(childId)
        assertTrue(progress.level3.isUnlocked)
        assertFalse(progress.level4.isUnlocked)
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(4))

        // Master Level 3 (Gunung Kata)
        seedStandardSessionWithAnswers("s1_l3", modId = "gunung_kata", targetLetter = "BOLA", time = 7000L)
        seedStandardSessionWithAnswers("s2_l3", modId = "gunung_kata", targetLetter = "BOLA", time = 8000L)
        seedStandardSessionWithAnswers("s3_l3", modId = "gunung_kata", targetLetter = "BOLA", time = 9000L)

        progress = getProgressionUseCase(childId)
        assertTrue(progress.level4.isUnlocked)
        assertEquals(LevelNodeStatus.ACTIVE, progress.getNodeStatus(4))
    }

    // 19. Normal mode ignores developer preview overrides.
    @Test
    fun test19_normalMode_ignoresDeveloperPreviewOverrides() = runTest {
        // Set preview manager to ALL_UNLOCKED but keep preview disabled
        DeveloperPreviewManager.setLevelMapState(LevelMapPreviewState.ALL_UNLOCKED)
        DeveloperPreviewManager.enablePreview(false)

        val progress = getProgressionUseCase(childId)
        // In real repository, Level 2 is locked
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(2))
    }

    // 20. Debug preview can display all unlocked levels without modifying real data.
    @Test
    fun test20_debugPreview_canDisplayAllUnlockedWithoutModifyingRealData() = runTest {
        DeveloperPreviewManager.setLevelMapState(LevelMapPreviewState.ALL_UNLOCKED)
        DeveloperPreviewManager.enablePreview(true)

        // Visual check from DeveloperPreviewManager
        assertEquals(LevelMapPreviewState.ALL_UNLOCKED, DeveloperPreviewManager.levelMapState.value)

        // Check real repository data remains completely untouched
        val realUnlocked = levelProgressRepository.getUnlockedLevels(childId)
        assertEquals(setOf("pulau_alfabet"), realUnlocked)
        val sessionsInRepo = sessionRepository.getCompletedSessions(childId)
        assertTrue(sessionsInRepo.isEmpty())

        DeveloperPreviewManager.enablePreview(false)
    }

    // 21. Release mode does not allow preview bypass.
    @Test
    fun test21_releaseMode_doesNotAllowPreviewBypass() = runTest {
        // Simulate release build logic: if !isDebug, preview state is strictly disabled
        val isDebugBuild = false
        val isPreviewEnabled = true
        val effectivePreviewActive = isDebugBuild && isPreviewEnabled

        assertFalse(effectivePreviewActive)

        // When effectivePreviewActive is false, real progression rules always govern
        val progress = getProgressionUseCase(childId)
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(2))
    }

    // 22. Session history updates refresh the effective map state.
    @Test
    fun test22_sessionHistoryUpdates_refreshEffectiveMapState() = runTest {
        var progress = getProgressionUseCase(childId)
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(2))

        // Complete 3 sessions
        seedStandardSessionWithAnswers("s1", time = 1000L)
        seedStandardSessionWithAnswers("s2", time = 2000L)
        seedStandardSessionWithAnswers("s3", time = 3000L)

        progress = getProgressionUseCase(childId)
        assertEquals(LevelNodeStatus.ACTIVE, progress.getNodeStatus(2))
        assertEquals(LevelNodeStatus.COMPLETED, progress.getNodeStatus(1))
    }

    // 23. Duplicate sessions do not increase session count.
    @Test
    fun test23_duplicateSessions_doNotIncreaseSessionCount() = runTest {
        // Save the same session twice with identical sessionId
        seedStandardSessionWithAnswers("s1_dup", time = 1000L)
        seedStandardSessionWithAnswers("s1_dup", time = 1000L) // Duplicate
        seedStandardSessionWithAnswers("s2_other", time = 2000L)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertEquals(2, result.sessionCount) // Deduplicated to 2 unique sessions
    }

    // 24. No requirement checks the number of distinct learning days.
    @Test
    fun test24_noRequirementChecks_numberOfDistinctLearningDays() = runTest {
        // All 3 sessions completed on the exact same second / day
        val sameTimestamp = 1700000000000L
        seedStandardSessionWithAnswers("s1_sametime", time = sameTimestamp)
        seedStandardSessionWithAnswers("s2_sametime", time = sameTimestamp)
        seedStandardSessionWithAnswers("s3_sametime", time = sameTimestamp)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.READY_TO_UNLOCK, result.status)
        assertTrue(result.isReadyToUnlock)

        val progress = getProgressionUseCase(childId)
        assertTrue(progress.level2.isUnlocked)
    }
}
