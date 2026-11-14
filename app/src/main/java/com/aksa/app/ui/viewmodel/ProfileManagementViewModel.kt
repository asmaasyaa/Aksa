package com.aksa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.Profile
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.ProfileRepository
import com.aksa.app.ui.state.AksaUserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileManagementUiState(
    val profiles: List<Profile> = emptyList(),
    val activeProfile: Profile? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
    val updateSuccess: Boolean = false
)

class ProfileManagementViewModel(
    private val authRepository: AuthRepository = AksaRepositoryProvider.authRepository,
    private val profileRepository: ProfileRepository = AksaRepositoryProvider.profileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileManagementUiState())
    val uiState: StateFlow<ProfileManagementUiState> = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    fun loadProfiles() {
        val parentUid = authRepository.currentGuardian.value?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            profileRepository.observeProfiles(parentUid).collect { profileList ->
                val activeId = AksaUserState.currentProfileId
                val active = if (activeId.isNotBlank()) {
                    profileList.find { it.profileId == activeId } ?: profileList.firstOrNull()
                } else {
                    profileList.firstOrNull()
                }

                if (active != null && AksaUserState.currentProfileId.isBlank()) {
                    AksaUserState.syncFromProfile(active)
                }

                _uiState.value = _uiState.value.copy(
                    profiles = profileList,
                    activeProfile = active,
                    isLoading = false
                )
            }
        }
    }

    fun selectProfile(profile: Profile, onSelected: () -> Unit) {
        AksaUserState.syncFromProfile(profile)
        _uiState.value = _uiState.value.copy(activeProfile = profile)
        onSelected()
    }

    fun updateProfile(
        name: String,
        age: Int,
        avatarId: String,
        onSuccess: () -> Unit
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Nama anak tidak boleh kosong")
            return
        }
        if (trimmedName.length > 30) {
            _uiState.value = _uiState.value.copy(errorMessage = "Nama terlalu panjang (maksimal 30 karakter)")
            return
        }

        val parentUid = authRepository.currentGuardian.value?.uid
        if (parentUid == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Sesi tidak ditemukan. Silakan masuk kembali.")
            return
        }

        val profileId = _uiState.value.activeProfile?.profileId ?: AksaUserState.currentProfileId
        if (profileId.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Profil aktif tidak ditemukan.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null, updateSuccess = false)
            val result = profileRepository.updateProfile(
                userId = parentUid,
                profileId = profileId,
                name = trimmedName,
                avatarId = avatarId,
                age = age
            )
            result.fold(
                onSuccess = { updated ->
                    AksaUserState.syncFromProfile(updated)
                    _uiState.value = _uiState.value.copy(
                        activeProfile = updated,
                        isUpdating = false,
                        updateSuccess = true,
                        errorMessage = null
                    )
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        updateSuccess = false,
                        errorMessage = error.localizedMessage ?: "Gagal memperbarui profil anak."
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetUpdateSuccess() {
        _uiState.value = _uiState.value.copy(updateSuccess = false)
    }
}

