package com.aksa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.ChildProfile
import com.aksa.app.domain.model.WeeklyProgress
import com.aksa.app.domain.repository.AchievementRepository
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.ProfileRepository
import com.aksa.app.domain.repository.SessionRepository
import com.aksa.app.domain.usecase.CalculateWeeklyProgressUseCase
import com.aksa.app.ui.state.AksaUserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val childProfile: ChildProfile? = null,
    val weeklyProgress: WeeklyProgress? = null,
    val isDemoMode: Boolean = true,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val authRepository: AuthRepository = AksaRepositoryProvider.authRepository,
    private val profileRepository: ProfileRepository = AksaRepositoryProvider.profileRepository,
    private val sessionRepository: SessionRepository = AksaRepositoryProvider.sessionRepository,
    private val achievementRepository: AchievementRepository = AksaRepositoryProvider.achievementRepository,
    private val calculateWeeklyProgressUseCase: CalculateWeeklyProgressUseCase = CalculateWeeklyProgressUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isDemoMode = authRepository.isDemoMode))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val guardian = authRepository.currentGuardian.value
            val guardianUid = guardian?.uid ?: "demo_guardian_default"
            val profile = profileRepository.getActiveChildProfile(guardianUid)
            val childId = profile?.childId ?: "child_$guardianUid"

            if (profile != null) {
                AksaUserState.syncFromProfile(profile)
            }

            // Combine session history and achievements flow
            combine(
                sessionRepository.observeCompletedSessions(childId),
                achievementRepository.observeAchievements(childId)
            ) { sessions, achievements ->
                val mondayStr = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).toString()
                val milestonesThisWeek = achievements
                    .filter { it.weekStartDate == mondayStr }
                    .map { it.milestoneDays }
                    .toSet()

                val progress = calculateWeeklyProgressUseCase(
                    completedSessions = sessions,
                    milestonesEarned = milestonesThisWeek
                )

                if (profile != null) {
                    AksaUserState.syncFromProfile(profile, progress.currentStreak)
                }

                HomeUiState(
                    childProfile = profile,
                    weeklyProgress = progress,
                    isDemoMode = authRepository.isDemoMode,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
