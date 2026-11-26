package com.aksa.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.ChildProfile
import com.aksa.app.domain.model.WeeklyProgress
import com.aksa.app.domain.repository.AchievementRepository
import com.aksa.app.domain.repository.AuthRepository
import com.aksa.app.domain.repository.LevelProgressRepository
import com.aksa.app.domain.repository.ProfileRepository
import com.aksa.app.domain.repository.SessionRepository
import com.aksa.app.domain.usecase.CalculateWeeklyProgressUseCase
import com.aksa.app.domain.usecase.GetLevelProgressionUseCase
import com.aksa.app.ui.components.LevelNodeStatus
import com.aksa.app.ui.state.AksaUserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class HomeUiState(
    val childProfile: ChildProfile? = null,
    val weeklyProgress: WeeklyProgress? = null,
    val activeModuleNumber: Int = 1,
    val activeModuleBadgeLabel: String = "Modul 1: Huruf",
    val activeModuleTitle: String = "Pulau Alfabet",
    val activeModuleDescription: String = "Latihan pengenalan huruf dan bunyi suara kata",
    val completedModulesCount: Int = 0,
    val totalModulesCount: Int = 4,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val authRepository: AuthRepository = AksaRepositoryProvider.authRepository,
    private val profileRepository: ProfileRepository = AksaRepositoryProvider.profileRepository,
    private val sessionRepository: SessionRepository = AksaRepositoryProvider.sessionRepository,
    private val achievementRepository: AchievementRepository = AksaRepositoryProvider.achievementRepository,
    private val levelProgressRepository: LevelProgressRepository = AksaRepositoryProvider.levelProgressRepository,
    private val calculateWeeklyProgressUseCase: CalculateWeeklyProgressUseCase = CalculateWeeklyProgressUseCase(),
    private val getLevelProgressionUseCase: GetLevelProgressionUseCase = GetLevelProgressionUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val guardian = authRepository.currentGuardian.value
            val guardianUid = guardian?.uid ?: return@launch
            val activeId = AksaUserState.currentProfileId.takeIf { it.isNotBlank() }
            val profile = profileRepository.getActiveChildProfile(guardianUid, activeId)
                ?: return@launch
            val childId = profile.childId

            AksaUserState.syncFromProfile(profile)

            combine(
                sessionRepository.observeCompletedSessions(childId),
                achievementRepository.observeAchievements(childId),
                levelProgressRepository.observeUnlockedLevels(childId)
            ) { sessions, achievements, _ ->
                val mondayStr = currentMondayDateString()
                val milestonesThisWeek = achievements
                    .filter { it.weekStartDate == mondayStr }
                    .map { it.milestoneDays }
                    .toSet()

                val progress = calculateWeeklyProgressUseCase(
                    completedSessions = sessions,
                    milestonesEarned = milestonesThisWeek
                )

                val mapProgress = runCatching { getLevelProgressionUseCase(childId) }.getOrNull()
                val statuses = listOf(
                    mapProgress?.getNodeStatus(1) ?: LevelNodeStatus.ACTIVE,
                    mapProgress?.getNodeStatus(2) ?: LevelNodeStatus.LOCKED,
                    mapProgress?.getNodeStatus(3) ?: LevelNodeStatus.LOCKED,
                    mapProgress?.getNodeStatus(4) ?: LevelNodeStatus.LOCKED
                )

                val activeModule = when {
                    statuses[3] != LevelNodeStatus.LOCKED -> 4
                    statuses[2] != LevelNodeStatus.LOCKED -> 3
                    statuses[1] != LevelNodeStatus.LOCKED -> 2
                    else -> 1
                }

                val completedModules = statuses.count { it == LevelNodeStatus.COMPLETED }

                val (badgeLabel, moduleTitle, moduleDesc) = when (activeModule) {
                    1 -> Triple(
                        "Modul 1: Huruf",
                        "Pulau Alfabet",
                        "Latihan pengenalan huruf dan bunyi suara kata"
                    )
                    2 -> Triple(
                        "Modul 2: Suku Kata",
                        "Dunia Suku Kata",
                        "Latihan menyusun dan membaca gabungan suku kata"
                    )
                    3 -> Triple(
                        "Modul 3: Kata",
                        "Gunung Kata",
                        "Latihan merangkai kata utuh dan kosakata baru"
                    )
                    else -> Triple(
                        "Modul 4: Cerita",
                        "Sungai Cerita",
                        "Latihan membaca cerita pendek dan pemahaman bacaan"
                    )
                }

                AksaUserState.level = badgeLabel
                AksaUserState.syncFromProfile(profile, progress.currentStreak)

                HomeUiState(
                    childProfile = profile,
                    weeklyProgress = progress,
                    activeModuleNumber = activeModule,
                    activeModuleBadgeLabel = badgeLabel,
                    activeModuleTitle = moduleTitle,
                    activeModuleDescription = moduleDesc,
                    completedModulesCount = completedModules,
                    totalModulesCount = 4,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun currentMondayDateString(): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }
}