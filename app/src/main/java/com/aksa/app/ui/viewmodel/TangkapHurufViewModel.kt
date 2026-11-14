package com.aksa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksa.app.audio.AksaAudioSpeaker
import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.repository.AchievementRepository
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.SessionRepository
import com.aksa.app.domain.usecase.EvaluateWeeklyMilestonesUseCase
import com.aksa.app.ui.state.AksaUserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.UUID
import kotlin.random.Random

enum class MiniGameStage {
    INSTRUCTIONS,
    PLAYING,
    RESULTS
}

data class FallingLetterItem(
    val id: String,
    val letter: String,
    val isTarget: Boolean,
    val normX: Float,
    val spawnDelayMs: Long = 0L,
    val fallDurationMs: Long = 4500L
)

data class TangkapHurufUiState(
    val sessionId: String = "",
    val stage: MiniGameStage = MiniGameStage.INSTRUCTIONS,
    val currentRound: Int = 1,
    val totalRounds: Int = 15,
    val score: Int = 0,
    val correctAnswersCount: Int = 0,
    val incorrectAnswersCount: Int = 0,
    val difficultyLevel: Int = 1,
    val consecutiveCorrect: Int = 0,
    val consecutiveIncorrect: Int = 0,
    val targetLetter: String = "",
    val fallingLetters: List<FallingLetterItem> = emptyList(),
    val roundResolved: Boolean = false,
    val roundFeedbackMsg: String? = null,
    val lastIsCorrect: Boolean? = null,
    val lastScoreEarned: Int = 0,
    val lastWasFast: Boolean = false,
    val accuracy: Float = 0f,
    val starRating: Int = 1,
    val sessionResult: SessionResult? = null,
    val newlyAwardedMilestones: List<Int> = emptyList(),
    val isTtsReady: Boolean = false
)

class TangkapHurufViewModel(
    private val audioSpeaker: AksaAudioSpeaker? = null,
    private val sessionRepository: SessionRepository = AksaRepositoryProvider.sessionRepository,
    private val achievementRepository: AchievementRepository = AksaRepositoryProvider.achievementRepository,
    private val authRepository: AuthRepository = AksaRepositoryProvider.authRepository,
    private val evaluateWeeklyMilestonesUseCase: EvaluateWeeklyMilestonesUseCase = EvaluateWeeklyMilestonesUseCase(achievementRepository),
    private val random: Random = Random.Default
) : ViewModel() {

    private var sessionId = "session_tg_${UUID.randomUUID()}"
    private var startedAtMs: Long = System.currentTimeMillis()
    private var isSessionCommitted = false

    // Visually similar letter pairs for Level 3 dyslexia adaptation
    private val similarPairs = listOf(
        "B" to "D",
        "P" to "Q",
        "M" to "N",
        "U" to "V",
        "E" to "F",
        "C" to "G",
        "K" to "R",
        "W" to "M",
        "b" to "d",
        "p" to "q"
    )

    private val alphabetPool = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
        "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
        "U", "V", "W", "X", "Y", "Z", "b", "d", "p", "q"
    )

    private val _uiState = MutableStateFlow(
        TangkapHurufUiState(sessionId = sessionId)
    )
    val uiState: StateFlow<TangkapHurufUiState> = _uiState.asStateFlow()

    init {
        if (audioSpeaker != null) {
            viewModelScope.launch {
                audioSpeaker.isReady.collect { ready ->
                    _uiState.value = _uiState.value.copy(isTtsReady = ready)
                }
            }
        }

        determineInitialDifficulty()
    }

    private fun determineInitialDifficulty() {
        viewModelScope.launch {
            val childId = getActiveChildId()
            val completedSessions = sessionRepository.getCompletedSessions(childId)
                .filter { it.activityId == "tangkap_huruf" }

            val initialLevel = if (completedSessions.isNotEmpty()) {
                val latest = completedSessions.maxByOrNull { it.completedAt ?: 0L }
                when {
                    latest != null && latest.accuracy >= 0.85f -> 2
                    latest != null && latest.accuracy < 0.60f -> 1
                    else -> 1
                }
            } else {
                1
            }

            _uiState.value = _uiState.value.copy(difficultyLevel = initialLevel)
        }
    }

    fun startGame() {
        sessionId = "session_tg_${UUID.randomUUID()}"
        startedAtMs = System.currentTimeMillis()
        isSessionCommitted = false

        _uiState.value = _uiState.value.copy(
            sessionId = sessionId,
            stage = MiniGameStage.PLAYING,
            currentRound = 1,
            score = 0,
            correctAnswersCount = 0,
            incorrectAnswersCount = 0,
            consecutiveCorrect = 0,
            consecutiveIncorrect = 0,
            sessionResult = null,
            newlyAwardedMilestones = emptyList()
        )

        prepareRound(1)
    }

    fun playCurrentTargetAudio() {
        val target = _uiState.value.targetLetter
        if (target.isNotEmpty()) {
            audioSpeaker?.stop()
            audioSpeaker?.speak("Tangkap huruf $target!")
        }
    }

    private fun prepareRound(roundNum: Int) {
        val state = _uiState.value
        val level = state.difficultyLevel

        val target = alphabetPool.random(random)
        val distractors = mutableListOf<String>()

        when (level) {
            1 -> {
                val nonSimilarPool = alphabetPool.filter { char ->
                    char != target && similarPairs.none { (a, b) ->
                        (a.equals(target, ignoreCase = true) && b.equals(char, ignoreCase = true)) ||
                        (b.equals(target, ignoreCase = true) && a.equals(char, ignoreCase = true))
                    }
                }
                val distractor = nonSimilarPool.randomOrNull(random) ?: alphabetPool.filter { it != target }.random(random)
                distractors.add(distractor)
            }
            2 -> {
                val available = alphabetPool.filter { it != target }.shuffled(random)
                distractors.addAll(available.take(2))
            }
            else -> {
                val similarPair = similarPairs.find { it.first.equals(target, ignoreCase = true) }?.second
                    ?: similarPairs.find { it.second.equals(target, ignoreCase = true) }?.first

                if (similarPair != null && similarPair != target && alphabetPool.contains(similarPair)) {
                    distractors.add(similarPair)
                    val otherPool = alphabetPool.filter { it != target && it != similarPair }.shuffled(random)
                    distractors.addAll(otherPool.take(1))
                } else {
                    val available = alphabetPool.filter { it != target }.shuffled(random)
                    distractors.addAll(available.take(2))
                }
            }
        }

        val itemsCount = distractors.size + 1
        val baseLanes = when (itemsCount) {
            2 -> listOf(0.28f, 0.72f)
            3 -> listOf(0.20f, 0.50f, 0.80f)
            else -> listOf(0.18f, 0.38f, 0.62f, 0.82f).take(itemsCount)
        }.shuffled(random)

        val fallDuration = when (level) {
            1 -> 4800L
            2 -> 4000L
            else -> 3200L
        }

        val baseGap = when (level) {
            1 -> 2400L
            2 -> 2000L
            else -> 1700L
        }

        val rawList = mutableListOf<Pair<String, Boolean>>()
        rawList.add(target to true)
        distractors.forEach { dist ->
            rawList.add(dist to false)
        }

        val shuffledList = rawList.shuffled(random)
        var currentDelay = 400L
        val items = mutableListOf<FallingLetterItem>()

        shuffledList.forEachIndexed { index, (char, isTarget) ->
            val jitter = (random.nextFloat() * 0.08f - 0.04f)
            val normX = (baseLanes[index] + jitter).coerceIn(0.16f, 0.84f)
            val id = if (isTarget) "item_${roundNum}_target" else "item_${roundNum}_dist_$index"

            items.add(
                FallingLetterItem(
                    id = id,
                    letter = char,
                    isTarget = isTarget,
                    normX = normX,
                    spawnDelayMs = currentDelay,
                    fallDurationMs = fallDuration
                )
            )

            val gapJitter = if (level == 3) random.nextInt(200) - 100 else random.nextInt(300) - 150
            currentDelay += (baseGap + gapJitter)
        }

        _uiState.value = _uiState.value.copy(
            currentRound = roundNum,
            targetLetter = target,
            fallingLetters = items,
            roundResolved = false,
            roundFeedbackMsg = null,
            lastIsCorrect = null,
            lastScoreEarned = 0,
            lastWasFast = false
        )

        audioSpeaker?.stop()
        audioSpeaker?.speak("Tangkap huruf $target!")
    }

    fun onLetterCaught(letterItem: FallingLetterItem, responseTimeMs: Long) {
        val state = _uiState.value
        if (state.roundResolved) return

        val isCorrect = letterItem.isTarget
        val chosenChar = letterItem.letter
        processAnswer(chosenChar = chosenChar, isCorrect = isCorrect, responseTimeMs = responseTimeMs)
    }

    fun onLetterMissed(responseTimeMs: Long) {
        val state = _uiState.value
        if (state.roundResolved) return

        processAnswer(chosenChar = "", isCorrect = false, responseTimeMs = responseTimeMs)
    }

    private fun processAnswer(chosenChar: String, isCorrect: Boolean, responseTimeMs: Long) {
        val state = _uiState.value
        val level = state.difficultyLevel
        val currentRound = state.currentRound

        val baseScore = 10 * level
        val isFast = responseTimeMs < 3000
        val bonus = if (isCorrect && isFast) 5 else 0
        val roundScore = if (isCorrect) (baseScore + bonus) else 0

        val newScore = state.score + roundScore
        val newCorrectCount = if (isCorrect) state.correctAnswersCount + 1 else state.correctAnswersCount
        val newIncorrectCount = if (!isCorrect) state.incorrectAnswersCount + 1 else state.incorrectAnswersCount

        var newConsecutiveCorrect = if (isCorrect) state.consecutiveCorrect + 1 else 0
        var newConsecutiveIncorrect = if (!isCorrect) state.consecutiveIncorrect + 1 else 0
        var newLevel = level

        if (newConsecutiveCorrect >= 3) {
            if (newLevel < 3) {
                newLevel++
            }
            newConsecutiveCorrect = 0
        } else if (newConsecutiveIncorrect >= 2) {
            if (newLevel > 1) {
                newLevel--
            }
            newConsecutiveIncorrect = 0
        }

        val feedbackMsg = when {
            isCorrect && isFast -> "Super Cepat! +$roundScore Point 🌟"
            isCorrect -> "Hebat! +$roundScore Point"
            chosenChar.isNotEmpty() -> "Itu huruf $chosenChar. Cari ${state.targetLetter}!"
            else -> "Huruf ${state.targetLetter} terlewat!"
        }

        _uiState.value = state.copy(
            score = newScore,
            correctAnswersCount = newCorrectCount,
            incorrectAnswersCount = newIncorrectCount,
            difficultyLevel = newLevel,
            consecutiveCorrect = newConsecutiveCorrect,
            consecutiveIncorrect = newConsecutiveIncorrect,
            roundResolved = true,
            roundFeedbackMsg = feedbackMsg,
            lastIsCorrect = isCorrect,
            lastScoreEarned = roundScore,
            lastWasFast = isFast
        )

        audioSpeaker?.stop()
        if (isCorrect) {
            audioSpeaker?.speak("Bagus! Huruf ${state.targetLetter} tertangkap!")
        } else if (chosenChar.isNotEmpty()) {
            audioSpeaker?.speak("Itu huruf $chosenChar. Cari huruf ${state.targetLetter} ya!")
        } else {
            audioSpeaker?.speak("Huruf ${state.targetLetter} terlewat!")
        }

        viewModelScope.launch {
            val childId = getActiveChildId()
            val answerLog = AnswerLog(
                answerId = "ans_${sessionId}_round_${currentRound}",
                sessionId = sessionId,
                childId = childId,
                moduleId = "pulau_alfabet",
                activityId = "tangkap_huruf",
                questionId = "tangkap_${currentRound}_${state.targetLetter}",
                targetChar = state.targetLetter,
                chosenChar = chosenChar,
                isCorrect = isCorrect,
                responseTimeMs = responseTimeMs,
                timestamp = System.currentTimeMillis()
            )
            sessionRepository.recordAnswer(answerLog)
        }
    }

    fun nextRoundOrFinish() {
        val state = _uiState.value
        if (!state.roundResolved) return

        if (state.currentRound < state.totalRounds) {
            prepareRound(state.currentRound + 1)
        } else {
            completeSession()
        }
    }

    private fun completeSession() {
        if (isSessionCommitted) return
        isSessionCommitted = true

        audioSpeaker?.stop()

        val state = _uiState.value
        val total = state.totalRounds
        val correct = state.correctAnswersCount
        val incorrect = state.incorrectAnswersCount
        val accuracy = if (total > 0) correct.toFloat() / total.toFloat() else 0f

        val stars = when {
            accuracy >= 0.90f -> 3
            accuracy >= 0.70f -> 2
            else -> 1
        }

        val completedAtMs = System.currentTimeMillis()
        val result = SessionResult(
            sessionId = sessionId,
            childId = getActiveChildId(),
            moduleId = "pulau_alfabet",
            activityId = "tangkap_huruf",
            startedAt = startedAtMs,
            completedAt = completedAtMs,
            totalQuestions = total,
            correctAnswers = correct,
            incorrectAnswers = incorrect,
            accuracy = accuracy,
            status = SessionStatus.COMPLETED
        )

        viewModelScope.launch {
            sessionRepository.saveSessionResult(result)
            AksaUserState.hurufMiripCompleted = true

            val childId = getActiveChildId()
            val completedSessions = sessionRepository.getCompletedSessions(childId)
            val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

            val distinctActiveDaysInWeek = completedSessions
                .filter { it.isCompleted && it.completedAt != null }
                .mapNotNull { s ->
                    val date = Instant.ofEpochMilli(s.completedAt!!)
                        .atZone(ZoneId.of("Asia/Jakarta"))
                        .toLocalDate()
                    if (!date.isBefore(monday) && !date.isAfter(monday.plusDays(6))) date else null
                }.toSet().size

            val awarded = evaluateWeeklyMilestonesUseCase(
                childId = childId,
                weekStartDate = monday,
                activeDaysCount = distinctActiveDaysInWeek
            )

            _uiState.value = _uiState.value.copy(
                stage = MiniGameStage.RESULTS,
                accuracy = accuracy,
                starRating = stars,
                sessionResult = result,
                newlyAwardedMilestones = awarded
            )

            audioSpeaker?.speak("Hore! Permainan selesai!")
        }
    }

    fun abandonSession() {
        if (isSessionCommitted) return
        isSessionCommitted = true

        audioSpeaker?.stop()

        val state = _uiState.value
        val result = SessionResult(
            sessionId = sessionId,
            childId = getActiveChildId(),
            moduleId = "pulau_alfabet",
            activityId = "tangkap_huruf",
            startedAt = startedAtMs,
            completedAt = null,
            totalQuestions = state.totalRounds,
            correctAnswers = state.correctAnswersCount,
            incorrectAnswers = state.incorrectAnswersCount,
            accuracy = 0f,
            status = SessionStatus.ABANDONED
        )

        viewModelScope.launch {
            sessionRepository.saveSessionResult(result)
        }
    }

    private fun getActiveChildId(): String {
        if (com.aksa.app.ui.state.AksaUserState.currentProfileId.isNotBlank()) {
            return com.aksa.app.ui.state.AksaUserState.currentProfileId
        }
        val guardian = authRepository.currentGuardian.value
        val guardianUid = guardian?.uid ?: "demo_guardian_default"
        return "child_$guardianUid"
    }

    override fun onCleared() {
        audioSpeaker?.stop()
    }
}
