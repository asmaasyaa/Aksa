package com.aksa.app

import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.usecase.AnalyzeErrorPatternsUseCase
import com.aksa.app.domain.usecase.GenerateHurufMiripQuestionsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AnalyzeErrorPatternsTest {

    private val analyzer = AnalyzeErrorPatternsUseCase()
    private val generator = GenerateHurufMiripQuestionsUseCase(Random(42))

    private fun makeLog(
        target: String,
        isCorrect: Boolean,
        responseTimeMs: Long
    ) = AnswerLog(
        answerId = "id_${System.nanoTime()}",
        sessionId = "s1",
        childId = "c1",
        moduleId = "pulau_alfabet",
        activityId = "huruf_mirip",
        questionId = "q1",
        targetChar = target,
        chosenChar = if (isCorrect) target else "x",
        isCorrect = isCorrect,
        responseTimeMs = responseTimeMs
    )

    @Test
    fun `ignores letters with fewer than 3 attempts`() {
        val logs = listOf(
            makeLog("b", isCorrect = false, responseTimeMs = 5000),
            makeLog("b", isCorrect = false, responseTimeMs = 5000),
            makeLog("d", isCorrect = true, responseTimeMs = 2000),
            makeLog("d", isCorrect = true, responseTimeMs = 2000),
            makeLog("d", isCorrect = false, responseTimeMs = 2000)
        )

        val result = analyzer.analyzeLogs(logs)
        assertEquals(listOf("d"), result.priorityLetters)
    }

    @Test
    fun `ranks priority letters using 0_7 errorRate plus 0_3 normalizedTime and selects top 3`() {
        val logs = buildList {
            repeat(3) { add(makeLog("d", isCorrect = false, responseTimeMs = 4000)) }
            repeat(2) { add(makeLog("p", isCorrect = false, responseTimeMs = 3000)) }
            add(makeLog("p", isCorrect = true, responseTimeMs = 3000))
            add(makeLog("b", isCorrect = false, responseTimeMs = 2000))
            repeat(2) { add(makeLog("b", isCorrect = true, responseTimeMs = 2000)) }
            repeat(3) { add(makeLog("q", isCorrect = true, responseTimeMs = 1000)) }
        }

        val result = analyzer.analyzeLogs(logs)
        assertEquals(listOf("d", "p", "b"), result.priorityLetters)

        val dStat = result.evaluatedLetters.first { it.letter == "d" }
        assertEquals(1.0, dStat.priorityScore, 0.001)
    }

    @Test
    fun `generates 60 percent priority questions and 40 percent other questions`() {
        val priority = listOf("d", "p")
        val questions = generator(priority)

        assertEquals(10, questions.size)
        val priorityCount = questions.count { it.targetLetter in priority }
        val otherCount = questions.count { it.targetLetter !in priority }

        assertEquals(6, priorityCount)
        assertEquals(4, otherCount)
    }

    @Test
    fun `marks pair as needsPractice when pair accuracy is below 70 percent`() {
        val logs = listOf(
            makeLog("b", isCorrect = true, responseTimeMs = 1500),
            makeLog("b", isCorrect = false, responseTimeMs = 2500),
            makeLog("d", isCorrect = false, responseTimeMs = 2500),
            makeLog("d", isCorrect = false, responseTimeMs = 2500)
        )

        val result = analyzer.analyzeLogs(logs)
        val bdStat = result.pairStats.first { it.pairLabel == "b/d" }
        assertTrue(bdStat.needsPractice)
        assertEquals(1, bdStat.correctCount)
        assertEquals(3, bdStat.wrongCount)
    }
}