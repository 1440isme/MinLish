package com.minlish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minlish.core.navigation.MinLishAppContent
import com.minlish.core.presentation.MainViewModel
import com.minlish.core.presentation.MinLishViewModelFactory
import com.minlish.ui.theme.MinLishTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as MinLishApplication
            val viewModelFactory = MinLishViewModelFactory(
                application = app,
                vocabularyRepository = app.vocabularyRepository,
                settingsRepository = app.settingsRepository,
                authRepository = app.authRepository,
                userRepository = app.userRepository,
                analyticsRepository = app.analyticsRepository,
                notificationRepository = app.notificationRepository,
            )
            val mainViewModel: MainViewModel = viewModel(
                factory = viewModelFactory,
            )

            MinLishTheme {
                MinLishAppContent(
                    mainViewModel = mainViewModel,
                    viewModelFactory = viewModelFactory,
                )
            }
        }
    }
}
