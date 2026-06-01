package com.minlish.feature.home.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.core.data.model.DashboardAnalyticsDto
import com.minlish.core.data.model.RecentStudyDeckEntity
import com.minlish.core.data.repository.AnalyticsRepository
import com.minlish.core.data.repository.SettingsRepository
import com.minlish.core.data.repository.UserRepository
import com.minlish.core.data.repository.VocabularyRepository
import com.minlish.core.network.ApiErrorParser
import com.minlish.core.network.dto.LearningLevelDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val vocabularyRepository: VocabularyRepository,
    settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {
    private val _learningLevels = MutableStateFlow<List<LearningLevelDto>>(emptyList())

    val fullName = settingsRepository.fullName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "Guest",
    )
    val avatarUrl = settingsRepository.avatarUrl.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "",
    )
    val learningGoal = settingsRepository.learningGoal.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "TOEIC",
    )
    val targetLevel = combine(
        settingsRepository.targetLevelId,
        _learningLevels,
    ) { levelId, levels ->
        levels.find { it.id == levelId }?.name ?: when (levelId) {
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1" -> "TOEIC 450+"
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2" -> "TOEIC 600+"
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3" -> "TOEIC 750+"
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4" -> "TOEIC 900+"
            "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1" -> "IELTS 4.0+"
            "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2" -> "IELTS 5.5+"
            "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3" -> "IELTS 6.5+"
            "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb4" -> "IELTS 7.0+"
            else -> "TOEIC 600+"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "TOEIC 600+",
    )
    val dailyNewWordsGoal = settingsRepository.dailyNewWordsGoal.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 10,
    )

    private val _dashboardAnalytics = MutableStateFlow(
        DashboardAnalyticsDto(0, 0, 0, 0, 0.0f, 0, 0),
    )
    val dashboardAnalytics: StateFlow<DashboardAnalyticsDto> = _dashboardAnalytics

    private val _recentStudyDeck = MutableStateFlow<RecentStudyDeckEntity?>(null)
    val recentStudyDeck: StateFlow<RecentStudyDeckEntity?> = _recentStudyDeck

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage

    fun fetchDashboardAnalytics() {
        viewModelScope.launch {
            try {
                _dashboardAnalytics.value = analyticsRepository.getDashboardAnalytics()
            } catch (e: Exception) {
                _lastErrorMessage.value = ApiErrorParser.humanMessage(
                    e,
                    "Mất kết nối dữ liệu thống kê cloud",
                )
            }
        }
    }

    fun refreshRecentStudyDeck() {
        viewModelScope.launch {
            try {
                _recentStudyDeck.value = vocabularyRepository.getRecentStudyDeck()
            } catch (_: Exception) {
                _recentStudyDeck.value = null
            }
        }
    }

    fun fetchLearningLevels() {
        viewModelScope.launch {
            try {
                _learningLevels.value = userRepository.getLevels()
            } catch (e: Exception) {
                android.util.Log.e("MINLISH_LEVELS", "Failed to fetch learning levels: ", e)
            }
        }
    }
}
