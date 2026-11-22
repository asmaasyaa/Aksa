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
    fun adaptiveOneWeakTargetAllocatesFourQuestions() {
        val generator = GenerateHurufMiripQuestionsUseCase(Random(55))
        val questions = generator(listOf("b"))

        assertEquals(10, questions.size)
        val counts = questions.groupingBy { it.targetLetter }.eachCount()

        assertEquals("Weak letter 'b' must have exactly 4 questions", 4, counts["b"])
        assertEquals("Letter 'd' must have exactly 2 questions", 2, counts["d"])
        assertEquals("Letter 'p' must have exactly 2 questions", 2, counts["p"])
        assertEquals("Letter 'q' must have exactly 2 questions", 2, counts["q"])
        assertEquals(10, counts.values.sum())
    }

    @Test
    fun adaptiveTwoWeakTargetsAllocatesThreeQuestionsEach() {
        val generator = GenerateHurufMiripQuestionsUseCase(Random(66))
        val questions = generator(listOf("b", "d"))

        assertEquals(10, questions.size)
        val counts = questions.groupingBy { it.targetLetter }.eachCount()

        assertEquals("Weak letter 'b' must have 3 questions", 3, counts["b"])
        assertEquals("Weak letter 'd' must have 3 questions", 3, counts["d"])
        assertEquals("Letter 'p' must have 2 questions", 2, counts["p"])
        assertEquals("Letter 'q' must have 2 questions", 2, counts["q"])
        assertEquals(10, counts.values.sum())
    }

    @Test
    fun adaptiveThreeWeakTargetsAllocatesFourThreeTwoOne() {
        val generator = GenerateHurufMiripQuestionsUseCase(Random(77))
        val questions = generator(listOf("p", "q", "b"))

        assertEquals(10, questions.size)
        val counts = questions.groupingBy { it.targetLetter }.eachCount()

        assertEquals("Top weak letter 'p' must have 4 questions", 4, counts["p"])
        assertEquals("Second weak letter 'q' must have 3 questions", 3, counts["q"])
        assertEquals("Third weak letter 'b' must have 2 questions", 2, counts["b"])
        assertEquals("Remaining strong letter 'd' must have 1 question", 1, counts["d"])
        assertEquals(10, counts.values.sum())
    }

    @Test
    fun adaptiveFourWeakTargetsAllocatesThreeThreeTwoTwo() {
        val generator = GenerateHurufMiripQuestionsUseCase(Random(88))
        val questions = generator(listOf("d", "p", "b", "q"))

        assertEquals(10, questions.size)
        val counts = questions.groupingBy { it.targetLetter }.eachCount()

        assertEquals("Top weak letter 'd' must have 3 questions", 3, counts["d"])
        assertEquals("Second weak letter 'p' must have 3 questions", 3, counts["p"])
        assertEquals("Third weak letter 'b' must have 2 questions", 2, counts["b"])
        assertEquals("Fourth weak letter 'q' must have 2 questions", 2, counts["q"])
        assertEquals(10, counts.values.sum())
    }

    @Test
    fun adaptiveFiltersDuplicatesAndInvalidLetters() {
        val generator = GenerateHurufMiripQuestionsUseCase(Random(101))
        // 'z' is invalid, 'b' is duplicated
        val questions = generator(listOf("b", "z", "b"))

        assertEquals(10, questions.size)
        val counts = questions.groupingBy { it.targetLetter }.eachCount()

        assertEquals(4, counts["b"])
        assertEquals(2, counts["d"])
        assertEquals(2, counts["p"])
        assertEquals(2, counts["q"])
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

        for (weakList in testCases) {
            val generator = GenerateHurufMiripQuestionsUseCase(Random(202))
            val questions = generator(weakList)

            assertEquals(10, questions.size)
            val counts = questions.groupingBy { it.targetLetter }.eachCount()

            assertEquals("All 4 letters must be present", requiredLetters, counts.keys)
            for (letter in requiredLetters) {
                assertTrue(
                    "Letter '$letter' must appear at least once for weakList $weakList",
                    (counts[letter] ?: 0) >= 1
                )
            }
        }
    }
}
