package com.aksa.app.domain.usecase

class CalculateSessionStarsUseCase {
    operator fun invoke(accuracyPercentage: Float): Int{
        return when {
            accuracyPercentage >= 85f -> 3 
            accuracyPercentage >= 60f -> 2 
            else -> 1 
        }
    }
}

