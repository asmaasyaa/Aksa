package com.aksa.app.domain.usecase

/** Stars per COMPLETED session. SessionResult.accuracy is a fraction in [0f, 1f]. */
class CalculateSessionStarsUseCase {
    operator fun invoke(accuracy: Float): Int {
        require(accuracy in 0f..1f) { "Session accuracy must be between 0 and 1" }
        return when {
            accuracy >= 0.90f -> 3
            accuracy >= 0.70f -> 2
            else -> 1
        }
    }
}
