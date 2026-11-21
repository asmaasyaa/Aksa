package com.aksa.app.domain.usecase

import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.ProfileRepository
import com.aksa.app.ui.state.AksaUserState

/** Resolves the selected child profile under the signed-in guardian. */
class GetActiveChildIdUseCase(
    private val authRepository: AuthRepository = AksaRepositoryProvider.authRepository,
    private val profileRepository: ProfileRepository = AksaRepositoryProvider.profileRepository
) {
    suspend operator fun invoke(): String {
        val parentUid = authRepository.currentGuardian.value?.uid
            ?: throw IllegalStateException("Silakan login sebagai wali terlebih dahulu")
        val activeId = AksaUserState.currentProfileId
        if (activeId.isNotBlank()) {
            // Validate the profile belongs to the signed-in parent.
            val selected = profileRepository.getProfile(parentUid, activeId)
                ?: throw IllegalStateException("Profil anak tidak ditemukan. Pilih ulang profil.")
            return selected.profileId
        }
        return profileRepository.getActiveChildProfile(parentUid)?.profileId
            ?: throw IllegalStateException("Belum ada profil anak yang aktif")
    }

    /** UI-only fast lookup. Never fabricate an ID from the guardian UID. */
    fun getSync(): String = AksaUserState.currentProfileId.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException("Pilih profil anak sebelum memulai latihan")
}
