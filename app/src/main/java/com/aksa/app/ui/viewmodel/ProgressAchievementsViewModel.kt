package com.aksa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.MilestoneBadgeInfo
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.WeeklyAchievement
import com.aksa.app.domain.repository.AchievementRepository
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.ProfileRepository
import com.aksa.app.domain.repository.SessionRepository
import com.aksa.app.domain.usecase.CalculateWeeklyProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class ProgressAchievementsUiState(
    val currentStreak: Int = 0,
    val completedSessionsCount: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val averageAccuracyPercent: Int = 0,
    val milestoneBadges: List<MilestoneBadgeInfo> = emptyList(),
    val isDemoMode: Boolean = true,
    val isLoading: Boolean = false
)

class ProgressAchievementsViewModel(
    private val authRepository: AuthRepository = AksaRepositoryProvider.authRepository,
    private val profileRepository: ProfileRepository = AksaRepositoryProvider.profileRepository,
    private val sessionRepository: SessionRepository = AksaRepositoryProvider.sessionRepository,
    private val achievementRepository: AchievementRepository = AksaRepositoryProvider.achievementRepository,
    private val calculateWeeklyProgressUseCase: CalculateWeeklyProgressUseCase = CalculateWeeklyProgressUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressAchievementsUiState(isDemoMode = authRepository.isDemoMode))
    val uiState: StateFlow<ProgressAchievementsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val guardian = authRepository.currentGuardian.value
            val guardianUid = guardian?.uid ?: "demo_guardian_default"
            val profile = profileRepository.getActiveChildProfile(guardianUid)
            val childId = profile?.childId ?: "child_${guardianUid}"

            combine(
                sessionRepository.observeCompletedSessions(childId),
                achievementRepository.observeAchievements(childId)
            ) { sessions, achievements ->
                val mondayStr = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).toString()

                val weeklyProgress = calculateWeeklyProgressUseCase(sessions)

                val count2 = achievements.count { it.milestoneDays == 2 }
                val count3 = achievements.count { it.milestoneDays == 3 }
                val count7 = achievements.count { it.milestoneDays == 7 }

                val earned2ThisWeek = achievements.any { it.milestoneDays == 2 && it.weekStartDate == mondayStr }
                val earned3ThisWeek = achievements.any { it.milestoneDays == 3 && it.weekStartDate == mondayStr }
                val earned7ThisWeek = achievements.any { it.milestoneDays == 7 && it.weekStartDate == mondayStr }

                val badges = listOf(
                    MilestoneBadgeInfo(
                        milestoneDays = 2,
                        title = "Penjelajah 2 Hari",
                        description = "Selesaikan latihan di 2 hari berbeda dalam satu minggu",
                        isEarnedThisWeek = earned2ThisWeek,
                        cumulativeEarnedCount = count2
                    ),
                    MilestoneBadgeInfo(
                        milestoneDays = 3,
                        title = "Juara Rajin 3 Hari",
                        description = "Selesaikan latihan di 3 hari berbeda dalam satu minggu",
                        isEarnedThisWeek = earned3ThisWeek,
                        cumulativeEarnedCount = count3
                    ),
                    MilestoneBadgeInfo(
                        milestoneDays = 7,
                        title = "Bintang Sempurna Seminggu",
                        description = "Luar biasa! Berlatih setiap hari dari Senin sampai Minggu",
                        isEarnedThisWeek = earned7ThisWeek,
                        cumulativeEarnedCount = count7
                    )
                )

                val totalQuestions = sessions.sumOf { it.totalQuestions }
                val totalCorrect = sessions.sumOf { it.correctAnswers }
                val avgAccuracy = if (totalQuestions > 0) {
                    ((totalCorrect.toFloat() / totalQuestions.toFloat()) * 100).toInt()
                } else 0

                ProgressAchievementsUiState(
                    currentStreak = weeklyProgress.currentStreak,
                    completedSessionsCount = sessions.size,
                    totalQuestionsAnswered = totalQuestions,
                    averageAccuracyPercent = avgAccuracy,
                    milestoneBadges = badges,
                    isDemoMode = authRepository.isDemoMode,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
