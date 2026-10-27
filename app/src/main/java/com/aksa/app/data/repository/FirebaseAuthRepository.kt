package com.aksa.app.data.repository

import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.GuardianUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Real Firebase Authentication repository using Firebase Auth email/password.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    private val _currentGuardian = MutableStateFlow<GuardianUser?>(
        auth.currentUser?.let {
            GuardianUser(uid = it.uid, email = it.email ?: "", isDemo = false)
        }
    )
    override val currentGuardian: StateFlow<GuardianUser?> = _currentGuardian.asStateFlow()
    override val isDemoMode: Boolean = false

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentGuardian.value = user?.let {
                GuardianUser(uid = it.uid, email = it.email ?: "", isDemo = false)
            }
        }
    }

    override suspend fun signIn(email: String, password: String): Result<GuardianUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw IllegalStateException("Firebase user was null after sign in")
            val guardian = GuardianUser(uid = user.uid, email = user.email ?: email, isDemo = false)
            _currentGuardian.value = guardian
            Result.success(guardian)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<GuardianUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw IllegalStateException("Firebase user was null after registration")
            val guardian = GuardianUser(uid = user.uid, email = user.email ?: email, isDemo = false)
            _currentGuardian.value = guardian
            Result.success(guardian)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
        _currentGuardian.value = null
    }
}
