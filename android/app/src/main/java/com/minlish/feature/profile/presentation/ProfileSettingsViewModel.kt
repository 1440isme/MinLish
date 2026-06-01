package com.minlish.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.core.data.repository.AuthRepository
import com.minlish.core.data.repository.NotificationRepository
import com.minlish.core.data.repository.SettingsRepository
import com.minlish.core.data.repository.UserRepository
import com.minlish.core.network.dto.LearningLevelDto
import com.minlish.feature.settings.data.NotificationSettingsResponse
import com.minlish.feature.settings.data.UpdateSettingsRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileSettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {
    private val _learningLevels = MutableStateFlow<List<LearningLevelDto>>(emptyList())
    val learningLevels: StateFlow<List<LearningLevelDto>> = _learningLevels

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
    val email = settingsRepository.email.stateIn(
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
        learningLevels,
    ) { levelId, levelsList ->
        levelsList.find { it.id == levelId }?.name ?: when (levelId) {
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
    val targetLevelId = settingsRepository.targetLevelId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "",
    )
    val dailyNewWordsGoal = settingsRepository.dailyNewWordsGoal.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 10,
    )
    val dailyReminderTime = settingsRepository.dailyReminderTime

    private val _notificationSettings = MutableStateFlow<NotificationSettingsResponse?>(null)
    val notificationSettings: StateFlow<NotificationSettingsResponse?> = _notificationSettings

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                userRepository.getProfile()
            } catch (_: Exception) {
                // Silently fail if network is not ready.
            }
        }
    }

    fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            try {
                userRepository.updateDailyNewWordsGoal(goal)
            } catch (_: Exception) {
            }
        }
    }

    fun updateTargetLevel(levelId: String) {
        viewModelScope.launch {
            try {
                userRepository.updateProfile(targetLevelId = levelId)
            } catch (_: Exception) {
            }
        }
    }

    fun updateLearningGoal(goal: String) {
        viewModelScope.launch {
            try {
                val defaultTargetLevelId = if (goal == "TOEIC") {
                    "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2"
                } else {
                    "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3"
                }
                userRepository.updateProfile(
                    learningGoal = goal,
                    targetLevelId = defaultTargetLevelId,
                )
            } catch (_: Exception) {
            }
        }
    }

    fun updateProfile(newName: String, newAvatarUrl: String) {
        viewModelScope.launch {
            try {
                userRepository.updateProfile(fullName = newName, avatarUrl = newAvatarUrl)
            } catch (_: Exception) {
            }
        }
    }

    fun fetchNotificationSettings() {
        viewModelScope.launch {
            try {
                _notificationSettings.value = notificationRepository.getSettings()
            } catch (_: Exception) {
            }
        }
    }

    fun updateNotificationToggle(
        dailyEnabled: Boolean? = null,
        timeStr: String? = null,
        dueEnabled: Boolean? = null,
        pushEnabled: Boolean? = null,
        emailEnabled: Boolean? = null,
    ) {
        viewModelScope.launch {
            try {
                val current = _notificationSettings.value ?: return@launch
                val request = UpdateSettingsRequest(
                    dailyReminderEnabled = dailyEnabled ?: current.dailyReminderEnabled,
                    dailyReminderTime = timeStr ?: current.dailyReminderTime,
                    dueReviewReminderEnabled = dueEnabled ?: current.dueReviewReminderEnabled,
                    pushEnabled = pushEnabled ?: current.pushEnabled,
                    emailEnabled = emailEnabled ?: current.emailEnabled,
                )
                _notificationSettings.value = notificationRepository.updateSettings(request)
            } catch (_: Exception) {
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

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
