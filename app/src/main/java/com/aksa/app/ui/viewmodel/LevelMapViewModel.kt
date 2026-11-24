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
    val practiceDaysRequirement: String = "Latihan minimal 2 hari berbeda dalam 7 hari terakhir",
    val recommendedLevelNumber: Int = (levelNumber - 1).coerceAtLeast(1)
)

data class LevelMapUiState(
    val isLoading: Boolean = false,
    val childId: String = "",
    val progress: LevelMapProgress? = null,
    val effectiveStatus1: LevelNodeStatus = LevelNodeStatus.ACTIVE,
    val effectiveStatus2: LevelNodeStatus = LevelNodeStatus.LOCKED,
    val effectiveStatus3: LevelNodeStatus = LevelNodeStatus.LOCKED,
    val effectiveStatus4: LevelNodeStatus = LevelNodeStatus.LOCKED,
    val selectedLockedLevel: LockedLevelDetails? = null
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
     * Cancels any ongoing observer job to prevent stale listeners when profiles switch.
     */
    fun onProfileChanged() {
        startObserving()
    }

    private fun startObserving() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            val childId = try {
                getActiveChildIdUseCase()
            } catch (e: Exception) {
                ""
            }

            _uiState.value = _uiState.value.copy(childId = childId, isLoading = true)

            // Reactively combine session changes and progression repository
            combine(
                sessionRepository.observeCompletedSessions(childId),
                levelProgressRepository.observeUnlockedLevels(childId)
            ) { _, _ ->
                updateProgressionState(childId)
            }.collect {}
        }
    }

    suspend fun refreshProgression() {
        val childId = _uiState.value.childId.ifEmpty {
            try { getActiveChildIdUseCase() } catch (e: Exception) { "" }
        }
        updateProgressionState(childId)
    }

    private suspend fun updateProgressionState(childId: String) {
        val realProgress = try {
            if (childId.isNotBlank()) getLevelProgressionUseCase(childId) else null
        } catch (e: Exception) {
            null
        }

        val s1 = realProgress?.getNodeStatus(1) ?: LevelNodeStatus.ACTIVE
        val s2 = realProgress?.getNodeStatus(2) ?: LevelNodeStatus.LOCKED
        val s3 = realProgress?.getNodeStatus(3) ?: LevelNodeStatus.LOCKED
        val s4 = realProgress?.getNodeStatus(4) ?: LevelNodeStatus.LOCKED

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            childId = childId,
            progress = realProgress,
            effectiveStatus1 = s1,
            effectiveStatus2 = s2,
            effectiveStatus3 = s3,
            effectiveStatus4 = s4
        )
    }

    fun onLevelClick(levelNumber: Int, onNavigate: () -> Unit) {
        val state = _uiState.value
        val status = when (levelNumber) {
            1 -> state.effectiveStatus1
            2 -> state.effectiveStatus2
            3 -> state.effectiveStatus3
            4 -> state.effectiveStatus4
            else -> LevelNodeStatus.LOCKED
        }

        if (status == LevelNodeStatus.LOCKED) {
            // Show friendly explanation modal
            _uiState.value = state.copy(
                selectedLockedLevel = buildLockedLevelDetails(levelNumber, state.progress)
            )
        } else {
            // Level is unlocked / completed -> permit navigation
            onNavigate()
        }
    }

    fun dismissLockedDialog() {
        _uiState.value = _uiState.value.copy(selectedLockedLevel = null)
    }

    private fun buildLockedLevelDetails(levelNumber: Int, progress: LevelMapProgress?): LockedLevelDetails {
        val (title, prereqNumber, prereqTitle) = when (levelNumber) {
            2 -> Triple("Dunia Suku Kata", 1, "Pulau Alfabet")
            3 -> Triple("Gunung Kata", 2, "Dunia Suku Kata")
            4 -> Triple("Sungai Cerita", 3, "Gunung Kata")
            else -> Triple("Level $levelNumber", 1, "Pulau Alfabet")
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
            recommendedLevelNumber = prereqNumber
        )
    }
}

