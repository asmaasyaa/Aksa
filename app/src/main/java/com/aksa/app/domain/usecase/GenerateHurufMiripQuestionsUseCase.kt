package com.aksa.app.domain.usecase

import com.aksa.app.domain.model.HurufMiripQuestion
import kotlin.random.Random

/**
 * Generates exactly 10 questions for a Huruf Mirip session.
 * Targets are b, d, p, and q.
 *
 * Supports:
 * - Standard balanced distribution: each letter appears 2 or 3 times (3+3+2+2 = 10).
 * - Adaptive distribution based on weakTargets: prioritizes weak letters while ensuring
 *   every letter appears >= 1 time and total questions is exactly 10.
 *
 * Supports an injectable [Random] instance for deterministic testing.
 */
class GenerateHurufMiripQuestionsUseCase(
    private val random: Random = Random.Default
) {
    private val validLetters = listOf("b", "d", "p", "q")

    operator fun invoke(): List<HurufMiripQuestion> {
        return invoke(emptyList())
    }

    operator fun invoke(weakTargets: List<String>): List<HurufMiripQuestion> {
        val filteredWeak = weakTargets.distinct().filter { it in validLetters }
        val targetLetters = mutableListOf<String>()

        when (filteredWeak.size) {
            0 -> {
                // Balanced standard: 2 letters get 3, 2 letters get 2 (3+3+2+2 = 10)
                val letters = validLetters.shuffled(random)
                targetLetters.addAll(List(3) { letters[0] })
                targetLetters.addAll(List(3) { letters[1] })
                targetLetters.addAll(List(2) { letters[2] })
                targetLetters.addAll(List(2) { letters[3] })
            }
            1 -> {
                // 1 weak target: weak gets 4 questions, other 3 letters get 2 each (4+2+2+2 = 10)
                val weak = filteredWeak[0]
                val others = (validLetters - weak).shuffled(random)
                targetLetters.addAll(List(4) { weak })
                others.forEach { letter ->
                    targetLetters.addAll(List(2) { letter })
                }
            }
            2 -> {
                // 2 weak targets: both weak get 3 each, other 2 letters get 2 each (3+3+2+2 = 10)
                val weak1 = filteredWeak[0]
                val weak2 = filteredWeak[1]
                val others = (validLetters - setOf(weak1, weak2)).shuffled(random)
                targetLetters.addAll(List(3) { weak1 })
                targetLetters.addAll(List(3) { weak2 })
                others.forEach { letter ->
                    targetLetters.addAll(List(2) { letter })
                }
            }
            3 -> {
                // 3 weak targets: 1st weak gets 4, 2nd gets 3, 3rd gets 2, remaining letter gets 1 (4+3+2+1 = 10)
                val weak1 = filteredWeak[0]
                val weak2 = filteredWeak[1]
                val weak3 = filteredWeak[2]
                val remaining = (validLetters - setOf(weak1, weak2, weak3)).first()
                targetLetters.addAll(List(4) { weak1 })
                targetLetters.addAll(List(3) { weak2 })
                targetLetters.addAll(List(2) { weak3 })
                targetLetters.add(remaining)
            }
            else -> {
                // 4 weak targets: 1st gets 3, 2nd gets 3, 3rd gets 2, 4th gets 2 (3+3+2+2 = 10)
                val weak1 = filteredWeak[0]
                val weak2 = filteredWeak[1]
                val weak3 = filteredWeak[2]
                val weak4 = filteredWeak[3]
                targetLetters.addAll(List(3) { weak1 })
                targetLetters.addAll(List(3) { weak2 })
                targetLetters.addAll(List(2) { weak3 })
                targetLetters.addAll(List(2) { weak4 })
            }
        }

        // Shuffle order of questions
        val shuffledTargets = targetLetters.shuffled(random)

        return shuffledTargets.mapIndexed { index, target ->
            createQuestion(
                id = "hm_q_${index + 1}",
                target = target,
                random = random
            )
        }
    }

    private fun createQuestion(
        id: String,
        target: String,
        random: Random
    ): HurufMiripQuestion {
        val (pair, hintTitle, leftHint, rightHint) = when (target) {
            "b" -> Quadruple(
                "d",
                "Perbedaan b dan d:",
                "b" to "Bulatannya ada di kanan",
                "d" to "Bulatannya ada di kiri"
            )
            "d" -> Quadruple(
                "b",
                "Perbedaan b dan d:",
                "d" to "Bulatannya ada di kiri",
                "b" to "Bulatannya ada di kanan"
            )
            "p" -> Quadruple(
                "q",
                "Perbedaan p dan q:",
                "p" to "Batang turun, bulatan di kanan",
                "q" to "Batang turun, bulatan di kiri"
            )
            "q" -> Quadruple(
                "p",
                "Perbedaan p dan q:",
                "q" to "Batang turun, bulatan di kiri",
                "p" to "Batang turun, bulatan di kanan"
            )
            else -> error("Unsupported target letter: $target")
        }

        val choices = listOf(target, pair).shuffled(random)

        return HurufMiripQuestion(
            id = id,
            targetLetter = target,
            pairLetter = pair,
            choices = choices,
            promptText = "Mana huruf $target?",
            audioPrompt = "Mana huruf $target?",
            hintTitle = hintTitle,
            leftHint = leftHint,
            rightHint = rightHint
        )
    }

    private data class Quadruple<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}
