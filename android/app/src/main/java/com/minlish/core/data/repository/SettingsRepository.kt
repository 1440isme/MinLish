package com.minlish.core.data.repository

import com.minlish.core.datastore.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsRepository(private val tokenManager: TokenManager) {
    val fullName: Flow<String> = tokenManager.fullName
    val email: Flow<String> = tokenManager.email
    val learningGoal: Flow<String> = tokenManager.learningGoal
    val dailyNewWordsGoal: Flow<Int> = tokenManager.dailyNewWordsGoal
    val isOnboarded: Flow<Boolean> = tokenManager.isOnboarded

    private val _targetLevel = MutableStateFlow("600")
    val targetLevel: StateFlow<String> = _targetLevel

    private val _dailyReminderTime = MutableStateFlow("20:00")
    val dailyReminderTime: StateFlow<String> = _dailyReminderTime

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    private val _isMockServiceOn = MutableStateFlow(false)
    val isMockServiceOn: StateFlow<Boolean> = _isMockServiceOn

    suspend fun saveOnboarding(name: String, goal: String, level: String, words: Int, reminder: String) {
        tokenManager.saveAuthResponse(
            accessToken = "",
            refreshToken = "",
            fullName = name,
            email = "",
            learningGoal = goal,
            dailyGoal = words,
            isOnboarded = true
        )
        _targetLevel.value = level
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
