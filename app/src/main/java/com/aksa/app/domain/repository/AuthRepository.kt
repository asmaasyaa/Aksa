package com.aksa.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class GuardianUser(
    val uid: String,
    val email: String,
    val isDemo: Boolean = false
)

interface AuthRepository {
    val currentGuardian: StateFlow<GuardianUser?>
    val isDemoMode: Boolean

    suspend fun signIn(email: String, password: String): Result<GuardianUser>
    suspend fun register(email: String, password: String): Result<GuardianUser>
    suspend fun signOut()
}
