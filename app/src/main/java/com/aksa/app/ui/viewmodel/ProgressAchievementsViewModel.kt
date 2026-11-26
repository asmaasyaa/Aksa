package com.aksa.app.ui.viewmodel

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.MilestoneBadgeInfo
import com.aksa.app.domain.repository.AchievementRepository
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.ProfileRepository
import com.aksa.app.domain.repository.SessionRepository
import com.aksa.app.domain.usecase.CalculateSessionStarsUseCase
import com.aksa.app.domain.usecase.CalculateWeeklyProgressUseCase
import com.aksa.app.ui.state.AksaUserState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class ProgressAchievementsUiState(
    val currentStreak: Int = 0,
    val completedSessionsCount: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val averageAccuracyPercent: Int = 0,
    val totalStarsEarned: Int = 0,
    val milestoneBadges: List<MilestoneBadgeInfo> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressAchievementsViewModel(
    private val authRepository: AuthRepository = AksaRepositoryProvider.authRepository,
    private val profileRepository: ProfileRepository = AksaRepositoryProvider.profileRepository,
    private val sessionRepository: SessionRepository = AksaRepositoryProvider.sessionRepository,
    private val achievementRepository: AchievementRepository = AksaRepositoryProvider.achievementRepository,
    private val calculateWeeklyProgressUseCase: CalculateWeeklyProgressUseCase = CalculateWeeklyProgressUseCase(),
    private val calculateSessionStarsUseCase: CalculateSessionStarsUseCase = CalculateSessionStarsUseCase(),
    private val activeProfileIdFlow: Flow<String> = snapshotFlow { AksaUserState.currentProfileId }
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressAchievementsUiState(isLoading = true))
    val uiState: StateFlow<ProgressAchievementsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                authRepository.currentGuardian,
                activeProfileIdFlow.distinctUntilChanged()
            ) { guardian, activeProfileId ->
                guardian?.uid to activeProfileId.takeIf { it.isNotBlank() }
            }
                .distinctUntilChanged()
                .flatMapLatest { (guardianUid, activeId) ->
                    if (guardianUid == null) {
                        return@flatMapLatest flowOf(ProgressAchievementsUiState(isLoading = false))
                    }

                    val childId = try {
                        activeId ?: profileRepository.getActiveChildProfile(guardianUid, null)?.childId
                    } catch (e: Exception) {
                        return@flatMapLatest flowOf(
                            ProgressAchievementsUiState(
                                isLoading = false,
                                errorMessage = e.localizedMessage ?: "Gagal memuat profil anak"
                            )
                        )
                    }

                    if (childId.isNullOrBlank()) {
                        return@flatMapLatest flowOf(ProgressAchievementsUiState(isLoading = false))
                    }

                    combine(
                        sessionRepository.observeCompletedSessions(childId),
                        achievementRepository.observeAchievements(childId)
                    ) { sessions, achievements ->
                        val completedSessions = sessions.filter { it.isCompleted }
                        val mondayStr = LocalDate.now()
                            .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                            .toString()

                        val weeklyProgress = calculateWeeklyProgressUseCase(completedSessions)

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

                        val totalQuestions = completedSessions.sumOf { it.totalQuestions }
                        val totalCorrect = completedSessions.sumOf { it.correctAnswers }
                        val avgAccuracy = if (totalQuestions > 0) {
                            ((totalCorrect.toFloat() / totalQuestions.toFloat()) * 100).toInt()
                        } else 0

                        val totalStars = completedSessions.sumOf { session ->
                            calculateSessionStarsUseCase(session.accuracy)
                        }

                        ProgressAchievementsUiState(
                            currentStreak = weeklyProgress.currentStreak,
                            completedSessionsCount = completedSessions.size,
                            totalQuestionsAnswered = totalQuestions,
                            averageAccuracyPercent = avgAccuracy,
                            totalStarsEarned = totalStars,
                            milestoneBadges = badges,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                        .onStart {
                            emit(_uiState.value.copy(isLoading = true, errorMessage = null))
                        }
                        .catch { e ->
                            emit(
                                ProgressAchievementsUiState(
                                    isLoading = false,
                                    errorMessage = e.localizedMessage ?: "Gagal memuat data progress"
                                )
                            )
                        }
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }
}
