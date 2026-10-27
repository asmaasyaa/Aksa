package com.aksa.app.domain.usecase

import com.aksa.app.domain.model.HurufMiripQuestion
import kotlin.random.Random

/**
 * Generates exactly 10 questions for a Huruf Mirip session.
 * Targets are b, d, p, and q with a balanced distribution where each letter appears 2 or 3 times (3+3+2+2 = 10).
 * Supports an injectable [Random] instance for deterministic testing.
 */
class GenerateHurufMiripQuestionsUseCase(
    private val random: Random = Random.Default
) {
    operator fun invoke(): List<HurufMiripQuestion> {
        // Choose two letters to appear 3 times and two letters to appear 2 times
        val letters = listOf("b", "d", "p", "q").shuffled(random)
        val targetLetters = mutableListOf<String>()

        // 2 letters get 3 occurrences, 2 letters get 2 occurrences (total = 10)
        targetLetters.addAll(List(3) { letters[0] })
        targetLetters.addAll(List(3) { letters[1] })
        targetLetters.addAll(List(2) { letters[2] })
        targetLetters.addAll(List(2) { letters[3] })

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
