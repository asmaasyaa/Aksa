package com.aksa.app.domain.usecase

import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.ProfileRepository
import com.aksa.app.ui.state.AksaUserState

/**
 * Resolves the currently active child profile identifier.
 * Priority:
 * 1. Active profile ID currently selected in AksaUserState.
 * 2. Active profile resolved from ProfileRepository for the authenticated guardian.
 * 3. Default fallback if no profile exists yet.
 *
 * Ensures consistent child identity across all learning activities and progression evaluation.
 */
class GetActiveChildIdUseCase(
    private val authRepository: AuthRepository = AksaRepositoryProvider.authRepository,
    private val profileRepository: ProfileRepository = AksaRepositoryProvider.profileRepository
) {
    suspend operator fun invoke(): String {
        val activeStateId = AksaUserState.currentProfileId.takeIf { it.isNotBlank() }
        val guardian = authRepository.currentGuardian.value
        val guardianUid = guardian?.uid ?: "demo_guardian_default"

        val profile = profileRepository.getActiveChildProfile(guardianUid, activeStateId)
        if (profile != null) {
            AksaUserState.syncFromProfile(profile)
            return profile.profileId
        }

        return activeStateId ?: "child_default"
    }

    fun getSync(): String {
        if (AksaUserState.currentProfileId.isNotBlank()) {
            return AksaUserState.currentProfileId
        }
        val guardian = authRepository.currentGuardian.value
        return guardian?.let { "child_${it.uid}" } ?: "child_default"
    }
}

