package com.aksa.app

import com.aksa.app.data.repository.demo.DemoAuthRepository
import com.aksa.app.data.repository.demo.DemoLevelProgressRepository
import com.aksa.app.data.repository.demo.DemoProfileRepository
import com.aksa.app.data.repository.demo.DemoSessionRepository
import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.model.LevelReadinessStatus
import com.aksa.app.domain.model.Profile
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.repository.SessionRepository
import com.aksa.app.domain.usecase.EvaluateLevelReadinessUseCase
import com.aksa.app.domain.usecase.GetActiveChildIdUseCase
import com.aksa.app.domain.usecase.GetLevelProgressionUseCase
import com.aksa.app.ui.components.LevelNodeStatus
import com.aksa.app.ui.state.AksaUserState
import com.aksa.app.ui.viewmodel.LevelMapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class LevelReadinessEvaluationTest {

    private val zoneId = ZoneId.of("Asia/Jakarta")
    private val baseDate = LocalDate.of(2026, 11, 21)
    private val fixedClock = Clock.fixed(baseDate.atStartOfDay(zoneId).toInstant(), zoneId)

    private lateinit var sessionRepository: DemoSessionRepository
    private lateinit var levelProgressRepository: DemoLevelProgressRepository
    private lateinit var evaluateReadinessUseCase: EvaluateLevelReadinessUseCase
    private lateinit var getProgressionUseCase: GetLevelProgressionUseCase

    private val childId = "child_test_syakira"

    @Before
    fun setUp() {
        sessionRepository = DemoSessionRepository()
        levelProgressRepository = DemoLevelProgressRepository()
        evaluateReadinessUseCase = EvaluateLevelReadinessUseCase(sessionRepository, fixedClock, zoneId)
        getProgressionUseCase = GetLevelProgressionUseCase(evaluateReadinessUseCase, levelProgressRepository)
    }

    private fun timestampForDay(daysAgo: Long, hour: Int = 10, minute: Int = 0): Long {
        return baseDate.minusDays(daysAgo).atTime(hour, minute).atZone(zoneId).toInstant().toEpochMilli()
    }

    private fun defaultTimestamp(id: String): Long {
        val sessionNum = Regex("""s(\d+)""").find(id)?.groupValues?.get(1)?.toIntOrNull() ?: 1
        return when (sessionNum) {
            1 -> timestampForDay(1, 10) // Yesterday
            2 -> timestampForDay(0, 10) // Today morning
            3 -> timestampForDay(0, 14) // Today afternoon
            4 -> timestampForDay(0, 16)
            5 -> timestampForDay(0, 17)
            6 -> timestampForDay(0, 18)
            else -> timestampForDay(0, 12)
        }
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
        time: Long = defaultTimestamp(id)
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
        time: Long = defaultTimestamp(sId)
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
        seedStandardSessionWithAnswers("s1")
        seedStandardSessionWithAnswers("s2")

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertEquals(2, result.sessionCount)
        assertFalse(result.isReadyToUnlock)

        val progress = getProgressionUseCase(childId)
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(2))
    }

    // 4. Three valid sessions spanning 2 distinct days are sufficient for unlock readiness.
    @Test
    fun test4_threeValidSessions_sufficientSessionCount() = runTest {
        seedStandardSessionWithAnswers("s1")
        seedStandardSessionWithAnswers("s2")
        seedStandardSessionWithAnswers("s3")

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertNotEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertEquals(3, result.sessionCount)
        assertEquals(2, result.distinctDaysInLast7Days)
    }

    // 5. Exactly 80% average accuracy passes.
    @Test
    fun test5_exactly80PercentAverageAccuracy_passes() = runTest {
        seedStandardSessionWithAnswers("s1", targetLetter = "b", total = 5, incorrect = 1, acc = 0.80f)
        seedStandardSessionWithAnswers("s2", targetLetter = "b", total = 5, incorrect = 1, acc = 0.80f)
        seedStandardSessionWithAnswers("s3", targetLetter = "b", total = 5, incorrect = 1, acc = 0.80f)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.READY_TO_UNLOCK, result.status)
        assertEquals(0.80f, result.averageAccuracy, 1e-4f)
        assertTrue(result.isReadyToUnlock)
    }

    // 6. Average accuracy below 80% fails.
    @Test
    fun test6_averageAccuracyBelow80Percent_fails() = runTest {
        seedStandardSessionWithAnswers("s1", total = 4, incorrect = 1, acc = 0.75f)
        seedStandardSessionWithAnswers("s2", total = 5, incorrect = 1, acc = 0.80f)
        seedStandardSessionWithAnswers("s3", total = 5, incorrect = 1, acc = 0.80f)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.LOW_ACCURACY, result.status)
        assertFalse(result.isReadyToUnlock)
    }

    // 7. Exactly 40% target error rate passes.
    @Test
    fun test7_exactly40PercentTargetErrorRate_passes() = runTest {
        val s1 = session("s1", total = 4, correct = 4, incorrect = 0, acc = 1.0f)
        val s2 = session("s2", total = 3, correct = 2, incorrect = 1, acc = 0.80f)
        val s3 = session("s3", total = 3, correct = 2, incorrect = 1, acc = 0.80f)
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
        val s1 = session("s1", total = 5, correct = 4, incorrect = 1, acc = 0.80f)
        val s2 = session("s2", total = 5, correct = 4, incorrect = 1, acc = 0.80f)
        val s3 = session("s3", total = 5, correct = 4, incorrect = 1, acc = 0.80f)
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
        val s1 = session("s1", total = 5, correct = 5, incorrect = 0, acc = 1.0f)
        val s2 = session("s2", total = 5, correct = 5, incorrect = 0, acc = 1.0f)
        val s3 = session("s3", total = 5, correct = 5, incorrect = 0, acc = 1.0f)
        sessionRepository.saveSessionResult(s1)
        sessionRepository.saveSessionResult(s2)
        sessionRepository.saveSessionResult(s3)

        for (i in 1..5) {
            val sid = if (i <= 2) "s1" else if (i <= 4) "s2" else "s3"
            sessionRepository.recordAnswer(answer("a$i", sid, target = "b", isCorrect = true))
        }
        sessionRepository.recordAnswer(answer("ad1", "s1", target = "d", isCorrect = true))
        sessionRepository.recordAnswer(answer("ad2", "s2", target = "d", isCorrect = true))

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_EVIDENCE, result.status)
        assertFalse(result.isReadyToUnlock)
    }

    // 10. Missing AnswerLog does not count as zero errors.
    @Test
    fun test10_missingAnswerLog_doesNotCountAsZeroErrors() = runTest {
        val s1 = session("s1", total = 5, correct = 5, incorrect = 0, acc = 1.0f)
        val s2 = session("s2", total = 5, correct = 5, incorrect = 0, acc = 1.0f)
        val s3 = session("s3", total = 5, correct = 5, incorrect = 0, acc = 1.0f)
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
        seedStandardSessionWithAnswers("s1")
        seedStandardSessionWithAnswers("s2")

        val abandoned = session("s3_abandoned", status = SessionStatus.ABANDONED)
        sessionRepository.saveSessionResult(abandoned)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertEquals(2, result.sessionCount)
    }

    // 12. IN_PROGRESS sessions do not count.
    @Test
    fun test12_inProgressSessions_doNotCount() = runTest {
        seedStandardSessionWithAnswers("s1")
        seedStandardSessionWithAnswers("s2")

        val inProgress = session("s3_in_progress", status = SessionStatus.IN_PROGRESS)
        sessionRepository.saveSessionResult(inProgress)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertEquals(2, result.sessionCount)
    }

    // 13. Sessions from different modules do not mix.
    @Test
    fun test13_sessionsFromDifferentModules_doNotMix() = runTest {
        seedStandardSessionWithAnswers("s1", modId = "pulau_alfabet")
        seedStandardSessionWithAnswers("s2", modId = "pulau_alfabet")
        seedStandardSessionWithAnswers("s3", modId = "dunia_suku_kata")
        seedStandardSessionWithAnswers("s4", modId = "dunia_suku_kata")

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
        seedStandardSessionWithAnswers("s1")
        seedStandardSessionWithAnswers("s2")

        val miniGame = session("s3_mini", actId = "tangkap_huruf_game", acc = 1.0f)
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

        seedStandardSessionWithAnswers("s1_a", cId = childA)
        seedStandardSessionWithAnswers("s2_a", cId = childA)
        seedStandardSessionWithAnswers("s3_a", cId = childA)

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
        seedStandardSessionWithAnswers("s1")
        seedStandardSessionWithAnswers("s2")
        seedStandardSessionWithAnswers("s3")

        val progressInitial = getProgressionUseCase(childId)
        assertTrue(progressInitial.level2.isUnlocked)
        assertEquals(LevelNodeStatus.ACTIVE, progressInitial.getNodeStatus(2))

        seedStandardSessionWithAnswers("s4_low", total = 5, incorrect = 4, acc = 0.20f)
        seedStandardSessionWithAnswers("s5_low", total = 5, incorrect = 4, acc = 0.20f)
        seedStandardSessionWithAnswers("s6_low", total = 5, incorrect = 4, acc = 0.20f)

        val resultAfter = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.LOW_ACCURACY, resultAfter.status)

        val progressAfter = getProgressionUseCase(childId)
        assertTrue(progressAfter.level2.isUnlocked)
        assertNotEquals(LevelNodeStatus.LOCKED, progressAfter.getNodeStatus(2))
    }

    // 17. Level 3 depends on Level 2 mastery.
    @Test
    fun test17_level3_dependsOnLevel2Mastery() = runTest {
        seedStandardSessionWithAnswers("s1_l1", modId = "pulau_alfabet")
        seedStandardSessionWithAnswers("s2_l1", modId = "pulau_alfabet")
        seedStandardSessionWithAnswers("s3_l1", modId = "pulau_alfabet")

        var progress = getProgressionUseCase(childId)
        assertTrue(progress.level2.isUnlocked)
        assertFalse(progress.level3.isUnlocked)
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(3))

        seedStandardSessionWithAnswers("s1_l2", modId = "dunia_suku_kata", targetLetter = "MATA")
        seedStandardSessionWithAnswers("s2_l2", modId = "dunia_suku_kata", targetLetter = "MATA")
        seedStandardSessionWithAnswers("s3_l2", modId = "dunia_suku_kata", targetLetter = "MATA")

        progress = getProgressionUseCase(childId)
        assertTrue(progress.level3.isUnlocked)
        assertEquals(LevelNodeStatus.ACTIVE, progress.getNodeStatus(3))
    }

    // 18. Level 4 depends on Level 3 mastery.
    @Test
    fun test18_level4_dependsOnLevel3Mastery() = runTest {
        seedStandardSessionWithAnswers("s1_l1", modId = "pulau_alfabet")
        seedStandardSessionWithAnswers("s2_l1", modId = "pulau_alfabet")
        seedStandardSessionWithAnswers("s3_l1", modId = "pulau_alfabet")

        seedStandardSessionWithAnswers("s1_l2", modId = "dunia_suku_kata", targetLetter = "MATA")
        seedStandardSessionWithAnswers("s2_l2", modId = "dunia_suku_kata", targetLetter = "MATA")
        seedStandardSessionWithAnswers("s3_l2", modId = "dunia_suku_kata", targetLetter = "MATA")

        var progress = getProgressionUseCase(childId)
        assertTrue(progress.level3.isUnlocked)
        assertFalse(progress.level4.isUnlocked)
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(4))

        seedStandardSessionWithAnswers("s1_l3", modId = "gunung_kata", targetLetter = "BOLA")
        seedStandardSessionWithAnswers("s2_l3", modId = "gunung_kata", targetLetter = "BOLA")
        seedStandardSessionWithAnswers("s3_l3", modId = "gunung_kata", targetLetter = "BOLA")

        progress = getProgressionUseCase(childId)
        assertTrue(progress.level4.isUnlocked)
        assertEquals(LevelNodeStatus.ACTIVE, progress.getNodeStatus(4))
    }

    // 19. Progression is strictly driven by real session evaluation and level progress repository.
    @Test
    fun test19_progressionIsStrictlyDrivenByRealSessionEvaluation() = runTest {
        val progress = getProgressionUseCase(childId)
        assertEquals(LevelNodeStatus.ACTIVE, progress.getNodeStatus(1))
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(2))
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(3))
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(4))
    }

    // 22. Session history updates refresh the effective map state.
    @Test
    fun test22_sessionHistoryUpdates_refreshEffectiveMapState() = runTest {
        var progress = getProgressionUseCase(childId)
        assertEquals(LevelNodeStatus.LOCKED, progress.getNodeStatus(2))

        seedStandardSessionWithAnswers("s1")
        seedStandardSessionWithAnswers("s2")
        seedStandardSessionWithAnswers("s3")

        progress = getProgressionUseCase(childId)
        assertEquals(LevelNodeStatus.ACTIVE, progress.getNodeStatus(2))
        assertEquals(LevelNodeStatus.COMPLETED, progress.getNodeStatus(1))
    }

    // 23. Duplicate sessions do not increase session count.
    @Test
    fun test23_duplicateSessions_doNotIncreaseSessionCount() = runTest {
        seedStandardSessionWithAnswers("s1_dup")
        seedStandardSessionWithAnswers("s1_dup")
        seedStandardSessionWithAnswers("s2_other")

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertEquals(2, result.sessionCount)
    }

    // 24. Practicing three valid mastery sessions on the SAME single day MUST unlock the next level (no 2-day rule).
    @Test
    fun test24_sameDayThreeValidSessions_unlocksNextLevel() = runTest {
        val sameDayTime = timestampForDay(0, 10)
        seedStandardSessionWithAnswers("s1_same", time = sameDayTime)
        seedStandardSessionWithAnswers("s2_same", time = sameDayTime + 60_000)
        seedStandardSessionWithAnswers("s3_same", time = sameDayTime + 120_000)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.READY_TO_UNLOCK, result.status)
        assertEquals(3, result.sessionCount)
        assertEquals(1, result.distinctDaysInLast7Days)
        assertTrue(result.isReadyToUnlock)

        val progress = getProgressionUseCase(childId)
        assertTrue(progress.level2.isUnlocked)
        assertEquals(LevelNodeStatus.ACTIVE, progress.getNodeStatus(2))
        assertEquals(LevelNodeStatus.COMPLETED, progress.getNodeStatus(1))
    }

    // 24B. Practicing across 2 distinct days within the last 7 days passes.
    @Test
    fun test24b_twoDistinctDaysWithinLastSevenDays_passes() = runTest {
        seedStandardSessionWithAnswers("s1_day1", time = timestampForDay(1, 10))
        seedStandardSessionWithAnswers("s2_day2", time = timestampForDay(0, 10))
        seedStandardSessionWithAnswers("s3_day2", time = timestampForDay(0, 14))

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.READY_TO_UNLOCK, result.status)
        assertEquals(3, result.sessionCount)
        assertEquals(2, result.distinctDaysInLast7Days)
        assertTrue(result.isReadyToUnlock)

        val progress = getProgressionUseCase(childId)
        assertTrue(progress.level2.isUnlocked)
    }

    // 24C. Sessions older than 7 days still count towards mastery unlock (calendar days never block F2).
    @Test
    fun test24c_sessionsOlderThanSevenDays_stillCountTowardsMasteryUnlock() = runTest {
        // 2 sessions from 8 days ago
        seedStandardSessionWithAnswers("s1_old", time = timestampForDay(8, 10))
        seedStandardSessionWithAnswers("s2_old", time = timestampForDay(8, 14))
        // 1 session today
        seedStandardSessionWithAnswers("s3_new", time = timestampForDay(0, 10))

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.READY_TO_UNLOCK, result.status)
        assertEquals(3, result.sessionCount)
        assertEquals(1, result.distinctDaysInLast7Days)
        assertTrue(result.isReadyToUnlock)
        assertFalse(LevelReadinessStatus.entries.any { it.name == "INSUFFICIENT_PRACTICE_DAYS" })
    }

    // 24D. Exact 7th day boundary (session on today - 6 days counts as valid practice day).
    @Test
    fun test24d_exactSevenDayBoundary_countsAsValidPracticeDay() = runTest {
        // Session exactly 6 days ago (7th calendar day of window [today-6 .. today])
        seedStandardSessionWithAnswers("s1_edge", time = timestampForDay(6, 10))
        seedStandardSessionWithAnswers("s2_today", time = timestampForDay(0, 10))
        seedStandardSessionWithAnswers("s3_today", time = timestampForDay(0, 14))

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.READY_TO_UNLOCK, result.status)
        assertEquals(3, result.sessionCount)
        assertEquals(2, result.distinctDaysInLast7Days)
        assertTrue(result.isReadyToUnlock)
    }

    // 24E. Abandoned sessions do not count towards practice days.
    @Test
    fun test24e_abandonedSessions_doNotCountTowardsPracticeDays() = runTest {
        seedStandardSessionWithAnswers("s1_c", time = timestampForDay(1, 10))
        val abandoned = session("s2_ab", status = SessionStatus.ABANDONED, time = timestampForDay(0, 10))
        sessionRepository.saveSessionResult(abandoned)
        seedStandardSessionWithAnswers("s3_c", time = timestampForDay(0, 14))

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertEquals(2, result.sessionCount)
    }

    // 24F. Multi-profile isolation: practice days are strictly isolated per child profile.
    @Test
    fun test24f_multiProfileIsolation_practiceDaysNotShared() = runTest {
        val childKakak = "profile_kakak"
        val childAdik = "profile_adik"

        seedStandardSessionWithAnswers("s1_k", cId = childKakak, time = timestampForDay(1, 10))
        seedStandardSessionWithAnswers("s2_k", cId = childKakak, time = timestampForDay(0, 10))
        seedStandardSessionWithAnswers("s3_k", cId = childKakak, time = timestampForDay(0, 14))

        val resultKakak = evaluateReadinessUseCase(childKakak, "pulau_alfabet")
        assertTrue(resultKakak.isReadyToUnlock)
        assertEquals(2, resultKakak.distinctDaysInLast7Days)

        seedStandardSessionWithAnswers("s1_a", cId = childAdik, time = timestampForDay(0, 10))

        val resultAdik = evaluateReadinessUseCase(childAdik, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, resultAdik.status)
        assertEquals(1, resultAdik.sessionCount)
        assertEquals(1, resultAdik.distinctDaysInLast7Days)
    }

    // 25. Multi-profile isolation under same guardian: Child 1 unlocking Level 2 does not unlock Level 2 for Child 2.
    @Test
    fun test25_multiProfileIsolation_differentChildrenUnderSameParent() = runTest {
        val child1 = "profile_child_kakak"
        val child2 = "profile_child_adik"

        seedStandardSessionWithAnswers("s1_c1", cId = child1)
        seedStandardSessionWithAnswers("s2_c1", cId = child1)
        seedStandardSessionWithAnswers("s3_c1", cId = child1)

        val progressChild1 = getProgressionUseCase(child1)
        assertTrue("Child 1 should have Level 2 unlocked", progressChild1.level2.isUnlocked)
        assertEquals(LevelNodeStatus.ACTIVE, progressChild1.getNodeStatus(2))

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

        seedStandardSessionWithAnswers("s1_k", cId = childKakak)
        seedStandardSessionWithAnswers("s2_k", cId = childKakak)
        seedStandardSessionWithAnswers("s3_k", cId = childKakak)
        getProgressionUseCase(childKakak)

        AksaUserState.currentProfileId = childKakak
        val kakakUnlocked = levelProgressRepository.getUnlockedLevels(AksaUserState.currentProfileId)
        assertTrue(kakakUnlocked.contains("dunia_suku_kata"))

        AksaUserState.currentProfileId = childAdik
        val adikUnlocked = levelProgressRepository.getUnlockedLevels(AksaUserState.currentProfileId)
        assertFalse(adikUnlocked.contains("dunia_suku_kata"))
        assertEquals(setOf("pulau_alfabet"), adikUnlocked)

        AksaUserState.currentProfileId = childKakak
        val kakakUnlockedAgain = levelProgressRepository.getUnlockedLevels(AksaUserState.currentProfileId)
        assertTrue(kakakUnlockedAgain.contains("dunia_suku_kata"))
    }

    // 27. LevelProgressRepository preserves default level, set operations, and anti-relocking.
    @Test
    fun test27_levelProgressRepository_monotonicAndAntiRelocking() = runTest {
        val repo = DemoLevelProgressRepository()
        val testChild = "child_test_repo_fallback"

        val initialLevels = repo.getUnlockedLevels(testChild)
        assertEquals(setOf("pulau_alfabet"), initialLevels)

        repo.unlockLevel(testChild, "dunia_suku_kata")
        val afterUnlock = repo.getUnlockedLevels(testChild)
        assertTrue(afterUnlock.contains("dunia_suku_kata"))
        assertTrue(afterUnlock.contains("pulau_alfabet"))

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

        AksaUserState.currentProfileId = ""
        authRepo.signOut()

        try {
            useCase()
            org.junit.Assert.fail("Should throw IllegalStateException when unauthenticated")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("login"))
        }

        authRepo.signIn("user_baru@aksa.id", "123456")
        val parentUid = authRepo.currentGuardian.value!!.uid
        try {
            useCase()
            org.junit.Assert.fail("Should throw IllegalStateException when no child profile exists")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("profil anak"))
        }

        val profile = Profile(
            profileId = "profile_repo_child_1",
            userId = parentUid,
            name = "Ahmad",
            avatarId = "mascot_classic"
        )
        profileRepo.saveChildProfile(profile)

        val resolvedFromRepo = useCase()
        assertEquals("profile_repo_child_1", resolvedFromRepo)

        AksaUserState.currentProfileId = "profile_repo_child_1"
        val resolvedFromState = useCase()
        assertEquals("profile_repo_child_1", resolvedFromState)

        assertEquals("profile_repo_child_1", useCase.getSync())

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
        seedStandardSessionWithAnswers("s_valid")

        val abandoned = session("s_abandoned", status = SessionStatus.ABANDONED)
        sessionRepository.saveSessionResult(abandoned)

        val inProgress = session("s_in_progress", status = SessionStatus.IN_PROGRESS)
        sessionRepository.saveSessionResult(inProgress)

        val miniGame = session("s_game", actId = "tangkap_huruf_game")
        sessionRepository.saveSessionResult(miniGame)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals("Only 1 valid session should be counted", 1, result.sessionCount)
        assertEquals(LevelReadinessStatus.INSUFFICIENT_SESSIONS, result.status)
        assertFalse(result.isReadyToUnlock)
    }

    // 30. Partial missing AnswerLogs (2 of 3 sessions have logs, 1 has none) must NOT unlock.
    @Test
    fun test30_partialMissingAnswerLogs_returnsInsufficientEvidence() = runTest {
        seedStandardSessionWithAnswers("s1_with_logs")
        seedStandardSessionWithAnswers("s2_with_logs")
        val s3NoLogs = session("s3_no_logs", total = 5, correct = 5, incorrect = 0, acc = 1.0f)
        sessionRepository.saveSessionResult(s3NoLogs)

        val result = evaluateReadinessUseCase(childId, "pulau_alfabet")
        assertEquals(LevelReadinessStatus.INSUFFICIENT_EVIDENCE, result.status)
        assertFalse(result.isReadyToUnlock)
    }

    // 31. Firestore read error must propagate rather than producing a misleading mastery diagnosis.
    @Test
    fun test31_firestoreReadFailure_propagatesWithoutMisleadingDiagnosis() = runTest {
        val failingSessionRepo = object : SessionRepository {
            override suspend fun recordAnswer(answer: AnswerLog) = Result.success(Unit)
            override suspend fun getAnswersForSession(sessionId: String): List<AnswerLog> = emptyList()
            override suspend fun getAnswersForChild(childId: String): List<AnswerLog> = emptyList()
            override suspend fun getAnswersForChildResult(childId: String): Result<List<AnswerLog>> =
                Result.failure(IllegalStateException("Firestore FAILED_PRECONDITION / offline"))
            override suspend fun saveSessionResult(result: SessionResult) = Result.success(Unit)
            override suspend fun getCompletedSessions(childId: String): List<SessionResult> = emptyList()
            override suspend fun getCompletedSessionsResult(childId: String): Result<List<SessionResult>> =
                Result.failure(IllegalStateException("Firestore FAILED_PRECONDITION / offline"))
            override fun observeCompletedSessions(childId: String): Flow<List<SessionResult>> = emptyFlow()
        }

        val failingEvaluator = EvaluateLevelReadinessUseCase(failingSessionRepo, fixedClock, zoneId)
        try {
            failingEvaluator(childId, "pulau_alfabet")
            org.junit.Assert.fail("Expected exception when Firestore read fails")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("Firestore"))
        }

        // Also test answer read failure after sessions succeed
        val answersFailSessionRepo = object : SessionRepository {
            override suspend fun recordAnswer(answer: AnswerLog) = Result.success(Unit)
            override suspend fun getAnswersForSession(sessionId: String): List<AnswerLog> = emptyList()
            override suspend fun getAnswersForChild(childId: String): List<AnswerLog> = emptyList()
            override suspend fun getAnswersForChildResult(childId: String): Result<List<AnswerLog>> =
                Result.failure(IllegalStateException("PERMISSION_DENIED reading answerLogs"))
            override suspend fun saveSessionResult(result: SessionResult) = Result.success(Unit)
            override suspend fun getCompletedSessions(childId: String): List<SessionResult> = listOf(
                session("s1"),
                session("s2"),
                session("s3")
            )
            override fun observeCompletedSessions(childId: String): Flow<List<SessionResult>> = emptyFlow()
        }

        val answerFailEvaluator = EvaluateLevelReadinessUseCase(answersFailSessionRepo, fixedClock, zoneId)
        try {
            answerFailEvaluator(childId, "pulau_alfabet")
            org.junit.Assert.fail("Expected exception when answerLogs read fails")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("PERMISSION_DENIED"))
        }
    }

    // 32. LevelMapViewModel clears previous profile progress on switch, shows dynamic explanations without 2-day text, and surfaces errors.
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun test32_levelMapViewModel_profileSwitchAndReadErrorHandling() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val authRepo = DemoAuthRepository()
            val profileRepo = DemoProfileRepository()
            authRepo.signIn("ortu@aksa.id", "123456")
            val parentUid = authRepo.currentGuardian.value!!.uid

            val pA = profileRepo.createProfile(parentUid, "Anak A", "mascot_classic").getOrThrow()
            val pB = profileRepo.createProfile(parentUid, "Anak B", "cat_orange").getOrThrow()

            // Seed 3 same-day mastery sessions for Anak A
            val sameDay = timestampForDay(0, 9)
            seedStandardSessionWithAnswers("s1_vm", cId = pA.profileId, time = sameDay)
            seedStandardSessionWithAnswers("s2_vm", cId = pA.profileId, time = sameDay + 60_000)
            seedStandardSessionWithAnswers("s3_vm", cId = pA.profileId, time = sameDay + 120_000)

            AksaUserState.currentProfileId = pA.profileId
            val activeChildUseCase = GetActiveChildIdUseCase(authRepo, profileRepo)
            val progressionUseCase = GetLevelProgressionUseCase(evaluateReadinessUseCase, levelProgressRepository)

            val vm = LevelMapViewModel(
                getActiveChildIdUseCase = activeChildUseCase,
                getLevelProgressionUseCase = progressionUseCase,
                sessionRepository = sessionRepository,
                levelProgressRepository = levelProgressRepository
            )
            advanceUntilIdle()

            assertEquals(pA.profileId, vm.uiState.value.childId)
            assertEquals(LevelNodeStatus.COMPLETED, vm.uiState.value.effectiveStatus1)
            assertEquals(LevelNodeStatus.ACTIVE, vm.uiState.value.effectiveStatus2)

            // Switch to Anak B (new profile with 0 sessions)
            AksaUserState.currentProfileId = pB.profileId
            vm.onProfileChanged()
            advanceUntilIdle()

            assertEquals(pB.profileId, vm.uiState.value.childId)
            assertEquals(LevelNodeStatus.ACTIVE, vm.uiState.value.effectiveStatus1)
            assertEquals(LevelNodeStatus.LOCKED, vm.uiState.value.effectiveStatus2)

            // Click locked Level 2 -> dynamic explanation must NOT mention 2 days
            vm.onLevelClick(2) {}
            val lockedDetails = vm.uiState.value.selectedLockedLevel
            assertEquals("Selesaikan 3 sesi latihan lagi, yuk!", lockedDetails?.explanation)
            assertFalse(lockedDetails!!.isReadError)
            assertFalse(lockedDetails.explanation.contains("2 hari", ignoreCase = true))
            vm.dismissLockedDialog()

            // Verify error handling when repository read fails
            val failingRepo = object : SessionRepository {
                override suspend fun recordAnswer(answer: AnswerLog) = Result.success(Unit)
                override suspend fun getAnswersForSession(sessionId: String): List<AnswerLog> = emptyList()
                override suspend fun getAnswersForChild(childId: String): List<AnswerLog> = emptyList()
                override suspend fun saveSessionResult(result: SessionResult) = Result.success(Unit)
                override suspend fun getCompletedSessions(childId: String): List<SessionResult> = emptyList()
                override suspend fun getCompletedSessionsResult(childId: String): Result<List<SessionResult>> =
                    Result.failure(RuntimeException("Koneksi Firestore terputus"))
                override fun observeCompletedSessions(childId: String): Flow<List<SessionResult>> = emptyFlow()
            }
            val failingVm = LevelMapViewModel(
                getActiveChildIdUseCase = activeChildUseCase,
                getLevelProgressionUseCase = GetLevelProgressionUseCase(
                    EvaluateLevelReadinessUseCase(failingRepo, fixedClock, zoneId),
                    levelProgressRepository
                ),
                sessionRepository = failingRepo,
                levelProgressRepository = levelProgressRepository
            )
            advanceUntilIdle()
            failingVm.refreshProgression()
            advanceUntilIdle()

            assertTrue(failingVm.uiState.value.errorMessage?.contains("Firestore") == true)
            failingVm.onLevelClick(2) {}
            assertTrue(failingVm.uiState.value.selectedLockedLevel?.isReadError == true)
        } finally {
            AksaUserState.clearSession()
            Dispatchers.resetMain()
        }
    }
}
