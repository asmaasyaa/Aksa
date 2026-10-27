package com.aksa.app.data.repository

import android.content.Context
import com.aksa.app.data.repository.demo.DemoAchievementRepository
import com.aksa.app.data.repository.demo.DemoAuthRepository
import com.aksa.app.data.repository.demo.DemoProfileRepository
import com.aksa.app.data.repository.demo.DemoSessionRepository
import com.aksa.app.domain.repository.AchievementRepository
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.ProfileRepository
import com.aksa.app.domain.repository.SessionRepository
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RepositoryMode {
    DEMO,
    REAL_FIREBASE
}

/**
 * Service locator and repository provider for AKSA.
 * Autodetects Firebase configuration or safely falls back to in-memory Demo repositories.
 */
object AksaRepositoryProvider {

    private val _currentMode = MutableStateFlow(RepositoryMode.DEMO)
    val currentMode: StateFlow<RepositoryMode> = _currentMode.asStateFlow()

    // Demo singletons
    val demoAuthRepository: DemoAuthRepository by lazy { DemoAuthRepository() }
    val demoProfileRepository: DemoProfileRepository by lazy { DemoProfileRepository() }
    val demoSessionRepository: DemoSessionRepository by lazy { DemoSessionRepository() }
    val demoAchievementRepository: DemoAchievementRepository by lazy { DemoAchievementRepository() }

    // Real Firebase singletons (lazily instantiated only if Firebase is present)
    private val firebaseAuthRepository: FirebaseAuthRepository by lazy { FirebaseAuthRepository() }
    private val firestoreProfileRepository: FirestoreProfileRepository by lazy { FirestoreProfileRepository() }
    private val firestoreSessionRepository: FirestoreSessionRepository by lazy {
        FirestoreSessionRepository(firebaseAuthRepository)
    }
    private val firestoreAchievementRepository: FirestoreAchievementRepository by lazy {
        FirestoreAchievementRepository(firebaseAuthRepository)
    }

    /**
     * Initializes repository mode on application startup.
     */
    fun initialize(context: Context) {
        val hasFirebase = try {
            FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
        } catch (e: Exception) {
            false
        }

        if (hasFirebase) {
            _currentMode.value = RepositoryMode.REAL_FIREBASE
        } else {
            _currentMode.value = RepositoryMode.DEMO
        }
    }

    fun setMode(mode: RepositoryMode) {
        _currentMode.value = mode
    }

    val authRepository: AuthRepository
        get() = if (_currentMode.value == RepositoryMode.REAL_FIREBASE) {
            firebaseAuthRepository
        } else {
            demoAuthRepository
        }

    val profileRepository: ProfileRepository
        get() = if (_currentMode.value == RepositoryMode.REAL_FIREBASE) {
            firestoreProfileRepository
        } else {
            demoProfileRepository
        }

    val sessionRepository: SessionRepository
        get() = if (_currentMode.value == RepositoryMode.REAL_FIREBASE) {
            firestoreSessionRepository
        } else {
            demoSessionRepository
        }

    val achievementRepository: AchievementRepository
        get() = if (_currentMode.value == RepositoryMode.REAL_FIREBASE) {
            firestoreAchievementRepository
        } else {
            demoAchievementRepository
        }
}
