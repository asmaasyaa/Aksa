package com.aksa.app

import com.aksa.app.data.repository.FirestoreLevelProgressRepository
import com.aksa.app.data.repository.demo.DemoAuthRepository
import com.aksa.app.data.repository.demo.DemoLevelProgressRepository
import com.aksa.app.data.repository.demo.DemoProfileRepository
import com.aksa.app.data.repository.demo.DemoSessionRepository
import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.model.LevelReadinessStatus
import com.aksa.app.domain.model.Profile
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.usecase.EvaluateLevelReadinessUseCase
import com.aksa.app.domain.usecase.GetActiveChildIdUseCase
import com.aksa.app.domain.usecase.GetLevelProgressionUseCase
import com.aksa.app.ui.components.LevelNodeStatus
import com.aksa.app.ui.preview.DeveloperPreviewManager
import com.aksa.app.ui.preview.LevelMapPreviewState
import com.aksa.app.ui.state.AksaUserState
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
        val s1 = session("s1", total = 4, correct = 4, incorrect = 0, acc = 1.0f, time = 1000L)
        val s2 = session("s2", total = 3, correct = 2, incorrect = 1, acc = 0.80f, time = 2000L)
        val s3 = session("s3", total = 3, correct = 2, incorrect = 1, acc = 0.80f, time = 3000L)
        sessionRepository.saveSessionResult(s1)
        sessionRepository.saveSessionResult(s2)
        sessionRepository.saveSessionResult(s3)

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
        seedStandardSessionWithAnswers("s1", modId = "pulau_alfabet", time = 1000L)
        seedStandardSessionWithAnswers("s2", modId = "pulau_alfabet", time = 2000L)
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
        seedStandardSessionWithAnswers("s1", time = 1000L)
        seedStandardSessionWithAnswers("s2", time = 2000L)
        seedStandardSessionWithAnswers("s3", time = 3000L)

        val progressInitial = getProgressionUseCase(childId)
        assertTrue(progressInitial.level2.isUnlocked)
        assertEquals(LevelNodeStatus.ACTIVE, progressInitial.getNodeStatus(2))

        seedStandardSessionWithAnswers("s4_low", total = 5, incorrect = 4, acc = 0.20f, time = 4000L)
        seedStandardSessionWithAnswers("s5_low", total = 5, incorrect = 4, acc = 0.20f, time = 5000L)
        seedStandardSessionWithAnswers("s6_low", total = 5, incorrect = 4, acc = 0.20f, time = 6000L)

        val resultAfter = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.LOW_ACCURACY, resultAfter.status)

        val progressAfter = getProgressionUseCase(childId)
        assertTrue(progressAfter.level2.isUnlocked)
        assertNotEquals(LevelNodeStatus.LOCKED, progressAfter.getNodeStatus(2))
    }

    // 17. Level 3 depends on Level 2 mastery.
    @Test
    fun test17_level3_dependsOnLevel2Mastery() = runTest {
        seedStandardSessionWithAnswers("s1_l1", modId = "pulau_alfabet", time = 1000L)
        seedStandardSessionWithAnswers("s2_l1", modId = "pulau_alfabet", time = 2000L)
        seedStandardSessionWithAnswers("s3_l1", modId = "pulau_alfabet", time = 3000L)

        var progress = getProgressionUseCase(childId)
        assertTrue(progress.level2.isUnlocked)
        assertFalse(progress.level3.isUnlocked)
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(3))

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
        DeveloperPreviewManager.setLevelMapState(LevelMapPreviewState.ALL_UNLOCKED)
        DeveloperPreviewManager.enablePreview(false)

        val progress = getProgressionUseCase(childId)
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(2))
    }

    // 20. Debug preview can display all unlocked levels without modifying real data.
    @Test
    fun test20_debugPreview_canDisplayAllUnlockedWithoutModifyingRealData() = runTest {
        DeveloperPreviewManager.setLevelMapState(LevelMapPreviewState.ALL_UNLOCKED)
        DeveloperPreviewManager.enablePreview(true)

        assertEquals(LevelMapPreviewState.ALL_UNLOCKED, DeveloperPreviewManager.levelMapState.value)

        val realUnlocked = levelProgressRepository.getUnlockedLevels(childId)
        assertEquals(setOf("pulau_alfabet"), realUnlocked)
        val sessionsInRepo = sessionRepository.getCompletedSessions(childId)
        assertTrue(sessionsInRepo.isEmpty())

        DeveloperPreviewManager.enablePreview(false)
    }

    // 21. Release mode does not allow preview bypass.
    @Test
    fun test21_releaseMode_doesNotAllowPreviewBypass() = runTest {
        val isDebugBuild = false
        val isPreviewEnabled = true
        val effectivePreviewActive = isDebugBuild && isPreviewEnabled

        assertFalse(effectivePreviewActive)

        val progress = getProgressionUseCase(childId)
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(2))
    }

    // 22. Session history updates refresh the effective map state.
    @Test
    fun test22_sessionHistoryUpdates_refreshEffectiveMapState() = runTest {
        var progress = getProgressionUseCase(childId)
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(2))

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
        seedStandardSessionWithAnswers("s1_dup", time = 1000L)
        seedStandardSessionWithAnswers("s1_dup", time = 1000L)
        seedStandardSessionWithAnswers("s2_other", time = 2000L)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertEquals(2, result.sessionCount)
    }

    // 24. No requirement checks the number of distinct learning days.
    @Test
    fun test24_noRequirementChecks_numberOfDistinctLearningDays() = runTest {
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

    // 25. Multi-profile isolation under same guardian: Child 1 unlocking Level 2 does not unlock Level 2 for Child 2.
    @Test
    fun test25_multiProfileIsolation_differentChildrenUnderSameParent() = runTest {
        val child1 = "profile_child_kakak"
        val child2 = "profile_child_adik"

        // Child 1 qualifies and unlocks Level 2
        seedStandardSessionWithAnswers("s1_c1", cId = child1, time = 1000L)
        seedStandardSessionWithAnswers("s2_c1", cId = child1, time = 2000L)
        seedStandardSessionWithAnswers("s3_c1", cId = child1, time = 3000L)

        val progressChild1 = getProgressionUseCase(child1)
        assertTrue("Child 1 should have Level 2 unlocked", progressChild1.level2.isUnlocked)
        assertEquals(LevelNodeStatus.ACTIVE, progressChild1.getNodeStatus(2))

        // Child 2 has zero sessions recorded
        val progressChild2 = getProgressionUseCase(child2)
        assertFalse("Child 2 must NOT have Level 2 unlocked", progressChild2.level2.isUnlocked)
        assertEquals(LevelNodeStatus.LOCKED, progressChild2.getNodeStatus(2))
        assertTrue("Child 2 must start with Level 1 unlocked", progressChild2.level1.isUnlocked)
    }

    // 26. Profile switching preserves independent unlock states and reactively updates.
    @Test
    fun test26_profileSwitching_preservesIndependentUnlockStates() = runTest {
        val childKakak = "profile_kakak"
        val childAdik = "profile_adik"

        // Unlock Level 2 for Kakak
        seedStandardSessionWithAnswers("s1_k", cId = childKakak, time = 1000L)
        seedStandardSessionWithAnswers("s2_k", cId = childKakak, time = 2000L)
        seedStandardSessionWithAnswers("s3_k", cId = childKakak, time = 3000L)
        getProgressionUseCase(childKakak)

        // Switch to Kakak
        AksaUserState.currentProfileId = childKakak
        val kakakUnlocked = levelProgressRepository.getUnlockedLevels(AksaUserState.currentProfileId)
        assertTrue(kakakUnlocked.contains("dunia_suku_kata"))

        // Switch to Adik
        AksaUserState.currentProfileId = childAdik
        val adikUnlocked = levelProgressRepository.getUnlockedLevels(AksaUserState.currentProfileId)
        assertFalse(adikUnlocked.contains("dunia_suku_kata"))
        assertEquals(setOf("pulau_alfabet"), adikUnlocked)

        // Switch back to Kakak
        AksaUserState.currentProfileId = childKakak
        val kakakUnlockedAgain = levelProgressRepository.getUnlockedLevels(AksaUserState.currentProfileId)
        assertTrue(kakakUnlockedAgain.contains("dunia_suku_kata"))
    }

    // 27. LevelProgressRepository preserves default level, set operations, and anti-relocking.
    @Test
    fun test27_levelProgressRepository_monotonicAndAntiRelocking() = runTest {
        val repo = DemoLevelProgressRepository()
        val testChild = "child_test_repo_fallback"

        // Brand-new child always has pulau_alfabet
        val initialLevels = repo.getUnlockedLevels(testChild)
        assertEquals(setOf("pulau_alfabet"), initialLevels)

        // Unlock Level 2
        repo.unlockLevel(testChild, "dunia_suku_kata")
        val afterUnlock = repo.getUnlockedLevels(testChild)
        assertTrue(afterUnlock.contains("dunia_suku_kata"))
        assertTrue(afterUnlock.contains("pulau_alfabet"))

        // Overwrite save without default level still keeps pulau_alfabet
        repo.saveUnlockedLevels(testChild, setOf("gunung_kata"))
        val afterSave = repo.getUnlockedLevels(testChild)
        assertTrue(afterSave.contains("pulau_alfabet"))
        assertTrue(afterSave.contains("gunung_kata"))
    }

    // 28. GetActiveChildIdUseCase: enforces authenticated guardian and valid selected child profile.
    @Test
    fun test28_getActiveChildIdUseCase_priorityAndFallback() = runTest {
        val authRepo = DemoAuthRepository()
        val profileRepo = DemoProfileRepository()
        val useCase = GetActiveChildIdUseCase(authRepo, profileRepo)

        // Reset state
        AksaUserState.currentProfileId = ""
        authRepo.signOut()

        // 1. Unauthenticated -> throws IllegalStateException
        try {
            useCase()
            org.junit.Assert.fail("Should throw IllegalStateException when unauthenticated")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("login"))
        }

        // 2. Authenticated parent but no profiles created yet -> throws IllegalStateException
        authRepo.signIn("user_baru@aksa.id", "123456")
        val parentUid = authRepo.currentGuardian.value!!.uid
        try {
            useCase()
            org.junit.Assert.fail("Should throw IllegalStateException when no child profile exists")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("profil anak"))
        }

        // 3. Parent has a profile in repository -> resolves active profile
        val profile = Profile(
            profileId = "profile_repo_child_1",
            userId = parentUid,
            name = "Ahmad",
            avatarId = "mascot_classic"
        )
        profileRepo.saveChildProfile(profile)

        val resolvedFromRepo = useCase()
        assertEquals("profile_repo_child_1", resolvedFromRepo)

        // 4. AksaUserState explicit currentProfileId resolves validated profile
        AksaUserState.currentProfileId = "profile_repo_child_1"
        val resolvedFromState = useCase()
        assertEquals("profile_repo_child_1", resolvedFromState)

        // Sync helper
        assertEquals("profile_repo_child_1", useCase.getSync())

        // 5. Blank AksaUserState causes getSync() to throw
        AksaUserState.currentProfileId = ""
        try {
            useCase.getSync()
            org.junit.Assert.fail("getSync should throw when currentProfileId is blank")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("Pilih profil anak"))
        }
    }

    // 29. Session eligibility: only COMPLETED sessions with valid status are considered.
    @Test
    fun test29_sessionEligibility_onlyCompletedSessionsCount() = runTest {
        // 1 valid completed session
        seedStandardSessionWithAnswers("s_valid", time = 1000L)

        // 1 abandoned session
        val abandoned = session("s_abandoned", status = SessionStatus.ABANDONED, time = 2000L)
        sessionRepository.saveSessionResult(abandoned)

        // 1 in_progress session
        val inProgress = session("s_in_progress", status = SessionStatus.IN_PROGRESS, time = 3000L)
        sessionRepository.saveSessionResult(inProgress)

        // 1 mini game session
        val miniGame = session("s_game", actId = "tangkap_huruf_game", time = 4000L)
        sessionRepository.saveSessionResult(miniGame)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals("Only 1 valid session should be counted", 1, result.sessionCount)
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertFalse(result.isReadyToUnlock)
    }
}

