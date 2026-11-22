package com.aksa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksa.app.audio.AksaAudioSpeaker
import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.model.HurufMiripQuestion
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.repository.AchievementRepository
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.ProfileRepository
import com.aksa.app.domain.repository.SessionRepository
import com.aksa.app.domain.usecase.AnalyzeTargetErrorsUseCase
import com.aksa.app.domain.usecase.EvaluateWeeklyMilestonesUseCase
import com.aksa.app.domain.usecase.GenerateHurufMiripQuestionsUseCase
import com.aksa.app.domain.usecase.GetActiveChildIdUseCase
import com.aksa.app.ui.state.AksaUserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.UUID

data class HurufMiripUiState(
    val sessionId: String = "",
    val questions: List<HurufMiripQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedChoice: String? = null,
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val totalQuestions: Int = 10,
    val correctAnswersCount: Int = 0,
    val incorrectAnswersCount: Int = 0,
    val showCompletionDialog: Boolean = false,
    val sessionResult: SessionResult? = null,
    val newlyAwardedMilestones: List<Int> = emptyList(),
    val isTtsReady: Boolean = false,
    val currentDifficulty: Int = 1,
    val consecutiveCorrect: Int = 0,
    val consecutiveIncorrect: Int = 0,
    val weakTargets: List<String> = emptyList()
) {
    val currentQuestion: HurufMiripQuestion?
        get() = questions.getOrNull(currentQuestionIndex)
}

class HurufMiripViewModel(
    private val audioSpeaker: AksaAudioSpeaker? = null,
    private val sessionRepository: SessionRepository = AksaRepositoryProvider.sessionRepository,
    private val achievementRepository: AchievementRepository = AksaRepositoryProvider.achievementRepository,
    private val authRepository: AuthRepository = AksaRepositoryProvider.authRepository,
    private val profileRepository: ProfileRepository = AksaRepositoryProvider.profileRepository,
    private val questionGenerator: GenerateHurufMiripQuestionsUseCase = GenerateHurufMiripQuestionsUseCase(),
    private val evaluateWeeklyMilestonesUseCase: EvaluateWeeklyMilestonesUseCase = EvaluateWeeklyMilestonesUseCase(achievementRepository),
    private val getActiveChildIdUseCase: GetActiveChildIdUseCase = GetActiveChildIdUseCase(authRepository, profileRepository),
    private val analyzeTargetErrorsUseCase: AnalyzeTargetErrorsUseCase = AnalyzeTargetErrorsUseCase(sessionRepository)
) : ViewModel() {

    private val sessionId = "session_${UUID.randomUUID()}"
    private val startedAtMs = System.currentTimeMillis()
    private var questionDisplayedAtMs: Long = startedAtMs
    private var isSessionCommitted = false

    private val _uiState = MutableStateFlow(
        HurufMiripUiState(
            sessionId = sessionId,
            questions = questionGenerator()
        )
    )
    val uiState: StateFlow<HurufMiripUiState> = _uiState.asStateFlow()

    init {
        questionDisplayedAtMs = System.currentTimeMillis()
        playCurrentQuestionAudio()

        // Load adaptive questions based on past performance
        viewModelScope.launch {
            loadAdaptiveQuestions()
        }

        // Observe TTS readiness if speaker provided
        if (audioSpeaker != null) {
            viewModelScope.launch {
                audioSpeaker.isReady.collect { ready ->
                    _uiState.value = _uiState.value.copy(isTtsReady = ready)
                }
            }
        }
    }

    private suspend fun loadAdaptiveQuestions() {
        try {
            val childId = getActiveChildId()
            val report = analyzeTargetErrorsUseCase(childId, moduleId = "pulau_alfabet", activityId = "huruf_mirip")
            if (report.weakTargets.isNotEmpty()) {
                val currentState = _uiState.value
                // Only update questions if the child has not started answering yet
                if (currentState.currentQuestionIndex == 0 && !currentState.isAnswered) {
                    val adaptiveQuestions = questionGenerator(report.weakTargets)
                    _uiState.value = currentState.copy(
                        questions = adaptiveQuestions,
                        weakTargets = report.weakTargets
                    )
                    playCurrentQuestionAudio()
                } else {
                    _uiState.value = currentState.copy(weakTargets = report.weakTargets)
                }
            }
        } catch (_: Exception) {
            // Silently retain standard question set if loading adaptive targets fails
        }
    }

    fun playCurrentQuestionAudio() {
        val q = _uiState.value.currentQuestion ?: return
        audioSpeaker?.speak(q.audioPrompt)
    }

    fun selectChoice(choice: String) {
        val state = _uiState.value
        if (state.isAnswered) return // Prevent multiple answers on same question

        val question = state.currentQuestion ?: return
        val isCorrect = choice == question.targetLetter
        val responseTimeMs = (System.currentTimeMillis() - questionDisplayedAtMs).coerceAtLeast(0)

        val newCorrectCount = if (isCorrect) state.correctAnswersCount + 1 else state.correctAnswersCount
        val newIncorrectCount = if (!isCorrect) state.incorrectAnswersCount + 1 else state.incorrectAnswersCount

        // Adaptive difficulty progression (matching DeveloperPreviewAndAdaptiveLogicTest specifications)
        // 3 consecutive correct -> level up by 1 (max 3), reset consecutiveCorrect to 0
        // 2 consecutive incorrect -> level down by 1 (min 1), reset consecutiveIncorrect to 0
        val newConsecutiveCorrect: Int
        val newConsecutiveIncorrect: Int
        var newDifficulty = state.currentDifficulty

        if (isCorrect) {
            val count = state.consecutiveCorrect + 1
            newConsecutiveIncorrect = 0
            if (count >= 3) {
                if (newDifficulty < 3) newDifficulty++
                newConsecutiveCorrect = 0
            } else {
                newConsecutiveCorrect = count
            }
        } else {
            val count = state.consecutiveIncorrect + 1
            newConsecutiveCorrect = 0
            if (count >= 2) {
                if (newDifficulty > 1) newDifficulty--
                newConsecutiveIncorrect = 0
            } else {
                newConsecutiveIncorrect = count
            }
        }

        _uiState.value = state.copy(
            selectedChoice = choice,
            isAnswered = true,
            isCorrect = isCorrect,
            correctAnswersCount = newCorrectCount,
            incorrectAnswersCount = newIncorrectCount,
            currentDifficulty = newDifficulty,
            consecutiveCorrect = newConsecutiveCorrect,
            consecutiveIncorrect = newConsecutiveIncorrect
        )

        // Persist AnswerLog idempotently
        viewModelScope.launch {
            val childId = getActiveChildId()
            val answerLog = AnswerLog(
                answerId = "ans_${sessionId}_${question.id}",
                sessionId = sessionId,
                childId = childId,
                moduleId = "pulau_alfabet",
                activityId = "huruf_mirip",
                questionId = question.id,
                targetChar = question.targetLetter,
                chosenChar = choice,
                isCorrect = isCorrect,
                responseTimeMs = responseTimeMs,
                timestamp = System.currentTimeMillis()
            )
            sessionRepository.recordAnswer(answerLog)
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (!state.isAnswered) return

        if (state.currentQuestionIndex < state.questions.size - 1) {
            _uiState.value = state.copy(
                currentQuestionIndex = state.currentQuestionIndex + 1,
                selectedChoice = null,
                isAnswered = false,
                isCorrect = false
            )
            questionDisplayedAtMs = System.currentTimeMillis()
            playCurrentQuestionAudio()
        } else {
            // Reached end of 10th question -> Finalize and complete session
            completeSession()
        }
    }

    private fun completeSession() {
        if (isSessionCommitted) return
        isSessionCommitted = true

        val state = _uiState.value
        val completedAtMs = System.currentTimeMillis()
        val total = state.questions.size
        val correct = state.correctAnswersCount
        val incorrect = state.incorrectAnswersCount
        val accuracy = if (total > 0) correct.toFloat() / total.toFloat() else 0f

        val result = SessionResult(
            sessionId = sessionId,
            childId = getActiveChildId(),
            moduleId = "pulau_alfabet",
            activityId = "huruf_mirip",
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

            // Calculate active days in current week and evaluate milestones
            val childId = getActiveChildId()
            val completedSessions = sessionRepository.getCompletedSessions(childId)
            val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))

            val distinctActiveDaysInWeek = completedSessions
                .filter { it.isCompleted && it.completedAt != null }
                .mapNotNull { s ->
                    val date = java.time.Instant.ofEpochMilli(s.completedAt!!)
                        .atZone(java.time.ZoneId.of("Asia/Jakarta"))
                        .toLocalDate()
                    if (!date.isBefore(monday) && !date.isAfter(monday.plusDays(6))) date else null
                }.toSet().size

            val awarded = evaluateWeeklyMilestonesUseCase(
                childId = childId,
                weekStartDate = monday,
                activeDaysCount = distinctActiveDaysInWeek
            )

            _uiState.value = _uiState.value.copy(
                showCompletionDialog = true,
                sessionResult = result,
                newlyAwardedMilestones = awarded
            )
        }
    }

    fun abandonSession() {
        // If child exits before completing all 10 questions, mark ABANDONED so it does not count as active day
        if (isSessionCommitted) return
        isSessionCommitted = true

        audioSpeaker?.stop()

        val state = _uiState.value
        val result = SessionResult(
            sessionId = sessionId,
            childId = getActiveChildId(),
            moduleId = "pulau_alfabet",
            activityId = "huruf_mirip",
            startedAt = startedAtMs,
            completedAt = null,
            totalQuestions = state.questions.size,
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
        return try {
            getActiveChildIdUseCase.getSync()
        } catch (_: Exception) {
            if (AksaUserState.currentProfileId.isNotBlank()) {
                AksaUserState.currentProfileId
            } else {
                val guardian = authRepository.currentGuardian.value
                val guardianUid = guardian?.uid ?: "demo_guardian_default"
                "child_$guardianUid"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioSpeaker?.stop()
    }
}
