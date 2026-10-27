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
}
