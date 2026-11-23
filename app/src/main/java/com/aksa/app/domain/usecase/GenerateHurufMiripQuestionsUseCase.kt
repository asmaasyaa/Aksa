package com.aksa.app.domain.usecase

import com.aksa.app.domain.model.HurufMiripQuestion
import kotlin.random.Random

/**
 * Generates exactly 10 questions for a Huruf Mirip session.
 * - If [priorityLetters] is empty: balanced distribution (3+3+2+2 = 10).
 * - If [priorityLetters] is provided (1..3 letters): 60% (6 questions) from priority letters,
 *   and 40% (4 questions) from the remaining letters.
 */
class GenerateHurufMiripQuestionsUseCase(
    private val random: Random = Random.Default
) {
    operator fun invoke(priorityLetters: List<String> = emptyList()): List<HurufMiripQuestion> {
        val allLetters = listOf("b", "d", "p", "q")
        val validPriority = priorityLetters
            .map { it.lowercase() }
            .distinct()
            .filter { it in allLetters }
            .take(3)

        val targetLetters = if (validPriority.isEmpty()) {
            // Distribusi seimbang default (sesi awal / data belum cukup)
            val shuffled = allLetters.shuffled(random)
            buildList {
                addAll(List(3) { shuffled[0] })
                addAll(List(3) { shuffled[1] })
                addAll(List(2) { shuffled[2] })
                addAll(List(2) { shuffled[3] })
            }
        } else {
            val otherLetters = allLetters.filterNot { it in validPriority }
            val priorityPool = buildBalancedPool(validPriority, totalCount = 6)
            val nonPriorityPool = if (otherLetters.isNotEmpty()) {
                buildBalancedPool(otherLetters, totalCount = 4)
            } else {
                buildBalancedPool(allLetters, totalCount = 4)
            }
            priorityPool + nonPriorityPool
        }

        val shuffledTargets = targetLetters.shuffled(random)

        return shuffledTargets.mapIndexed { index, target ->
            createQuestion(
                id = "hm_q_${index + 1}",
                target = target,
                random = random
            )
        }
    }

    private fun buildBalancedPool(letters: List<String>, totalCount: Int): List<String> {
        val pool = mutableListOf<String>()
        val baseCount = totalCount / letters.size
        val remainder = totalCount % letters.size

        letters.forEachIndexed { index, letter ->
            val count = baseCount + if (index < remainder) 1 else 0
            repeat(count) { pool.add(letter) }
        }
        return pool
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
