package com.aksa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.GuardianUser
import com.aksa.app.domain.repository.ProfileRepository
import com.aksa.app.ui.state.AksaUserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
data class AuthUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentGuardian: GuardianUser? = null,
    val registerSuccess: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository = AksaRepositoryProvider.authRepository,
    private val profileRepository: ProfileRepository = AksaRepositoryProvider.profileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            currentGuardian = authRepository.currentGuardian.value
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")

    init {
        viewModelScope.launch {
            authRepository.currentGuardian.collect { guardian ->
                _uiState.value = _uiState.value.copy(
                    currentGuardian = guardian
                )
            }
        }
    }

    fun login(
        email: String,
        pass: String,
        onSuccess: (hasProfile: Boolean) -> Unit
    ) {
        if (_uiState.value.isLoading) return

        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Silakan masukkan email dan password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = authRepository.signIn(trimmedEmail, pass)
                result.fold(
                    onSuccess = { guardian ->
                        val profile = profileRepository.getActiveChildProfile(guardian.uid)
                        if (profile != null) {
                            AksaUserState.syncFromProfile(profile)
                        }
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                        onSuccess(profile != null)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = parseAuthError(error, isLogin = true)
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Terjadi kesalahan saat masuk."
                )
            }
        }
    }

    fun register(
        email: String,
        pass: String
    ) {
        if (_uiState.value.isLoading) return

        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email dan kata sandi tidak boleh kosong")
            return
        }

        if (!emailRegex.matches(trimmedEmail)) {
            _uiState.value = _uiState.value.copy(errorMessage = "Format email tidak valid. Gunakan format nama@domain.com")
            return
        }

        if (pass.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Kata sandi minimal 6 karakter")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = authRepository.register(trimmedEmail, pass)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            registerSuccess = true,
                            errorMessage = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = parseAuthError(error, isLogin = false)
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Gagal mendaftar akun orang tua."
                )
            }
        }
    }

    fun signInWithGoogle(
        idToken: String,
        onSuccess: (hasProfile: Boolean) -> Unit
    ) {
        if (idToken.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Token Google tidak valid")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = authRepository.signInWithGoogle(idToken)
                result.fold(
                    onSuccess = { guardian ->
                        val profile = profileRepository.getActiveChildProfile(guardian.uid)
                        if (profile != null) {
                            AksaUserState.syncFromProfile(profile)
                        }
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                        onSuccess(profile != null)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = parseAuthError(error, isLogin = true)
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Gagal autentikasi menggunakan Google."
                )
            }
        }
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    fun setLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = loading)
    }

    fun clearLoading() {
        _uiState.value = _uiState.value.copy(isLoading = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearRegisterSuccess() {
        _uiState.value = _uiState.value.copy(registerSuccess = false)
    }

    fun checkSession(onResult: (isLoggedIn: Boolean, hasProfile: Boolean) -> Unit) {
        viewModelScope.launch {
            val guardian = authRepository.currentGuardian.value
            if (guardian != null) {
                val profile = profileRepository.getActiveChildProfile(guardian.uid)
                if (profile != null) {
                    AksaUserState.syncFromProfile(profile)
                }
                onResult(true, profile != null)
            } else {
                onResult(false, false)
            }
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onComplete()
        }
    }

    private fun parseAuthError(error: Throwable, isLogin: Boolean): String {
        val msg = error.message ?: ""
        return when {
            msg.contains("account-exists-with-different-credential", ignoreCase = true) ->
                "Email ini sudah terdaftar dengan metode masuk yang berbeda. Silakan gunakan metode masuk sebelumnya."

            msg.contains("WEAK_PASSWORD", ignoreCase = true) ||
            msg.contains("weak password", ignoreCase = true) -> "Kata sandi terlalu lemah. Gunakan minimal 6 karakter."

            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            msg.contains("invalid-credential", ignoreCase = true) ||
            msg.contains("wrong-password", ignoreCase = true) ||
            msg.contains("user-not-found", ignoreCase = true) -> "Email atau kata sandi tidak sesuai."

            msg.contains("EMAIL_EXISTS", ignoreCase = true) ||
            msg.contains("email-already-in-use", ignoreCase = true) -> "Email sudah terdaftar. Silakan gunakan email lain atau masuk."

            msg.contains("network", ignoreCase = true) -> "Koneksi internet bermasalah. Silakan periksa jaringan Anda."

            else -> error.localizedMessage ?: if (isLogin) "Gagal masuk. Periksa kembali email dan password." else "Gagal mendaftar akun orang tua."
        }
    }
}
