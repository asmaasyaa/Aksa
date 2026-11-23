package com.aksa.app

import com.aksa.app.domain.usecase.GenerateHurufMiripQuestionsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class HurufMiripQuestionGeneratorTest {

    @Test
    fun generatesExactly10Questions() {
        val generator = GenerateHurufMiripQuestionsUseCase(Random(42))
        val questions = generator()
        assertEquals(10, questions.size)
    }

    @Test
    fun balancedTargetDistributionAcrossSeeds() {
        // Test with multiple seeds to verify controlled distribution:
        // Each target letter (b, d, p, q) must appear 2 or 3 times (total 10: 3+3+2+2)
        val validLetters = setOf("b", "d", "p", "q")

        for (seed in 1..25) {
            val generator = GenerateHurufMiripQuestionsUseCase(Random(seed))
            val questions = generator()

            assertEquals("Must have 10 questions for seed $seed", 10, questions.size)

            val counts = questions.groupingBy { it.targetLetter }.eachCount()
            assertEquals("Must only contain b, d, p, q", validLetters, counts.keys)

            for ((letter, count) in counts) {
                assertTrue(
                    "Letter $letter count $count must be between 2 and 3 for seed $seed",
                    count in 2..3
                )
            }

            assertEquals("Sum of counts must be 10", 10, counts.values.sum())
        }
    }

    @Test
    fun letterPairAssociationsAreCorrect() {
        val generator = GenerateHurufMiripQuestionsUseCase(Random(123))
        val questions = generator()

        for (q in questions) {
            when (q.targetLetter) {
                "b" -> assertEquals("d", q.pairLetter)
                "d" -> assertEquals("b", q.pairLetter)
                "p" -> assertEquals("q", q.pairLetter)
                "q" -> assertEquals("p", q.pairLetter)
            }
            assertTrue("Choices must contain target letter", q.choices.contains(q.targetLetter))
            assertTrue("Choices must contain pair letter", q.choices.contains(q.pairLetter))
            assertEquals("Choices must have exactly 2 options", 2, q.choices.size)
        }
    }

    @Test
    fun deterministicOutputWithSameRandomSeed() {
        val questionsA = GenerateHurufMiripQuestionsUseCase(Random(999))()
        val questionsB = GenerateHurufMiripQuestionsUseCase(Random(999))()

        assertEquals(questionsA.size, questionsB.size)
        for (i in questionsA.indices) {
            assertEquals(questionsA[i].targetLetter, questionsB[i].targetLetter)
            assertEquals(questionsA[i].pairLetter, questionsB[i].pairLetter)
            assertEquals(questionsA[i].choices, questionsB[i].choices)
        }
    }

    @Test
    fun adaptive6040OnePriorityTargetAllocatesSixToPriorityAndFourToOthers() {
        // Priority: 'b'
        // Expected: 'b' gets 6 questions (60%), other 3 letters get 4 questions (40%)
        val generator = GenerateHurufMiripQuestionsUseCase(Random(55))
        val questions = generator(listOf("b"))

        assertEquals(10, questions.size)
        val counts = questions.groupingBy { it.targetLetter }.eachCount()

        assertEquals("Priority letter 'b' must have 6 questions (60%)", 6, counts["b"])
        val nonPrioritySum = (counts["d"] ?: 0) + (counts["p"] ?: 0) + (counts["q"] ?: 0)
        assertEquals("Non-priority letters must have 4 questions (40%)", 4, nonPrioritySum)
        assertEquals(10, counts.values.sum())
    }

    @Test
    fun adaptive6040TwoPriorityTargetsAllocatesThreeEachAndTwoEachToOthers() {
        // Priority: 'b', 'd'
        // Expected: 'b'=3, 'd'=3 (total 6 priority = 60%), 'p'=2, 'q'=2 (total 4 non-priority = 40%)
        val generator = GenerateHurufMiripQuestionsUseCase(Random(66))
        val questions = generator(listOf("b", "d"))

        assertEquals(10, questions.size)
        val counts = questions.groupingBy { it.targetLetter }.eachCount()

        assertEquals("Priority 'b' must have 3 questions", 3, counts["b"])
        assertEquals("Priority 'd' must have 3 questions", 3, counts["d"])
        assertEquals(6, (counts["b"] ?: 0) + (counts["d"] ?: 0))

        assertEquals("Non-priority 'p' must have 2 questions", 2, counts["p"])
        assertEquals("Non-priority 'q' must have 2 questions", 2, counts["q"])
        assertEquals(4, (counts["p"] ?: 0) + (counts["q"] ?: 0))
        assertEquals(10, counts.values.sum())
    }

    @Test
    fun adaptive6040ThreePriorityTargetsAllocatesTwoEachAndFourToSingleNonPriority() {
        // Priority: 'p', 'q', 'b'
        // Expected: 'p'=2, 'q'=2, 'b'=2 (total 6 priority = 60%), 'd'=4 (40%)
        val generator = GenerateHurufMiripQuestionsUseCase(Random(77))
        val questions = generator(listOf("p", "q", "b"))

        assertEquals(10, questions.size)
        val counts = questions.groupingBy { it.targetLetter }.eachCount()

        assertEquals(2, counts["p"])
        assertEquals(2, counts["q"])
        assertEquals(2, counts["b"])
        assertEquals(6, (counts["p"] ?: 0) + (counts["q"] ?: 0) + (counts["b"] ?: 0))

        assertEquals("Remaining letter 'd' must have 4 questions (40%)", 4, counts["d"])
        assertEquals(10, counts.values.sum())
    }

    @Test
    fun adaptiveFourEligibleLettersTreatsTopThreeAsPriorityAndFourthAsNonPriority() {
        // Input: 4 eligible letters ['d', 'p', 'b', 'q']
        // Top 3 selected as priority: ['d', 'p', 'b'] (2 each = 6 priority)
        // 4th treated as non-priority: 'q' (gets 4)
        val generator = GenerateHurufMiripQuestionsUseCase(Random(88))
        val questions = generator(listOf("d", "p", "b", "q"))

        assertEquals(10, questions.size)
        val counts = questions.groupingBy { it.targetLetter }.eachCount()

        assertEquals(2, counts["d"])
        assertEquals(2, counts["p"])
        assertEquals(2, counts["b"])
        assertEquals(6, (counts["d"] ?: 0) + (counts["p"] ?: 0) + (counts["b"] ?: 0))
        assertEquals(4, counts["q"])
        assertEquals(10, counts.values.sum())
    }

    @Test
    fun adaptiveFiltersDuplicatesAndInvalidLetters() {
        val generator = GenerateHurufMiripQuestionsUseCase(Random(101))
        // 'z' is invalid, 'b' is duplicated -> 1 valid priority letter 'b'
        val questions = generator(listOf("b", "z", "b"))

        assertEquals(10, questions.size)
        val counts = questions.groupingBy { it.targetLetter }.eachCount()

        assertEquals(6, counts["b"])
        val nonPrioritySum = (counts["d"] ?: 0) + (counts["p"] ?: 0) + (counts["q"] ?: 0)
        assertEquals(4, nonPrioritySum)
    }

    @Test
    fun everyLetterAppearsAtLeastOnceInAllAdaptiveModes() {
        val testCases = listOf(
            emptyList(),
            listOf("b"),
            listOf("d"),
            listOf("p"),
            listOf("q"),
            listOf("b", "d"),
            listOf("p", "q"),
            listOf("b", "p", "q"),
            listOf("b", "d", "p", "q")
        )

        val requiredLetters = setOf("b", "d", "p", "q")

        for (priorityList in testCases) {
            val generator = GenerateHurufMiripQuestionsUseCase(Random(202))
            val questions = generator(priorityList)

            assertEquals(10, questions.size)
            val counts = questions.groupingBy { it.targetLetter }.eachCount()

            assertEquals("All 4 letters must be present", requiredLetters, counts.keys)
            for (letter in requiredLetters) {
                assertTrue(
                    "Letter '$letter' must appear at least once for priorityList $priorityList",
                    (counts[letter] ?: 0) >= 1
                )
            }
        }
    }
}
