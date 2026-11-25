package com.aksa.app.ui.viewmodel

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.repository.LevelProgressRepository
import com.aksa.app.domain.repository.SessionRepository
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
    val targetAccuracies: String = "Rata-rata akurasi minimal 80%",
    val targetErrorLimit: String = "Tingkat kesalahan materi maksimal 40%",
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

    /**
     * Re-initializes observation for the active child profile.
     * Cancels any ongoing observer job and clears previous profile progress immediately.
     */
    fun onProfileChanged() {
        startObserving()
    }

    private fun startObserving() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            // Reset immediately so previous child's progress is never shown during profile switch
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

            // Evaluate immediately so initial load never stalls if a listener delays or completes empty
            updateProgressionState(childId)

            // Reactively re-evaluate on subsequent session or unlock changes
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
            // Show friendly explanation modal (or surfaced read error, never a misleading diagnosis)
            _uiState.value = state.copy(
                selectedLockedLevel = buildLockedLevelDetails(levelNumber, state.progress, state.errorMessage)
            )
        } else {
            // Level is unlocked / completed -> permit navigation
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

        val explanation = prereqProgress?.readinessResult?.explanation
            ?: "Selesaikan latihan di $prereqTitle terlebih dahulu untuk membuka petualangan ini!"

        return LockedLevelDetails(
            levelNumber = levelNumber,
            title = title,
            prerequisiteTitle = prereqTitle,
            explanation = explanation,
            recommendedLevelNumber = prereqNumber,
            isReadError = false
        )
    }
}
