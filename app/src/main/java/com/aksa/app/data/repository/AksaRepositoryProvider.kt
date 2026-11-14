package com.aksa.app.data.repository

import android.content.Context
import com.aksa.app.domain.repository.AchievementRepository
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.ProfileRepository
import com.aksa.app.domain.repository.SessionRepository
import com.google.firebase.FirebaseApp

/**
 * Service locator and repository provider for AKSA.
 * Exposes production Cloud Firestore and Firebase Auth repositories.
 */
object AksaRepositoryProvider {

    // Real Firebase singletons
    val firebaseAuthRepository: FirebaseAuthRepository by lazy { FirebaseAuthRepository() }
    val firestoreProfileRepository: FirestoreProfileRepository by lazy { FirestoreProfileRepository() }
    val firestoreSessionRepository: FirestoreSessionRepository by lazy {
        FirestoreSessionRepository(firebaseAuthRepository)
    }
    val firestoreAchievementRepository: FirestoreAchievementRepository by lazy {
        FirestoreAchievementRepository(firebaseAuthRepository)
    }

    /**
     * Initializes Firebase on application startup if not already initialized.
     */
    fun initialize(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
        } catch (_: Exception) {
            // Ignored if already initialized or in unit test environment
        }
    }

    val authRepository: AuthRepository
        get() = firebaseAuthRepository

    val profileRepository: ProfileRepository
        get() = firestoreProfileRepository

    val sessionRepository: SessionRepository
        get() = firestoreSessionRepository

    val achievementRepository: AchievementRepository
        get() = firestoreAchievementRepository
}
