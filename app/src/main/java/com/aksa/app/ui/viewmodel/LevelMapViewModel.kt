package com.aksa.app.ui.viewmodel

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.LevelReadinessStatus
import com.aksa.app.domain.repository.LevelProgressRepository
import com.aksa.app.domain.repository.SessionRepository
import com.aksa.app.domain.usecase.EvaluateLevelReadinessUseCase
import com.aksa.app.domain.usecase.GetActiveChildIdUseCase
import com.aksa.app.domain.usecase.GetLevelProgressionUseCase
import com.aksa.app.domain.usecase.LevelMapProgress
import com.aksa.app.ui.components.LevelNodeStatus
import com.aksa.app.ui.state.AksaUserState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

data class LockedLevelDetails(
    val levelNumber: Int,
    val title: String,
    val prerequisiteTitle: String,
    val explanation: String,
    val currentSessions: Int = 0,
    val requiredSessions: Int = 3,
    val currentAccuracyPercent: Int = 0,
    val requiredAccuracyPercent: Int = 80,
    val hasLetterErrorRateCheck: Boolean = false,
    val isErrorRateMet: Boolean = false,
    val errorRateSummary: String = "Selesaikan minimal 3 sesi dulu",
    val targetAccuracies: String = "Rata-rata akurasi 3 sesi terakhir minimal 80%",
    val targetErrorLimit: String = "Kesalahan huruf mirip (b/d/p/q/m/n/u/w) maks. 40%",
    val recommendedLevelNumber: Int = (levelNumber - 1).coerceAtLeast(1),
    val isReadError: Boolean = false
)

data class LevelMapUiState(
    val isLoading: Boolean = false,
    val childId: String = "",
    val progress: LevelMapProgress? = null,
    val effectiveStatus1: LevelNodeStatus = LevelNodeStatus.ACTIVE,
    val effectiveStatus2: LevelNodeStatus = LevelNodeStatus.LOCKED,
    val effectiveStatus3: LevelNodeStatus = LevelNodeStatus.LOCKED,
    val effectiveStatus4: LevelNodeStatus = LevelNodeStatus.LOCKED,
    val selectedLockedLevel: LockedLevelDetails? = null,
    val errorMessage: String? = null
)

class LevelMapViewModel(
    private val getActiveChildIdUseCase: GetActiveChildIdUseCase = GetActiveChildIdUseCase(),
    private val getLevelProgressionUseCase: GetLevelProgressionUseCase = GetLevelProgressionUseCase(),
    private val sessionRepository: SessionRepository = AksaRepositoryProvider.sessionRepository,
    private val levelProgressRepository: LevelProgressRepository = AksaRepositoryProvider.levelProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LevelMapUiState(isLoading = true))
    val uiState: StateFlow<LevelMapUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        viewModelScope.launch {
            snapshotFlow { AksaUserState.currentProfileId }
                .distinctUntilChanged()
                .collect {
                    startObserving()
                }
        }
    }

    fun onProfileChanged() {
        startObserving()
    }

    private fun startObserving() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.value = LevelMapUiState(isLoading = true)

            val childId = try {
                getActiveChildIdUseCase()
            } catch (e: Exception) {
                _uiState.value = LevelMapUiState(
                    isLoading = false,
                    childId = "",
                    errorMessage = e.localizedMessage?.takeIf { it.isNotBlank() }
                        ?: "Pilih profil anak terlebih dahulu sebelum membuka peta level."
                )
                return@launch
            }

            _uiState.value = LevelMapUiState(
                isLoading = true,
                childId = childId
            )

            updateProgressionState(childId)

            combine(
                sessionRepository.observeCompletedSessions(childId),
                levelProgressRepository.observeUnlockedLevels(childId)
            ) { _, _ ->
                childId
            }
                .catch { e ->
                    val currentActive = AksaUserState.currentProfileId
                    if (currentActive.isBlank() || currentActive == childId) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            childId = childId,
                            errorMessage = e.localizedMessage?.takeIf { it.isNotBlank() }
                                ?: "Gagal memuat progres belajar. Periksa koneksi internet dan coba lagi."
                        )
                    }
                }
                .collect { activeId ->
                    updateProgressionState(activeId)
                }
        }
    }

    suspend fun refreshProgression() {
        val resolvedChildId = try {
            getActiveChildIdUseCase()
        } catch (e: Exception) {
            _uiState.value = LevelMapUiState(
                isLoading = false,
                childId = "",
                errorMessage = e.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Pilih profil anak terlebih dahulu sebelum membuka peta level."
            )
            return
        }

        if (resolvedChildId != _uiState.value.childId && _uiState.value.childId.isNotBlank()) {
            startObserving()
            return
        }

        updateProgressionState(resolvedChildId)
    }

    private suspend fun updateProgressionState(childId: String) {
        if (childId.isBlank()) {
            _uiState.value = LevelMapUiState(
                isLoading = false,
                childId = "",
                errorMessage = "Pilih profil anak terlebih dahulu."
            )
            return
        }

        try {
            val realProgress = getLevelProgressionUseCase(childId)
            val currentActive = AksaUserState.currentProfileId
            if (currentActive.isNotBlank() && currentActive != childId) return

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                childId = childId,
                progress = realProgress,
                effectiveStatus1 = realProgress.getNodeStatus(1),
                effectiveStatus2 = realProgress.getNodeStatus(2),
                effectiveStatus3 = realProgress.getNodeStatus(3),
                effectiveStatus4 = realProgress.getNodeStatus(4),
                errorMessage = null
            )
        } catch (e: Exception) {
            val currentActive = AksaUserState.currentProfileId
            if (currentActive.isNotBlank() && currentActive != childId) return

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                childId = childId,
                errorMessage = e.localizedMessage?.takeIf { it.isNotBlank() }
                    ?: "Gagal membaca progres level dari Firestore. Periksa koneksi atau izin akses data."
            )
        }
    }

    fun onLevelClick(levelNumber: Int, onNavigate: () -> Unit) {
        val state = _uiState.value
        if (state.isLoading) return

        val status = when (levelNumber) {
            1 -> state.effectiveStatus1
            2 -> state.effectiveStatus2
            3 -> state.effectiveStatus3
            4 -> state.effectiveStatus4
            else -> LevelNodeStatus.LOCKED
        }

        if (status == LevelNodeStatus.LOCKED) {
            _uiState.value = state.copy(
                selectedLockedLevel = buildLockedLevelDetails(levelNumber, state.progress, state.errorMessage)
            )
        } else {
            onNavigate()
        }
    }

    fun dismissLockedDialog() {
        _uiState.value = _uiState.value.copy(selectedLockedLevel = null)
    }

    private fun buildLockedLevelDetails(
        levelNumber: Int,
        progress: LevelMapProgress?,
        errorMessage: String?
    ): LockedLevelDetails {
        val (title, prereqNumber, prereqTitle) = when (levelNumber) {
            2 -> Triple("Dunia Suku Kata", 1, "Pulau Alfabet")
            3 -> Triple("Gunung Kata", 2, "Dunia Suku Kata")
            4 -> Triple("Sungai Cerita", 3, "Gunung Kata")
            else -> Triple("Level $levelNumber", 1, "Pulau Alfabet")
        }

        if (errorMessage != null) {
            return LockedLevelDetails(
                levelNumber = levelNumber,
                title = title,
                prerequisiteTitle = prereqTitle,
                explanation = errorMessage,
                recommendedLevelNumber = prereqNumber,
                isReadError = true
            )
        }

        val prereqProgress = when (prereqNumber) {
            1 -> progress?.level1
            2 -> progress?.level2
            3 -> progress?.level3
            else -> null
        }

        val hasLetterCheck = prereqNumber == 1
        val readiness = prereqProgress?.readinessResult
        val currentSessions = readiness?.sessionCount ?: 0
        val currentAccuracyPercent = ((readiness?.averageAccuracy ?: 0f) * 100).toInt()

        val confusablePerformances = readiness?.targetPerformances?.values
            ?.filter { it.target in EvaluateLevelReadinessUseCase.CONFUSABLE_LETTERS && it.totalAttempts >= 2 }
            .orEmpty()

        val failingLetters = confusablePerformances
            .filter { it.errorRate > 0.40f + 1e-6f }
            .sortedByDescending { it.errorRate }

        val maxErrorRatePercent = confusablePerformances.maxOfOrNull { (it.errorRate * 100).toInt() }

        val isErrorRateMet = hasLetterCheck &&
                readiness != null &&
                readiness.targetPerformances.isNotEmpty() &&
                readiness.status != LevelReadinessStatus.HIGH_ERROR_RATE &&
                readiness.status != LevelReadinessStatus.INSUFFICIENT_EVIDENCE

        val errorRateSummary = when {
            !hasLetterCheck -> ""
            readiness == null || readiness.targetPerformances.isEmpty() ->
                "Selesaikan minimal 3 sesi dengan akurasi ≥ 80% dulu"
            failingLetters.isNotEmpty() -> {
                val detail = failingLetters.joinToString(", ") {
                    "'${it.target}' (${(it.errorRate * 100).toInt()}%)"
                }
                "Perlu dilatih: $detail"
            }
            maxErrorRatePercent != null ->
                "Aman! Kesalahan tertinggi saat ini: $maxErrorRatePercent% (maks. 40%)"
            else ->
                "Tingkat kesalahan huruf mirip maksimal 40%"
        }

        val explanation = readiness?.explanation
            ?: "Selesaikan latihan di $prereqTitle terlebih dahulu untuk membuka petualangan ini!"

        return LockedLevelDetails(
            levelNumber = levelNumber,
            title = title,
            prerequisiteTitle = prereqTitle,
            explanation = explanation,
            currentSessions = currentSessions,
            requiredSessions = 3,
            currentAccuracyPercent = currentAccuracyPercent,
            requiredAccuracyPercent = 80,
            hasLetterErrorRateCheck = hasLetterCheck,
            isErrorRateMet = isErrorRateMet,
            errorRateSummary = errorRateSummary,
            recommendedLevelNumber = prereqNumber,
            isReadError = false
        )
    }
}