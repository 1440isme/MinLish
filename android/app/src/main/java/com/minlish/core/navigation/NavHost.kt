package com.minlish.core.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minlish.MinLishApplication
import com.minlish.core.presentation.MinLishViewModel
import com.minlish.feature.analytics.presentation.AnalyticsScreen
import com.minlish.feature.auth.presentation.AuthViewModel
import com.minlish.feature.auth.presentation.AuthViewModelFactory
import com.minlish.feature.auth.presentation.login.LoginScreen
import com.minlish.feature.auth.presentation.register.RegisterScreen
import com.minlish.feature.deck.presentation.DeckDetailScreen
import com.minlish.feature.deck.presentation.DecksScreen
import com.minlish.feature.home.presentation.dashboard.DashboardScreen
import com.minlish.feature.learning.presentation.StudyFlashcardsScreen
import com.minlish.feature.practice.presentation.PracticeQuizScreen
import com.minlish.feature.profile.presentation.ProfileScreen

@Composable
fun MinLishAppContent(viewModel: MinLishViewModel) {
    val isOnboarded by viewModel.isOnboarded.collectAsState()
    val databaseAnalytics by viewModel.dashboardAnalytics.collectAsState()

    var currentScreen by remember { mutableStateOf("home") }
    var detailDeckId by remember { mutableStateOf<String?>(null) }
    var activeQuizDeckId by remember { mutableStateOf<String?>(null) }
    var activeQuizType by remember { mutableStateOf("MULTIPLE_CHOICE") }
    var activeStudyDeckId by remember { mutableStateOf<String?>(null) }

    // Toggle register and login states when unauthenticated
    var isRegisterMode by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val app = context.applicationContext as MinLishApplication
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(app.authRepository)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        if (!isOnboarded) {
            if (isRegisterMode) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onBackToLogin = { isRegisterMode = false }
                )
            } else {
                LoginScreen(
                    authViewModel = authViewModel,
                    onRegisterClick = { isRegisterMode = true }
                )
            }
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.navigationBarsPadding(),
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                            label = { Text("Home", fontSize = 11.sp) },
                            selected = currentScreen == "home",
                            onClick = { currentScreen = "home"; detailDeckId = null }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Decks") },
                            label = { Text("Decks", fontSize = 11.sp) },
                            selected = (currentScreen == "decks" || currentScreen == "deck_detail"),
                            onClick = { currentScreen = "decks"; detailDeckId = null }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                            label = { Text("Stats", fontSize = 11.sp) },
                            selected = currentScreen == "analytics",
                            onClick = { currentScreen = "analytics"; detailDeckId = null }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile", fontSize = 11.sp) },
                            selected = currentScreen == "profile",
                            onClick = { currentScreen = "profile"; detailDeckId = null }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentScreen) {
                        "home" -> DashboardScreen(
                            viewModel = viewModel,
                            onStartDailyQuiz = {
                                activeStudyDeckId = null
                                viewModel.startDailyQuizSession()
                                currentScreen = "study"
                            },
                            onStartDailyNew = {
                                activeStudyDeckId = null
                                viewModel.startDailyNewSession()
                                currentScreen = "study"
                            },
                            onNavigateToDecks = {
                                currentScreen = "decks"
                            }
                        )
                        "decks" -> DecksScreen(
                            viewModel = viewModel,
                            onDeckClick = { deckId ->
                                viewModel.selectDeck(deckId)
                                detailDeckId = deckId
                                currentScreen = "deck_detail"
                            }
                        )
                        "deck_detail" -> {
                            detailDeckId?.let { deckId ->
                                DeckDetailScreen(
                                    deckId = deckId,
                                    viewModel = viewModel,
                                    onBack = { currentScreen = "decks"; detailDeckId = null },
                                    onStartStudy = {
                                        activeStudyDeckId = deckId
                                        viewModel.startStudySession(deckId)
                                        currentScreen = "study"
                                    },
                                    onStartQuiz = { qType ->
                                        activeQuizDeckId = deckId
                                        activeQuizType = qType
                                        viewModel.startQuizPractice(deckId, qType)
                                        currentScreen = "practice_quiz"
                                    }
                                )
                            } ?: run { currentScreen = "decks" }
                        }
                        "practice_quiz" -> {
                            activeQuizDeckId?.let { deckId ->
                                PracticeQuizScreen(
                                    deckId = deckId,
                                    practiceType = activeQuizType,
                                    viewModel = viewModel,
                                    onBack = {
                                        currentScreen = "deck_detail"
                                    }
                                )
                            } ?: run { currentScreen = "decks" }
                        }
                        "study" -> StudyFlashcardsScreen(
                            viewModel = viewModel,
                            onFinish = {
                                currentScreen = if (activeStudyDeckId != null) {
                                    "deck_detail"
                                } else {
                                    "home"
                                }
                            }
                        )
                        "analytics" -> AnalyticsScreen(
                            viewModel = viewModel,
                            stats = databaseAnalytics
                        )
                        "profile" -> ProfileScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
