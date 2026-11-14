package com.aksa.app.domain.repository

import kotlinx.coroutines.flow.StateFlow

data class GuardianUser(
    val uid: String,
    val email: String
)

interface AuthRepository {
    val currentGuardian: StateFlow<GuardianUser?>

    suspend fun signIn(email: String, password: String): Result<GuardianUser>
    suspend fun register(email: String, password: String): Result<GuardianUser>
    suspend fun signInWithGoogle(idToken: String): Result<GuardianUser>
    suspend fun signOut()
}
