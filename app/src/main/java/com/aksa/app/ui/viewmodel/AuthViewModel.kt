package com.aksa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.ChildProfile
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.GuardianUser
import com.aksa.app.domain.repository.ProfileRepository
import com.aksa.app.ui.state.AksaUserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class AuthUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentGuardian: GuardianUser? = null,
    val isDemoMode: Boolean = true
)

class AuthViewModel(
    private val authRepository: AuthRepository = AksaRepositoryProvider.authRepository,
    private val profileRepository: ProfileRepository = AksaRepositoryProvider.profileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            currentGuardian = authRepository.currentGuardian.value,
            isDemoMode = authRepository.isDemoMode
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentGuardian.collect { guardian ->
                _uiState.value = _uiState.value.copy(
                    currentGuardian = guardian,
                    isDemoMode = authRepository.isDemoMode
                )
            }
        }
    }

    fun login(
        email: String,
        pass: String,
        onSuccess: () -> Unit
    ) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Silakan masukkan email dan password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.signIn(email.trim(), pass)
            result.fold(
                onSuccess = { guardian ->
                    // Load or sync child profile
                    val profile = profileRepository.getActiveChildProfile(guardian.uid)
                    if (profile != null) {
                        AksaUserState.syncFromProfile(profile)
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Gagal masuk. Periksa kembali email dan password."
                    )
                }
            )
        }
    }

    fun register(
        email: String,
        pass: String,
        childName: String,
        childAge: Int,
        childAvatarId: String,
        onSuccess: () -> Unit
    ) {
        if (email.isBlank() || pass.isBlank() || childName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Semua bidang wajib diisi")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.register(email.trim(), pass)
            result.fold(
                onSuccess = { guardian ->
                    val newProfile = ChildProfile(
                        childId = "child_${UUID.randomUUID()}",
                        guardianUid = guardian.uid,
                        displayName = childName.trim(),
                        age = childAge,
                        avatarId = childAvatarId
                    )
                    profileRepository.saveChildProfile(newProfile)
                    AksaUserState.syncFromProfile(newProfile)
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Gagal mendaftar akun orang tua."
                    )
                }
            )
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onComplete()
        }
    }
}
