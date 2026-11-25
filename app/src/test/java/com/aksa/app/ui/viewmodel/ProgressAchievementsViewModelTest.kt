package com.aksa.app.ui.viewmodel

import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.model.Profile
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.model.WeeklyAchievement
import com.aksa.app.domain.repository.AchievementRepository
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.GuardianUser
import com.aksa.app.domain.repository.ProfileRepository
import com.aksa.app.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressAchievementsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeAuthRepository(
        guardian: GuardianUser? = GuardianUser("guardian_1", "parent@aksa.test")
    ) : AuthRepository {
        override val currentGuardian: StateFlow<GuardianUser?> = MutableStateFlow(guardian)
        override suspend fun signIn(email: String, password: String) = Result.success(currentGuardian.value!!)
        override suspend fun register(email: String, password: String) = Result.success(currentGuardian.value!!)
        override suspend fun signInWithGoogle(idToken: String) = Result.success(currentGuardian.value!!)
        override suspend fun signOut() {}
    }

    private class FakeProfileRepository(
        private val profiles: List<Profile> = emptyList()
    ) : ProfileRepository {
        override fun observeProfiles(userId: String): Flow<List<Profile>> = flowOf(profiles)
        override suspend fun getProfiles(userId: String): List<Profile> = profiles
        override suspend fun getProfile(userId: String, profileId: String): Profile? =
            profiles.firstOrNull { it.profileId == profileId }
        override suspend fun createProfile(userId: String, name: String, avatarId: String, age: Int) =
            Result.success(profiles.first())
        override suspend fun updateProfile(userId: String, profileId: String, name: String, avatarId: String, age: Int) =
            Result.success(profiles.first())
        override suspend fun getActiveChildProfile(guardianUid: String, activeProfileId: String?): Profile? =
            if (!activeProfileId.isNullOrBlank()) profiles.firstOrNull { it.profileId == activeProfileId }
            else profiles.firstOrNull()
        override suspend fun saveChildProfile(profile: Profile) = Result.success(Unit)
        override fun observeActiveChildProfile(guardianUid: String): Flow<Profile?> = flowOf(profiles.firstOrNull())
    }

    private class FakeSessionRepository(
        private val sessionsByChild: Map<String, List<SessionResult>> = emptyMap(),
        private val shouldThrowForChild: String? = null
    ) : SessionRepository {
        override suspend fun recordAnswer(answer: AnswerLog) = Result.success(Unit)
        override suspend fun getAnswersForSession(sessionId: String) = emptyList<AnswerLog>()
        override suspend fun saveSessionResult(result: SessionResult) = Result.success(Unit)
        override suspend fun getCompletedSessions(childId: String) = sessionsByChild[childId].orEmpty()
        override fun observeCompletedSessions(childId: String): Flow<List<SessionResult>> =
            if (childId == shouldThrowForChild) {
                flow { throw IllegalStateException("Firestore unavailable") }
            } else {
                flowOf(sessionsByChild[childId].orEmpty())
            }
    }

    private class FakeAchievementRepository : AchievementRepository {
        override suspend fun awardMilestoneIfEligible(childId: String, weekStartDate: String, milestoneDays: Int) = false
        override suspend fun getAchievements(childId: String) = emptyList<WeeklyAchievement>()
        override fun observeAchievements(childId: String): Flow<List<WeeklyAchievement>> = flowOf(emptyList())
    }

    private fun session(id: String, childId: String, accuracy: Float, status: SessionStatus = SessionStatus.COMPLETED) =
        SessionResult(
            sessionId = id,
            childId = childId,
            moduleId = "PULAU_ALFABET",
            activityId = "HURUF_MIRIP",
            startedAt = 1_000L,
            completedAt = 2_000L,
            totalQuestions = 10,
            correctAnswers = (accuracy / 10).toInt(),
            incorrectAnswers = 10 - (accuracy / 10).toInt(),
            accuracy = accuracy,
            status = status
        )

    @Test
    fun `displays earned stars from CalculateSessionStarsUseCase and isolates per child profile`() = runTest {
        val activeProfileFlow = MutableStateFlow("child_a")
        val sessions = mapOf(
            "child_a" to listOf(
                session("s1", "child_a", 90f), // 3 stars
                session("s2", "child_a", 70f), // 2 stars
                session("s3", "child_a", 40f), // 1 star
                session("s4", "child_a", 100f, SessionStatus.IN_PROGRESS) // ignored
            ),
            "child_b" to listOf(
                session("s5", "child_b", 85f) // 3 stars
            ),
            "child_c" to emptyList()
        )

        val viewModel = ProgressAchievementsViewModel(
            authRepository = FakeAuthRepository(),
            profileRepository = FakeProfileRepository(),
            sessionRepository = FakeSessionRepository(sessions),
            achievementRepository = FakeAchievementRepository(),
            activeProfileIdFlow = activeProfileFlow
        )

        advanceUntilIdle()
        assertEquals(6, viewModel.uiState.value.totalStarsEarned)
        assertEquals(3, viewModel.uiState.value.completedSessionsCount)
        assertFalse(viewModel.uiState.value.isLoading)

        // Switch to child_b
        activeProfileFlow.value = "child_b"
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.totalStarsEarned)
        assertEquals(1, viewModel.uiState.value.completedSessionsCount)

        // Switch to child_c with no rewards
        activeProfileFlow.value = "child_c"
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.totalStarsEarned)
        assertEquals(0, viewModel.uiState.value.completedSessionsCount)
    }

    @Test
    fun `handles repository error gracefully without crashing and stops loading`() = runTest {
        val viewModel = ProgressAchievementsViewModel(
            authRepository = FakeAuthRepository(),
            profileRepository = FakeProfileRepository(),
            sessionRepository = FakeSessionRepository(shouldThrowForChild = "child_err"),
            achievementRepository = FakeAchievementRepository(),
            activeProfileIdFlow = MutableStateFlow("child_err")
        )

        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(0, viewModel.uiState.value.totalStarsEarned)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }
}
