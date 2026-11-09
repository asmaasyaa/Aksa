package com.aksa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksa.app.BuildConfig
import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.LevelReadinessStatus
import com.aksa.app.domain.repository.LevelProgressRepository
import com.aksa.app.domain.repository.SessionRepository
import com.aksa.app.domain.usecase.GetActiveChildIdUseCase
import com.aksa.app.domain.usecase.GetLevelProgressionUseCase
import com.aksa.app.domain.usecase.LevelMapProgress
import com.aksa.app.ui.components.LevelNodeStatus
import com.aksa.app.ui.preview.DeveloperPreviewManager
import com.aksa.app.ui.preview.LevelMapPreviewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class LockedLevelDetails(
    val levelNumber: Int,
    val title: String,
    val prerequisiteTitle: String,
    val explanation: String,
    val targetAccuracies: String = "Rata-rata akurasi minimal 80%",
    val targetErrorLimit: String = "Tingkat kesalahan materi maksimal 40%",
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
    val selectedLockedLevel: LockedLevelDetails? = null,
    val isPreviewActive: Boolean = false
)

class LevelMapViewModel(
    private val getActiveChildIdUseCase: GetActiveChildIdUseCase = GetActiveChildIdUseCase(),
    private val getLevelProgressionUseCase: GetLevelProgressionUseCase = GetLevelProgressionUseCase(),
    private val sessionRepository: SessionRepository = AksaRepositoryProvider.sessionRepository,
    private val levelProgressRepository: LevelProgressRepository = AksaRepositoryProvider.levelProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LevelMapUiState(isLoading = true))
    val uiState: StateFlow<LevelMapUiState> = _uiState.asStateFlow()

    init {
        startObserving()
    }

    private fun startObserving() {
        viewModelScope.launch {
            val childId = try {
                getActiveChildIdUseCase()
            } catch (e: Exception) {
                "child_demo_guardian_default"
            }

            _uiState.value = _uiState.value.copy(childId = childId)

            // Reactively combine session changes, progression repository, and dev preview state
            combine(
                sessionRepository.observeCompletedSessions(childId),
                levelProgressRepository.observeUnlockedLevels(childId),
                DeveloperPreviewManager.isPreviewActive,
                DeveloperPreviewManager.levelMapState
            ) { _, _, isPreviewActive, previewMapState ->
                updateProgressionState(childId, isPreviewActive, previewMapState)
            }.collect {}
        }
    }

    suspend fun refreshProgression() {
        val childId = _uiState.value.childId.ifEmpty {
            try { getActiveChildIdUseCase() } catch (e: Exception) { "child_demo_guardian_default" }
        }
        val isPreviewActive = DeveloperPreviewManager.isPreviewActive.value
        val previewMapState = DeveloperPreviewManager.levelMapState.value
        updateProgressionState(childId, isPreviewActive, previewMapState)
    }

    private suspend fun updateProgressionState(
        childId: String,
        isPreviewActive: Boolean,
        previewMapState: LevelMapPreviewState
    ) {
        val realProgress = try {
            getLevelProgressionUseCase(childId)
        } catch (e: Exception) {
            null
        }

        val effectiveDebugPreview = BuildConfig.DEBUG && isPreviewActive

        val (s1, s2, s3, s4) = if (effectiveDebugPreview) {
            when (previewMapState) {
                LevelMapPreviewState.ALL_UNLOCKED ->
                    listOf(LevelNodeStatus.ACTIVE, LevelNodeStatus.ACTIVE, LevelNodeStatus.ACTIVE, LevelNodeStatus.ACTIVE)
                LevelMapPreviewState.ALL_COMPLETED ->
                    listOf(LevelNodeStatus.COMPLETED, LevelNodeStatus.COMPLETED, LevelNodeStatus.COMPLETED, LevelNodeStatus.COMPLETED)
                LevelMapPreviewState.ALL_LOCKED_EXCEPT_LEVEL_1 ->
                    listOf(LevelNodeStatus.ACTIVE, LevelNodeStatus.LOCKED, LevelNodeStatus.LOCKED, LevelNodeStatus.LOCKED)
                LevelMapPreviewState.DEFAULT_PROGRESSION ->
                    listOf(LevelNodeStatus.ACTIVE, LevelNodeStatus.ACTIVE, LevelNodeStatus.LOCKED, LevelNodeStatus.LOCKED)
            }
        } else {
            listOf(
                realProgress?.getNodeStatus(1) ?: LevelNodeStatus.ACTIVE,
                realProgress?.getNodeStatus(2) ?: LevelNodeStatus.LOCKED,
                realProgress?.getNodeStatus(3) ?: LevelNodeStatus.LOCKED,
                realProgress?.getNodeStatus(4) ?: LevelNodeStatus.LOCKED
            )
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            childId = childId,
            progress = realProgress,
            effectiveStatus1 = s1,
            effectiveStatus2 = s2,
            effectiveStatus3 = s3,
            effectiveStatus4 = s4,
            isPreviewActive = effectiveDebugPreview
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

