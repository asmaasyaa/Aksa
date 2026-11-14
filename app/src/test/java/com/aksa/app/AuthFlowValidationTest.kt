package com.aksa.app

import com.aksa.app.data.repository.demo.DemoAuthRepository
import com.aksa.app.data.repository.demo.DemoProfileRepository
import com.aksa.app.domain.model.ChildProfile
import com.aksa.app.domain.model.Profile
import com.aksa.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthFlowValidationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: DemoAuthRepository
    private lateinit var profileRepository: DemoProfileRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = DemoAuthRepository()
        profileRepository = DemoProfileRepository()
        viewModel = AuthViewModel(
            authRepository = authRepository,
            profileRepository = profileRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun registerWithBlankEmailOrPasswordShowsError() {
        viewModel.register(email = "", pass = "123456")
        assertEquals("Email dan kata sandi tidak boleh kosong", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.registerSuccess)

        viewModel.register(email = "test@aksa.id", pass = "")
        assertEquals("Email dan kata sandi tidak boleh kosong", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.registerSuccess)
    }

    @Test
    fun registerWithInvalidEmailFormatShowsError() {
        viewModel.register(email = "bukan_email", pass = "123456")
        assertEquals("Format email tidak valid. Gunakan format nama@domain.com", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.registerSuccess)

        viewModel.register(email = "user@", pass = "123456")
        assertEquals("Format email tidak valid. Gunakan format nama@domain.com", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.registerSuccess)
    }

    @Test
    fun registerWithShortPasswordShowsError() {
        viewModel.register(email = "parent@aksa.id", pass = "12345")
        assertEquals("Kata sandi minimal 6 karakter", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.registerSuccess)
    }

    @Test
    fun registerValidCredentialsSucceedsWithoutCreatingChildProfile() = runTest {
        viewModel.register(email = "valid.parent@aksa.id", pass = "secret123")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.registerSuccess)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)

        val currentGuardian = viewModel.uiState.value.currentGuardian
        assertNotNull(currentGuardian)

        // Acceptance criteria: Registration MUST NOT automatically create child profile
        val activeProfile = profileRepository.getActiveChildProfile(currentGuardian!!.uid)
        assertNull("Child profile must NOT be automatically created during registration", activeProfile)
    }

    @Test
    fun loginDetectsWhetherChildProfileExists() = runTest {
        var loginResultHasProfile: Boolean? = null

        // 1. Login with a guardian who has no profile
        viewModel.login(
            email = "new.parent@aksa.id",
            pass = "secret123",
            onSuccess = { hasProfile ->
                loginResultHasProfile = hasProfile
            }
        )
        advanceUntilIdle()

        assertNotNull(loginResultHasProfile)
        assertFalse(loginResultHasProfile!!)

        // 2. Add profile for the guardian
        val guardianUid = viewModel.uiState.value.currentGuardian!!.uid
        profileRepository.saveChildProfile(
            Profile(
                profileId = "child_test_1",
                userId = guardianUid,
                name = "Aksa",
                avatarId = "mascot_classic"
            )
        )

        // 3. Login again -> should report hasProfile = true
        var loginResultAfterProfileCreated: Boolean? = null
        viewModel.login(
            email = "new.parent@aksa.id",
            pass = "secret123",
            onSuccess = { hasProfile ->
                loginResultAfterProfileCreated = hasProfile
            }
        )
        advanceUntilIdle()

        assertNotNull(loginResultAfterProfileCreated)
        assertTrue(loginResultAfterProfileCreated!!)
    }

    @Test
    fun googleSignInDetectsProfileCorrectly() = runTest {
        var googleSignInResult: Boolean? = null

        viewModel.signInWithGoogle(
            idToken = "valid_google_token_xyz",
            onSuccess = { hasProfile ->
                googleSignInResult = hasProfile
            }
        )
        advanceUntilIdle()

        assertNotNull(googleSignInResult)
        assertFalse(googleSignInResult!!)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun multipleSubmissionPreventedWhenLoading() = runTest {
        viewModel.setLoading(true)

        // Attempting to register while loading should be ignored
        viewModel.register(email = "test@aksa.id", pass = "123456")
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)

        // Attempting to login while loading should be ignored
        viewModel.login(email = "test@aksa.id", pass = "123456", onSuccess = {})
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)

        viewModel.clearLoading()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun signOutClearsSession() = runTest {
        // Sign in first
        viewModel.login(email = "parent@aksa.id", pass = "123456", onSuccess = {})
        advanceUntilIdle()

        var signedOut = false
        viewModel.signOut {
            signedOut = true
        }
        advanceUntilIdle()

        assertTrue(signedOut)
        assertNull(authRepository.currentGuardian.value)
    }
}

