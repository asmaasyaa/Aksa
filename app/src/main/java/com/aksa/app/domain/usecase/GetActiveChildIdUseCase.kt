package com.aksa.app.domain.usecase

import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.ProfileRepository

/**
 * Reusable use case to resolve the currently active child profile identifier.
 * Ensures consistent child identity across all learning activities and progression evaluation.
 */
class GetActiveChildIdUseCase(
    private val authRepository: AuthRepository = AksaRepositoryProvider.authRepository,
    private val profileRepository: ProfileRepository = AksaRepositoryProvider.profileRepository
) {
    suspend operator fun invoke(): String {
        val guardian = authRepository.currentGuardian.value
        val guardianUid = guardian?.uid ?: "demo_guardian_default"
        val profile = profileRepository.getActiveChildProfile(guardianUid)
        return profile?.childId ?: "child_$guardianUid"
    }

    fun getSync(): String {
        val guardian = authRepository.currentGuardian.value
        val guardianUid = guardian?.uid ?: "demo_guardian_default"
        return "child_$guardianUid"
    }
}

