package com.aksa.app.domain.usecase

import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.model.HurufMiripQuestion
import com.aksa.app.domain.model.TargetPerformance

/**
 * Generates adaptive "Pulau Alfabet" (Huruf Mirip) questions covering:
 * - Mirror pairs: b / d, p / q
 * - Rotational/curve pairs: m / n, u / w
 */
class GenerateHurufMiripQuestionsUseCase {

    private data class ConfusablePairSpec(
        val firstLetter: String,
        val firstWord: String,
        val firstHint: String,
        val secondLetter: String,
        val secondWord: String,
        val secondHint: String
    )

    private val pairSpecs = listOf(
        ConfusablePairSpec(
            firstLetter = "b",
            firstWord = "bola",
            firstHint = "Perut di kanan",
            secondLetter = "d",
            secondWord = "domba",
            secondHint = "Perut di kiri"
        ),
        ConfusablePairSpec(
            firstLetter = "p",
            firstWord = "palu",
            firstHint = "Kepala di kanan bawah",
            secondLetter = "q",
            secondWord = "quran",
            secondHint = "Kepala di kiri bawah"
        ),
        ConfusablePairSpec(
            firstLetter = "m",
            firstWord = "mata",
            firstHint = "Dua lengkung ke bawah",
            secondLetter = "n",
            secondWord = "nasi",
            secondHint = "Satu lengkung ke bawah"
        ),
        ConfusablePairSpec(
            firstLetter = "u",
            firstWord = "ular",
            firstHint = "Satu mangkuk terbuka ke atas",
            secondLetter = "w",
            secondWord = "wajah",
            secondHint = "Dua mangkuk terbuka ke atas"
        )
    )

    // Dipanggil oleh HurufMiripViewModel: questionGenerator(analysisResult.priorityLetters)
    operator fun invoke(
        priorityLetters: List<String>,
        questionCount: Int = 10
    ): List<HurufMiripQuestion> {
        val priorityMap = priorityLetters
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .mapIndexed { index, letter ->
                val syntheticErrorRate = (1.0f - index * 0.1f).coerceAtLeast(0.45f)
                letter to TargetPerformance(
                    target = letter,
                    totalAttempts = 2,
                    incorrectAttempts = 1,
                    errorRate = syntheticErrorRate
                )
            }
            .toMap()

        return invoke(questionCount = questionCount, weakTargets = priorityMap)
    }

    @JvmName("invokeFromLogs")
    operator fun invoke(
        recentAnswers: List<AnswerLog>,
        questionCount: Int = 10
    ): List<HurufMiripQuestion> {
        val weakTargets = recentAnswers
            .filter { it.targetChar.isNotBlank() }
            .groupBy { it.targetChar.trim().lowercase() }
            .mapValues { (target, logs) ->
                val total = logs.size
                val wrong = logs.count { !it.isCorrect }
                TargetPerformance(
                    target = target,
                    totalAttempts = total,
                    incorrectAttempts = wrong,
                    errorRate = if (total > 0) wrong.toFloat() / total.toFloat() else 0f
                )
            }
        return invoke(questionCount = questionCount, weakTargets = weakTargets)
    }

    operator fun invoke(
        questionCount: Int = 10,
        weakTargets: Map<String, TargetPerformance> = emptyMap()
    ): List<HurufMiripQuestion> {
        val candidates = pairSpecs.flatMap { spec ->
            listOf(
                Triple(spec.firstLetter, spec.firstWord, spec),
                Triple(spec.secondLetter, spec.secondWord, spec)
            )
        }

        val weightedPool = candidates.sortedByDescending { (letter, _, _) ->
            val perf = weakTargets[letter]
            if (perf != null && perf.totalAttempts > 0) perf.errorRate else 0.25f
        }

        val selected = mutableListOf<Triple<String, String, ConfusablePairSpec>>()
        var idx = 0
        while (selected.size < questionCount) {
            selected.add(weightedPool[idx % weightedPool.size])
            idx++
        }

        return selected.shuffled().mapIndexed { index, (targetLetter, clueWord, spec) ->
            val pairLetter = if (targetLetter == spec.firstLetter) spec.secondLetter else spec.firstLetter
            val choices = listOf(spec.firstLetter, spec.secondLetter).shuffled()
            HurufMiripQuestion(
                id = "hm_${index + 1}_$targetLetter",
                targetLetter = targetLetter,
                pairLetter = pairLetter,
                choices = choices,
                promptText = "Ketuk huruf '$targetLetter' seperti pada kata \"$clueWord\"",
                audioPrompt = "Pilih huruf $targetLetter, seperti pada kata $clueWord.",
                hintTitle = "Bedakan huruf '${spec.firstLetter}' dan '${spec.secondLetter}'",
                leftHint = spec.firstLetter to spec.firstHint,
                rightHint = spec.secondLetter to spec.secondHint
            )
        }
    }
}