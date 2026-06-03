package com.minlish.core.data.repository

import com.minlish.core.datastore.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val tokenManager: TokenManager) {
    val fullName: Flow<String> = tokenManager.fullName
    val email: Flow<String> = tokenManager.email
    val learningGoal: Flow<String> = tokenManager.learningGoal
    val dailyNewWordsGoal: Flow<Int> = tokenManager.dailyNewWordsGoal
    val isOnboarded: Flow<Boolean> = tokenManager.isOnboarded
    val hasShownGoalSetup: Flow<Boolean> = tokenManager.hasShownGoalSetup
    val avatarUrl: Flow<String> = tokenManager.avatarUrl

    val targetLevelId: Flow<String> = tokenManager.targetLevelId
    val targetLevel: Flow<String> = tokenManager.targetLevelId.map { levelId ->
        when (levelId) {
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1" -> "TOEIC 450+"
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2" -> "TOEIC 600+"
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3" -> "TOEIC 750+"
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4" -> "TOEIC 900+"
            "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1" -> "IELTS 4.0+"
            "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2" -> "IELTS 5.5+"
            "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3" -> "IELTS 6.5+"
            "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb4" -> "IELTS 7.0+"
            else -> "TOEIC 600+" // default fallback matching initial mock value
        }
    }

    private val _dailyReminderTime = MutableStateFlow("20:00")
    val dailyReminderTime: StateFlow<String> = _dailyReminderTime

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    private val _isMockServiceOn = MutableStateFlow(false)
    val isMockServiceOn: StateFlow<Boolean> = _isMockServiceOn

    suspend fun saveHasShownGoalSetup(shown: Boolean) {
        tokenManager.saveHasShownGoalSetup(shown)
    }

    suspend fun saveOnboarding(name: String, goal: String, level: String, words: Int, reminder: String) {
        tokenManager.saveAuthResponse(
            accessToken = "",
            refreshToken = "",
            fullName = name,
            email = "",
            learningGoal = goal,
            dailyGoal = words,
            isOnboarded = true,
            targetLevelId = level
        )
        _dailyReminderTime.value = reminder
    }

    suspend fun clearOnboarding() {
        tokenManager.clearAuth()
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    fun setMockServiceOn(isMock: Boolean) {
        _isMockServiceOn.value = isMock
    }
}
