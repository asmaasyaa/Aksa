package com.aksa.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateSessionStarsUseCaseTest {
    private val calculate = CalculateSessionStarsUseCase()

    @Test fun ninetyPercentGivesThreeStars() {
        assertEquals(3, calculate(0.90f))
        assertEquals(3, calculate(0.95f))
        assertEquals(3, calculate(1.0f))
    }

    @Test fun seventyToBelowNinetyGivesTwoStars() {
        assertEquals(2, calculate(0.70f))
        assertEquals(2, calculate(0.899f))
    }

    @Test fun belowSeventyStillGivesOneStar() {
        assertEquals(1, calculate(0.699f))
        assertEquals(1, calculate(0.30f))
        assertEquals(1, calculate(0f))
    }
}
