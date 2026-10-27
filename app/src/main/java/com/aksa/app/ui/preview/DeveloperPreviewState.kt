package com.aksa.app.ui.preview

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PreviewVisualState {
    DEFAULT,
    IDLE,
    CORRECT_FEEDBACK,
    INCORRECT_FEEDBACK,
    SESSION_COMPLETED
}

enum class LevelMapPreviewState {
    DEFAULT_PROGRESSION,
    ALL_UNLOCKED,
    ALL_COMPLETED,
    ALL_LOCKED_EXCEPT_LEVEL_1
}

/**
 * In-memory manager for Developer Preview Mode.
 * Ensures preview states can be tested and photographed without affecting real user records.
 */
object DeveloperPreviewManager {
    private val _isPreviewActive = MutableStateFlow(false)
    val isPreviewActive: StateFlow<Boolean> = _isPreviewActive.asStateFlow()

    private val _visualState = MutableStateFlow(PreviewVisualState.DEFAULT)
    val visualState: StateFlow<PreviewVisualState> = _visualState.asStateFlow()

    private val _levelMapState = MutableStateFlow(LevelMapPreviewState.DEFAULT_PROGRESSION)
    val levelMapState: StateFlow<LevelMapPreviewState> = _levelMapState.asStateFlow()

    private val _previewWeeklyDays = MutableStateFlow(0)
    val previewWeeklyDays: StateFlow<Int> = _previewWeeklyDays.asStateFlow()

    fun enablePreview(enabled: Boolean) {
        _isPreviewActive.value = enabled
        if (!enabled) {
            reset()
        }
    }

    fun setVisualState(state: PreviewVisualState) {
        _visualState.value = state
    }

    fun setLevelMapState(state: LevelMapPreviewState) {
        _levelMapState.value = state
    }

    fun setPreviewWeeklyDays(days: Int) {
        _previewWeeklyDays.value = days.coerceIn(0, 7)
    }

    fun reset() {
        _visualState.value = PreviewVisualState.DEFAULT
        _levelMapState.value = LevelMapPreviewState.DEFAULT_PROGRESSION
        _previewWeeklyDays.value = 0
    }
}
