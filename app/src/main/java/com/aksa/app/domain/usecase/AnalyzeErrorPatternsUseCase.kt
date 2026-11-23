package com.aksa.app.domain.usecase

import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.repository.SessionRepository

data class LetterErrorStat(
    val letter: String,
    val totalAttempts: Int,
    val wrongAttempts: Int,
    val errorRate: Double,
    val averageResponseTimeMs: Double,
    val normalizedResponseTime: Double,
    val priorityScore: Double
)

data class PairConfusionStat(
    val pairLabel: String, // "b/d" atau "p/q"
    val correctCount: Int,
    val wrongCount: Int,
    val accuracy: Double,
    val needsPractice: Boolean // true jika akurasi < 70%
)

data class ErrorAnalysisResult(
    val evaluatedLetters: List<LetterErrorStat>,
    val priorityLetters: List<String>, // Maksimal 3 huruf teratas
    val pairStats: List<PairConfusionStat>
)

class AnalyzeErrorPatternsUseCase(
    private val sessionRepository: SessionRepository? = null
) {
    companion object {
        val SUPPORTED_LETTERS = listOf("b", "d", "p", "q")
        const val MIN_ATTEMPTS_THRESHOLD = 3
        const val MAX_PRIORITY_LETTERS = 3
        const val MAX_RESPONSE_TIME_CAP_MS = 10_000.0
    }

    /**
     * Mengambil log jawaban dari 3 sesi terakhir yang COMPLETED untuk childId tertentu,
     * lalu menganalisis pola kesalahan dan menentukan maksimal 3 huruf prioritas.
     */
    suspend operator fun invoke(childId: String): ErrorAnalysisResult {
        val repo = sessionRepository ?: return analyzeLogs(emptyList())

        val recentSessions = repo.getCompletedSessions(childId)
            .filter { it.isCompleted && it.activityId == "huruf_mirip" }
            .sortedByDescending { it.completedAt ?: it.startedAt }
            .take(3)

        val logs = recentSessions.flatMap { session ->
            repo.getAnswersForSession(session.sessionId)
        }

        return analyzeLogs(logs)
    }

    /**
     * Fungsi murni (pure function) untuk memproses daftar AnswerLog agar mudah di-unit test.
     */
    fun analyzeLogs(logs: List<AnswerLog>): ErrorAnalysisResult {
        val validLogs = logs.filter { it.targetChar.lowercase() in SUPPORTED_LETTERS }

        // 1. Hitung statistik per pasangan b/d dan p/q
        val pairStats = listOf(
            calculatePairStat("b/d", setOf("b", "d"), validLogs),
            calculatePairStat("p/q", setOf("p", "q"), validLogs)
        )

        if (validLogs.isEmpty()) {
            return ErrorAnalysisResult(
                evaluatedLetters = emptyList(),
                priorityLetters = emptyList(),
                pairStats = pairStats
            )
        }

        // 2. Kelompokkan per huruf target & abaikan yang percobaannya < 3
        val groupedByLetter = validLogs.groupBy { it.targetChar.lowercase() }
        val rawMetrics = groupedByLetter.mapNotNull { (letter, letterLogs) ->
            val total = letterLogs.size
            if (total < MIN_ATTEMPTS_THRESHOLD) return@mapNotNull null

            val wrong = letterLogs.count { !it.isCorrect }
            val errorRate = wrong.toDouble() / total.toDouble()
            val avgTimeMs = letterLogs
                .map { it.responseTimeMs.coerceIn(0L, MAX_RESPONSE_TIME_CAP_MS.toLong()) }
                .average()

            RawLetterMetric(
                letter = letter,
                totalAttempts = total,
                wrongAttempts = wrong,
                errorRate = errorRate,
                avgResponseTimeMs = avgTimeMs
            )
        }

        if (rawMetrics.isEmpty()) {
            return ErrorAnalysisResult(
                evaluatedLetters = emptyList(),
                priorityLetters = emptyList(),
                pairStats = pairStats
            )
        }

        // 3. Normalisasi waktu respons ke skala 0-1
        val minTime = rawMetrics.minOf { it.avgResponseTimeMs }
        val maxTime = rawMetrics.maxOf { it.avgResponseTimeMs }

        val evaluatedStats = rawMetrics.map { metric ->
            val normalizedTime = if (maxTime > minTime) {
                (metric.avgResponseTimeMs - minTime) / (maxTime - minTime)
            } else {
                // Jika semua huruf memiliki waktu respons identik, normalisasi terhadap batas wajar 10 detik
                (metric.avgResponseTimeMs / MAX_RESPONSE_TIME_CAP_MS).coerceIn(0.0, 1.0)
            }

            // Rumus laporan: Skor Prioritas = 0.7 * errorRate + 0.3 * waktuNorm
            val priorityScore = (0.7 * metric.errorRate) + (0.3 * normalizedTime)

            LetterErrorStat(
                letter = metric.letter,
                totalAttempts = metric.totalAttempts,
                wrongAttempts = metric.wrongAttempts,
                errorRate = metric.errorRate,
                averageResponseTimeMs = metric.avgResponseTimeMs,
                normalizedResponseTime = normalizedTime,
                priorityScore = priorityScore
            )
        }.sortedWith(
            compareByDescending<LetterErrorStat> { it.priorityScore }
                .thenByDescending { it.errorRate }
                .thenBy { it.letter }
        )

        // 4. Pilih maksimal 3 huruf teratas sebagai prioritas latihan
        val topPriorityLetters = evaluatedStats
            .take(MAX_PRIORITY_LETTERS)
            .map { it.letter }

        return ErrorAnalysisResult(
            evaluatedLetters = evaluatedStats,
            priorityLetters = topPriorityLetters,
            pairStats = pairStats
        )
    }

    private fun calculatePairStat(
        pairLabel: String,
        letters: Set<String>,
        logs: List<AnswerLog>
    ): PairConfusionStat {
        val pairLogs = logs.filter { it.targetChar.lowercase() in letters }
        val total = pairLogs.size
        val correct = pairLogs.count { it.isCorrect }
        val wrong = total - correct
        val accuracy = if (total > 0) correct.toDouble() / total.toDouble() else 1.0

        return PairConfusionStat(
            pairLabel = pairLabel,
            correctCount = correct,
            wrongCount = wrong,
            accuracy = accuracy,
            needsPractice = total > 0 && accuracy < 0.70
        )
    }

    private data class RawLetterMetric(
        val letter: String,
        val totalAttempts: Int,
        val wrongAttempts: Int,
        val errorRate: Double,
        val avgResponseTimeMs: Double
    )
}