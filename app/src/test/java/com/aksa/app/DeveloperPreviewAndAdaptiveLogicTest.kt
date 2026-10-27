package com.aksa.app

import com.aksa.app.ui.preview.DeveloperPreviewManager
import com.aksa.app.ui.preview.LevelMapPreviewState
import com.aksa.app.ui.preview.PreviewVisualState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeveloperPreviewAndAdaptiveLogicTest {

    @Before
    fun setUp() {
        DeveloperPreviewManager.enablePreview(false)
    }

    @Test
    fun testDeveloperPreviewManagerDefaults() {
        assertFalse(DeveloperPreviewManager.isPreviewActive.value)
        assertEquals(PreviewVisualState.DEFAULT, DeveloperPreviewManager.visualState.value)
        assertEquals(LevelMapPreviewState.DEFAULT_PROGRESSION, DeveloperPreviewManager.levelMapState.value)
        assertEquals(0, DeveloperPreviewManager.previewWeeklyDays.value)
    }

    @Test
    fun testDeveloperPreviewManagerUpdatesAndReset() {
        DeveloperPreviewManager.enablePreview(true)
        assertTrue(DeveloperPreviewManager.isPreviewActive.value)

        DeveloperPreviewManager.setVisualState(PreviewVisualState.CORRECT_FEEDBACK)
        assertEquals(PreviewVisualState.CORRECT_FEEDBACK, DeveloperPreviewManager.visualState.value)

        DeveloperPreviewManager.setLevelMapState(LevelMapPreviewState.ALL_UNLOCKED)
        assertEquals(LevelMapPreviewState.ALL_UNLOCKED, DeveloperPreviewManager.levelMapState.value)

        DeveloperPreviewManager.setPreviewWeeklyDays(7)
        assertEquals(7, DeveloperPreviewManager.previewWeeklyDays.value)

        DeveloperPreviewManager.reset()
        assertEquals(PreviewVisualState.DEFAULT, DeveloperPreviewManager.visualState.value)
        assertEquals(LevelMapPreviewState.DEFAULT_PROGRESSION, DeveloperPreviewManager.levelMapState.value)
        assertEquals(0, DeveloperPreviewManager.previewWeeklyDays.value)

        DeveloperPreviewManager.enablePreview(false)
        assertFalse(DeveloperPreviewManager.isPreviewActive.value)
    }

    @Test
    fun testAdaptiveDifficultyProgression() {
        var currentDifficulty = 1
        var consecutiveCorrect = 0
        var consecutiveIncorrect = 0

        fun onAnswer(isCorrect: Boolean) {
            if (isCorrect) {
                consecutiveCorrect++
                consecutiveIncorrect = 0
                if (consecutiveCorrect >= 3) {
                    if (currentDifficulty < 3) currentDifficulty++
                    consecutiveCorrect = 0
                }
            } else {
                consecutiveIncorrect++
                consecutiveCorrect = 0
                if (consecutiveIncorrect >= 2) {
                    if (currentDifficulty > 1) currentDifficulty--
                    consecutiveIncorrect = 0
                }
            }
        }

        // 3 consecutive correct -> level up to 2
        onAnswer(true)
        onAnswer(true)
        assertEquals(1, currentDifficulty)
        onAnswer(true)
        assertEquals(2, currentDifficulty)

        // 3 consecutive correct -> level up to 3
        onAnswer(true)
        onAnswer(true)
        onAnswer(true)
        assertEquals(3, currentDifficulty)

        // Cap at 3
        onAnswer(true)
        onAnswer(true)
        onAnswer(true)
        assertEquals(3, currentDifficulty)

        // 2 consecutive incorrect -> level down to 2
        onAnswer(false)
        assertEquals(3, currentDifficulty)
        onAnswer(false)
        assertEquals(2, currentDifficulty)

        // 2 consecutive incorrect -> level down to 1
        onAnswer(false)
        onAnswer(false)
        assertEquals(1, currentDifficulty)

        // Floor at 1
        onAnswer(false)
        onAnswer(false)
        assertEquals(1, currentDifficulty)
    }
}
