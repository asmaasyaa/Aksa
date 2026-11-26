package com.aksa.app.data.repository.demo

import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.GuardianUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * In-memory authentication repository used as test double in unit tests.
 */
class DemoAuthRepository : AuthRepository {
    private val _currentGuardian = MutableStateFlow<GuardianUser?>(
        GuardianUser(
            uid = "demo_guardian_default",
            email = "demo.orangtua@aksa.id"
        )
    )
    override val currentGuardian: StateFlow<GuardianUser?> = _currentGuardian.asStateFlow()

    override suspend fun signIn(email: String, password: String): Result<GuardianUser> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email dan password tidak boleh kosong"))
        }
        val isDefaultDemo = email.trim().equals("demo.orangtua@aksa.id", ignoreCase = true)
        val uid = if (isDefaultDemo) "demo_guardian_default" else "demo_user_${email.trim().hashCode()}"
        val user = GuardianUser(
            uid = uid,
            email = email.trim()
        )
        _currentGuardian.value = user
        return Result.success(user)
    }

    override suspend fun register(email: String, password: String): Result<GuardianUser> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email dan password tidak boleh kosong"))
        }
        val user = GuardianUser(
            uid = "demo_user_${UUID.randomUUID()}",
            email = email.trim()
        )
        _currentGuardian.value = user
        return Result.success(user)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<GuardianUser> {
        if (idToken.isBlank()) {
            return Result.failure(IllegalArgumentException("Token Google tidak valid"))
        }
        val user = GuardianUser(
            uid = "demo_google_guardian_${idToken.hashCode()}",
            email = "google.orangtua@aksa.id"
        )
        _currentGuardian.value = user
        return Result.success(user)
    }

    override suspend fun signOut() {
        _currentGuardian.value = null
    }
}
