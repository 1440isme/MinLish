package com.minlish.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.core.data.repository.AuthRepository
import com.minlish.core.data.repository.SettingsRepository
import com.minlish.core.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(
    private val vocabularyRepository: VocabularyRepository,
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    val isOnboarded = settingsRepository.isOnboarded
        .map { it as Boolean? }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    val isDarkTheme = settingsRepository.isDarkTheme
    val isMockMode = settingsRepository.isMockServiceOn

    fun performOnboarding(name: String, goal: String, level: String, words: Int, reminder: String) {
        viewModelScope.launch {
            settingsRepository.saveOnboarding(name, goal, level, words, reminder)
            vocabularyRepository.seedDatabaseAsNecessary()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun toggleTheme() {
        settingsRepository.setDarkTheme(!isDarkTheme.value)
    }
}
