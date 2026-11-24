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
import com.aksa.app.domain.usecase.AnalyzeErrorPatternsUseCase
import com.aksa.app.domain.usecase.EvaluateWeeklyMilestonesUseCase
import com.aksa.app.domain.usecase.GenerateHurufMiripQuestionsUseCase
import com.aksa.app.domain.usecase.GetActiveChildIdUseCase
import com.aksa.app.domain.usecase.PairConfusionStat
import com.aksa.app.ui.state.AksaUserState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.UUID

data class HurufMiripUiState(
    val sessionId: String = "",
    val isLoading: Boolean = true,
    val priorityLetters: List<String> = emptyList(),
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
    val sessionPairStats: List<PairConfusionStat> = emptyList(),
    val newlyAwardedMilestones: List<Int> = emptyList(),
    val isTtsReady: Boolean = false,
    val currentDifficulty: Int = 1,
    val consecutiveCorrect: Int = 0,
    val consecutiveIncorrect: Int = 0
) {
    val currentQuestion: HurufMiripQuestion?
        get() = questions.getOrNull(currentQuestionIndex)

    // Backwards-compatible alias for UI components checking weakTargets
    val weakTargets: List<String> get() = priorityLetters
}

class HurufMiripViewModel(
    private val audioSpeaker: AksaAudioSpeaker? = null,
    private val sessionRepository: SessionRepository = AksaRepositoryProvider.sessionRepository,
    private val achievementRepository: AchievementRepository = AksaRepositoryProvider.achievementRepository,
    private val authRepository: AuthRepository = AksaRepositoryProvider.authRepository,
    private val profileRepository: ProfileRepository = AksaRepositoryProvider.profileRepository,
    private val questionGenerator: GenerateHurufMiripQuestionsUseCase = GenerateHurufMiripQuestionsUseCase(),
    private val analyzeErrorPatternsUseCase: AnalyzeErrorPatternsUseCase = AnalyzeErrorPatternsUseCase(sessionRepository),
    private val evaluateWeeklyMilestonesUseCase: EvaluateWeeklyMilestonesUseCase = EvaluateWeeklyMilestonesUseCase(achievementRepository),
    private val getActiveChildIdUseCase: GetActiveChildIdUseCase = GetActiveChildIdUseCase(authRepository, profileRepository)
) : ViewModel() {

    private val sessionId = "session_${UUID.randomUUID()}"
    private val startedAtMs = System.currentTimeMillis()
    private var questionDisplayedAtMs: Long = startedAtMs
    private var isSessionCommitted = false
    private val currentSessionLogs = mutableListOf<AnswerLog>()
    private val pendingAnswerJobs = mutableListOf<Job>()
    private val sessionChildId: String = getActiveChildId()

    private val _uiState = MutableStateFlow(
        HurufMiripUiState(
            sessionId = sessionId,
            isLoading = true
        )
    )
    val uiState: StateFlow<HurufMiripUiState> = _uiState.asStateFlow()

    init {
        // Observe TTS readiness if speaker provided
        if (audioSpeaker != null) {
            viewModelScope.launch {
                audioSpeaker.isReady.collect { ready ->
                    _uiState.value = _uiState.value.copy(isTtsReady = ready)
                }
            }
        }

        // Jalankan analisis pola kesalahan dari 3 sesi terakhir sebelum menyusun soal
        viewModelScope.launch {
            val analysisResult = analyzeErrorPatternsUseCase(sessionChildId)
            val generatedQuestions = questionGenerator(analysisResult.priorityLetters)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                priorityLetters = analysisResult.priorityLetters,
                questions = generatedQuestions,
                totalQuestions = generatedQuestions.size
            )

            questionDisplayedAtMs = System.currentTimeMillis()
            playCurrentQuestionAudio()
        }
    }

    fun playCurrentQuestionAudio() {
        val q = _uiState.value.currentQuestion ?: return
        audioSpeaker?.speak(q.audioPrompt)
    }

    fun selectChoice(choice: String) {
        val state = _uiState.value
        if (state.isLoading || state.isAnswered) return // Prevent multiple answers on same question

        val question = state.currentQuestion ?: return
        val isCorrect = choice == question.targetLetter
        val responseTimeMs = (System.currentTimeMillis() - questionDisplayedAtMs).coerceAtLeast(0)

        val newCorrectCount = if (isCorrect) state.correctAnswersCount + 1 else state.correctAnswersCount
        val newIncorrectCount = if (!isCorrect) state.incorrectAnswersCount + 1 else state.incorrectAnswersCount

        // In-game adaptive difficulty progression (matching DeveloperPreviewAndAdaptiveLogicTest)
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

        val answerLog = AnswerLog(
            answerId = "ans_${sessionId}_${question.id}",
            sessionId = sessionId,
            childId = sessionChildId,
            moduleId = "pulau_alfabet",
            activityId = "huruf_mirip",
            questionId = question.id,
            targetChar = question.targetLetter,
            chosenChar = choice,
            isCorrect = isCorrect,
            responseTimeMs = responseTimeMs,
            timestamp = System.currentTimeMillis()
        )
        currentSessionLogs.add(answerLog)

        // Persist AnswerLog idempotently and track job
        val job = viewModelScope.launch {
            sessionRepository.recordAnswer(answerLog)
        }
        synchronized(pendingAnswerJobs) {
            pendingAnswerJobs.add(job)
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

        // Hitung breakdown benar & salah per pasangan huruf (b/d dan p/q) untuk sesi ini
        val sessionAnalysis = analyzeErrorPatternsUseCase.analyzeLogs(currentSessionLogs)

        val result = SessionResult(
            sessionId = sessionId,
            childId = sessionChildId,
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
            val jobsToWait = synchronized(pendingAnswerJobs) { pendingAnswerJobs.toList() }
            jobsToWait.joinAll()

            sessionRepository.saveSessionResult(result)
            AksaUserState.hurufMiripCompleted = true

            // Calculate active days in current week and evaluate milestones
            val completedSessions = sessionRepository.getCompletedSessions(sessionChildId)
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
                childId = sessionChildId,
                weekStartDate = monday,
                activeDaysCount = distinctActiveDaysInWeek
            )

            _uiState.value = _uiState.value.copy(
                showCompletionDialog = true,
                sessionResult = result,
                sessionPairStats = sessionAnalysis.pairStats,
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
            childId = sessionChildId,
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
            val jobsToWait = synchronized(pendingAnswerJobs) { pendingAnswerJobs.toList() }
            jobsToWait.joinAll()
            sessionRepository.saveSessionResult(result)
        }
    }

    private fun getActiveChildId(): String {
        return try {
            getActiveChildIdUseCase.getSync()
        } catch (_: Exception) {
            if (com.aksa.app.ui.state.AksaUserState.currentProfileId.isNotBlank()) {
                com.aksa.app.ui.state.AksaUserState.currentProfileId
            } else {
                val guardian = authRepository.currentGuardian.value
                val guardianUid = guardian?.uid ?: ""
                if (guardianUid.isNotBlank()) "child_$guardianUid" else ""
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioSpeaker?.stop()
    }
}
