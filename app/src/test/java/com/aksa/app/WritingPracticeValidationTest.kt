package com.aksa.app

import androidx.compose.ui.geometry.Offset
import com.aksa.app.ui.screens.getNormalizedLetterSegments
import com.aksa.app.ui.screens.validateLetterStrokes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WritingPracticeValidationTest {

    private val canvasWidth = 400f
    private val canvasHeight = 400f

    @Test
    fun validLetterStrokesAreAccepted() {
        val segments = getNormalizedLetterSegments('A')
        val validStrokes = mutableListOf<List<Offset>>()

        segments.forEach { (start, end) ->
            val strokePoints = mutableListOf<Offset>()
            val steps = 10
            for (i in 0..steps) {
                val t = i.toFloat() / steps
                val x = (start.x + (end.x - start.x) * t) * canvasWidth
                val y = (start.y + (end.y - start.y) * t) * canvasHeight
                strokePoints.add(Offset(x, y))
            }
            validStrokes.add(strokePoints)
        }

        val result = validateLetterStrokes('A', validStrokes, canvasWidth, canvasHeight)
        assertTrue("Tracing the guide path of 'A' must be accepted", result.isValid)
        assertEquals("", result.feedback)
    }

    @Test
    fun randomScribblesAreRejected() {
        // Points drawn far off outside letter guide path (e.g. top-left corner box)
        val scribblePoints = listOf(
            Offset(10f, 10f),
            Offset(20f, 15f),
            Offset(30f, 10f),
            Offset(40f, 25f),
            Offset(50f, 10f),
            Offset(60f, 30f),
            Offset(70f, 10f),
            Offset(80f, 25f),
            Offset(90f, 10f),
            Offset(100f, 20f)
        )

        val result = validateLetterStrokes('A', listOf(scribblePoints), canvasWidth, canvasHeight)
        assertFalse("Random scribbles outside letter path must be rejected", result.isValid)
        assertTrue("Feedback must inform off-track stroke", result.feedback.contains("keluar jalur"))
    }

    @Test
    fun incompleteLetterStrokesAreRejected() {
        // Only 1 segment of 'A' (left leg only)
        val segments = getNormalizedLetterSegments('A')
        val firstSegment = segments.first()

        val strokePoints = mutableListOf<Offset>()
        val steps = 10
        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val x = (firstSegment.first.x + (firstSegment.second.x - firstSegment.first.x) * t) * canvasWidth
            val y = (firstSegment.first.y + (firstSegment.second.y - firstSegment.first.y) * t) * canvasHeight
            strokePoints.add(Offset(x, y))
        }

        val result = validateLetterStrokes('A', listOf(strokePoints), canvasWidth, canvasHeight)
        assertFalse("Incomplete letter stroke must be rejected", result.isValid)
        assertTrue("Feedback must inform incomplete letter", result.feedback.contains("belum lengkap"))
    }

    @Test
    fun emptyOrTooFewPointsAreRejected() {
        val result = validateLetterStrokes('A', emptyList(), canvasWidth, canvasHeight)
        assertFalse("Empty stroke list must be rejected", result.isValid)
        assertTrue("Feedback must prompt to write letter", result.feedback.contains("mengikuti garis putus-putus"))
    }

    @Test
    fun reasonableHandwritingVariationsAreAccepted() {
        // Valid letter A with slight offset jitter (+- 8px) representing child handwriting
        val segments = getNormalizedLetterSegments('A')
        val strokesWithJitter = mutableListOf<List<Offset>>()

        segments.forEach { (start, end) ->
            val strokePoints = mutableListOf<Offset>()
            val steps = 10
            for (i in 0..steps) {
                val t = i.toFloat() / steps
                val jitterX = if (i % 2 == 0) 6f else -6f
                val jitterY = if (i % 2 == 0) -6f else 6f
                val x = (start.x + (end.x - start.x) * t) * canvasWidth + jitterX
                val y = (start.y + (end.y - start.y) * t) * canvasHeight + jitterY
                strokePoints.add(Offset(x, y))
            }
            strokesWithJitter.add(strokePoints)
        }

        val result = validateLetterStrokes('A', strokesWithJitter, canvasWidth, canvasHeight)
        assertTrue("Reasonable child handwriting variation with slight wobbliness must be accepted", result.isValid)
    }

    @Test
    fun shiftedAndScaledHandwritingWithinReasonableBoundsIsAccepted() {
        val segments = getNormalizedLetterSegments('A')
        val scale = 0.78f
        val offsetX = 28f
        val offsetY = 18f
        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

        val shiftedScaledStrokes = segments.map { (start, end) ->
            (0..12).map { i ->
                val t = i.toFloat() / 12
                val rawX = (start.x + (end.x - start.x) * t) * canvasWidth
                val rawY = (start.y + (end.y - start.y) * t) * canvasHeight
                Offset(
                    x = center.x + (rawX - center.x) * scale + offsetX,
                    y = center.y + (rawY - center.y) * scale + offsetY
                )
            }
        }

        val result = validateLetterStrokes('A', shiftedScaledStrokes, canvasWidth, canvasHeight)
        assertTrue("Shifted and scaled letter within reasonable bounds must be accepted", result.isValid)
    }

    @Test
    fun excessiveScribbleOverLetterIsRejected() {
        val scribble = (0..40).map { i ->
            val x = 90f + (i % 5) * 55f
            val y = 80f + (i / 5) * 30f
            Offset(x, y)
        }

        val result = validateLetterStrokes('A', listOf(scribble), canvasWidth, canvasHeight)
        assertFalse("Dense scribble across canvas must be rejected", result.isValid)
    }

    @Test
    fun centerZigZagScribbleInsideLetterBoundsIsRejected() {
        // Random zig-zag scribble right inside the letter bounding box (112..288, 80..328)
        val zigZag = listOf(
            Offset(125f, 110f),
            Offset(270f, 130f),
            Offset(120f, 165f),
            Offset(275f, 195f),
            Offset(130f, 235f),
            Offset(265f, 270f),
            Offset(135f, 305f),
            Offset(260f, 315f)
        )

        val result = validateLetterStrokes('A', listOf(zigZag), canvasWidth, canvasHeight)
        assertFalse("Zig-zag scribble inside letter box must be rejected", result.isValid)
    }

    @Test
    fun straightLineForCurvedLetterIsRejected() {
        // Straight vertical line when target is 'O'
        val straightLine = (0..15).map { i ->
            Offset(canvasWidth * 0.5f, canvasHeight * (0.20f + 0.62f * (i / 15f)))
        }

        val result = validateLetterStrokes('O', listOf(straightLine), canvasWidth, canvasHeight)
        assertFalse("Straight line for curved letter 'O' must be rejected", result.isValid)
    }

    @Test
    fun missingCrossbarInLetterAIsRejected() {
        // Only the two legs of 'A', missing the horizontal crossbar
        val legsOnly = getNormalizedLetterSegments('A').take(2).map { (start, end) ->
            (0..12).map { i ->
                val t = i.toFloat() / 12
                Offset(
                    x = (start.x + (end.x - start.x) * t) * canvasWidth,
                    y = (start.y + (end.y - start.y) * t) * canvasHeight
                )
            }
        }

        val result = validateLetterStrokes('A', legsOnly, canvasWidth, canvasHeight)
        assertFalse("Letter 'A' without crossbar must be rejected as incomplete", result.isValid)
        assertTrue("Feedback must mention incomplete letter", result.feedback.contains("belum lengkap"))
    }

    @Test
    fun resetAndRetryFlowWorksCorrectly() {
        val scribble = (0..10).map { i -> Offset(15f + i * 8f, 15f + i * 5f) }
        val rejected = validateLetterStrokes('B', listOf(scribble), canvasWidth, canvasHeight)
        assertFalse("Initial bad attempt should be rejected", rejected.isValid)

        val cleared = validateLetterStrokes('B', emptyList(), canvasWidth, canvasHeight)
        assertFalse("Cleared canvas after reset should be rejected", cleared.isValid)

        val validB = getNormalizedLetterSegments('B').map { (start, end) ->
            (0..10).map { i ->
                val t = i.toFloat() / 10
                Offset(
                    x = (start.x + (end.x - start.x) * t) * canvasWidth,
                    y = (start.y + (end.y - start.y) * t) * canvasHeight
                )
            }
        }
        val accepted = validateLetterStrokes('B', validB, canvasWidth, canvasHeight)
        assertTrue("Valid retry after reset should be accepted", accepted.isValid)
    }
}
