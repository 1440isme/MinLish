package com.minlish.core.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.minlish.core.data.repository.AnalyticsRepository
import com.minlish.core.data.repository.AuthRepository
import com.minlish.core.data.repository.NotificationRepository
import com.minlish.core.data.repository.SettingsRepository
import com.minlish.core.data.repository.UserRepository
import com.minlish.core.data.repository.VocabularyRepository
import com.minlish.feature.deck.presentation.DeckDetailViewModel
import com.minlish.feature.deck.presentation.DecksViewModel
import com.minlish.feature.home.presentation.dashboard.DashboardViewModel
import com.minlish.feature.learning.presentation.StudyFlashcardsViewModel
import com.minlish.feature.practice.presentation.PracticeQuizViewModel
import com.minlish.feature.profile.presentation.ProfileSettingsViewModel

class MinLishViewModelFactory(
    private val application: Application,
    private val vocabularyRepository: VocabularyRepository,
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(
                vocabularyRepository = vocabularyRepository,
                settingsRepository = settingsRepository,
                authRepository = authRepository,
            )
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(
                vocabularyRepository = vocabularyRepository,
                settingsRepository = settingsRepository,
                userRepository = userRepository,
                analyticsRepository = analyticsRepository,
            )
            modelClass.isAssignableFrom(DecksViewModel::class.java) -> DecksViewModel(
                vocabularyRepository = vocabularyRepository,
            )
            modelClass.isAssignableFrom(DeckDetailViewModel::class.java) -> DeckDetailViewModel(
                vocabularyRepository = vocabularyRepository,
            )
            modelClass.isAssignableFrom(StudyFlashcardsViewModel::class.java) -> StudyFlashcardsViewModel(
                application = application,
                vocabularyRepository = vocabularyRepository,
                settingsRepository = settingsRepository,
            )
            modelClass.isAssignableFrom(PracticeQuizViewModel::class.java) -> PracticeQuizViewModel(
                vocabularyRepository = vocabularyRepository,
                analyticsRepository = analyticsRepository,
            )
            modelClass.isAssignableFrom(ProfileSettingsViewModel::class.java) -> ProfileSettingsViewModel(
                settingsRepository = settingsRepository,
                authRepository = authRepository,
                userRepository = userRepository,
                notificationRepository = notificationRepository,
            )
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
