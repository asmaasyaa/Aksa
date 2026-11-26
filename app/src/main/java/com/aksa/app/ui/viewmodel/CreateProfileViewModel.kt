package com.aksa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.ProfileRepository
import com.aksa.app.ui.state.AksaUserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateProfileUiState(
    val name: String = "",
    val age: Int = 8,
    val avatarId: String = "mascot_classic",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class CreateProfileViewModel(
    private val authRepository: AuthRepository = AksaRepositoryProvider.authRepository,
    private val profileRepository: ProfileRepository = AksaRepositoryProvider.profileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProfileUiState())
    val uiState: StateFlow<CreateProfileUiState> = _uiState.asStateFlow()

    fun setName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, errorMessage = null)
    }

    fun setAge(age: Int) {
        _uiState.value = _uiState.value.copy(age = age)
    }

    fun setAvatarId(avatarId: String) {
        _uiState.value = _uiState.value.copy(avatarId = avatarId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun reset() {
        _uiState.value = CreateProfileUiState()
    }

    fun createProfile(onSuccess: () -> Unit) {
        val state = _uiState.value
        val trimmedName = state.name.trim()

        if (trimmedName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Nama anak tidak boleh kosong")
            return
        }
        if (trimmedName.length > 30) {
            _uiState.value = state.copy(errorMessage = "Nama terlalu panjang (maksimal 30 karakter)")
            return
        }

        val userId = authRepository.currentGuardian.value?.uid
        if (userId == null) {
            _uiState.value = state.copy(errorMessage = "Sesi tidak ditemukan. Silakan masuk kembali.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = profileRepository.createProfile(
                userId = userId,
                name = trimmedName,
                avatarId = state.avatarId,
                age = state.age
            )
            result.fold(
                onSuccess = { profile ->
                    AksaUserState.syncFromProfile(profile)
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Gagal membuat profil. Coba lagi."
                    )
                }
            )
        }
    }
}
