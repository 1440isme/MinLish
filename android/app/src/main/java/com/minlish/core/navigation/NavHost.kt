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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minlish.MinLishApplication
import com.minlish.core.presentation.MainViewModel
import com.minlish.feature.analytics.presentation.AnalyticsScreen
import com.minlish.feature.auth.presentation.AuthViewModel
import com.minlish.feature.auth.presentation.AuthViewModelFactory
import com.minlish.feature.auth.presentation.login.LoginScreen
import com.minlish.feature.auth.presentation.register.RegisterScreen
import com.minlish.feature.deck.presentation.DeckDetailScreen
import com.minlish.feature.deck.presentation.DeckDetailViewModel
import com.minlish.feature.deck.presentation.DecksScreen
import com.minlish.feature.deck.presentation.DecksViewModel
import com.minlish.feature.home.presentation.dashboard.DashboardScreen
import com.minlish.feature.home.presentation.dashboard.DashboardViewModel
import com.minlish.feature.learning.presentation.StudyFlashcardsScreen
import com.minlish.feature.learning.presentation.StudyFlashcardsViewModel
import com.minlish.feature.practice.presentation.PracticeQuizScreen
import com.minlish.feature.practice.presentation.PracticeQuizViewModel
import com.minlish.feature.profile.presentation.ProfileScreen
import com.minlish.feature.profile.presentation.ProfileSettingsViewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import com.minlish.core.network.dto.CreateSessionResponse
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.minlish.core.utils.showToast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.minlish.feature.settings.presentation.SettingsScreen
import androidx.compose.ui.res.stringResource
import com.minlish.R

@Composable
fun MinLishAppContent(
    mainViewModel: MainViewModel,
    viewModelFactory: ViewModelProvider.Factory,
) {
    val dashboardViewModel: DashboardViewModel = viewModel(factory = viewModelFactory)
    val decksViewModel: DecksViewModel = viewModel(factory = viewModelFactory)
    val deckDetailViewModel: DeckDetailViewModel = viewModel(factory = viewModelFactory)
    val studyViewModel: StudyFlashcardsViewModel = viewModel(factory = viewModelFactory)
    val practiceViewModel: PracticeQuizViewModel = viewModel(factory = viewModelFactory)
    val profileSettingsViewModel: ProfileSettingsViewModel = viewModel(factory = viewModelFactory)

    val isOnboarded by mainViewModel.isOnboarded.collectAsState()
    val databaseAnalytics by dashboardViewModel.dashboardAnalytics.collectAsState()

    LaunchedEffect(isOnboarded) {
        if (isOnboarded) {
            dashboardViewModel.fetchLearningLevels()
            dashboardViewModel.fetchDashboardAnalytics()
            dashboardViewModel.refreshRecentStudyDeck()
            deckDetailViewModel.refreshFavoritedIds()
            profileSettingsViewModel.fetchUserProfile()
            profileSettingsViewModel.fetchNotificationSettings()
            profileSettingsViewModel.fetchLearningLevels()
            practiceViewModel.fetchPracticeHistory()
        }
    }

    var currentScreen by remember { mutableStateOf("home") }
    var detailDeckId by remember { mutableStateOf<String?>(null) }
    var activeQuizDeckId by remember { mutableStateOf<String?>(null) }
    var activeQuizType by remember { mutableStateOf("MULTIPLE_CHOICE") }
    var activeStudyDeckId by remember { mutableStateOf<String?>(null) }

    // Dialog state variables
    var showResumeDialog by remember { mutableStateOf(false) }
    var activeSessionResponse by remember { mutableStateOf<CreateSessionResponse?>(null) }

    var showSetupDialog by remember { mutableStateOf(false) }
    var showPracticeScopeSelectionDialog by remember { mutableStateOf(false) }
    var practiceScope by remember { mutableStateOf("LEARNED_ONLY") }
    var targetSetupDeckId by remember { mutableStateOf<String?>(null) }



    // Toggle register and login states when unauthenticated
    var isRegisterMode by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val app = context.applicationContext as MinLishApplication
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(app.authRepository)
    )

    // Dialog for resuming an active session
    if (showResumeDialog) {
        AlertDialog(
            onDismissRequest = { showResumeDialog = false },
            title = {
                Text(
                    stringResource(R.string.practice_resume_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    stringResource(R.string.practice_resume_desc),
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResumeDialog = false
                        activeSessionResponse?.let {
                            practiceViewModel.resumeActiveSession(it)
                            currentScreen = "practice_quiz"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                ) {
                    Text(stringResource(R.string.practice_btn_resume), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showResumeDialog = false
                        activeSessionResponse?.session?.id?.let { sessionId ->
                            practiceViewModel.cancelActiveSession(sessionId) {
                                targetSetupDeckId = detailDeckId
                                showPracticeScopeSelectionDialog = true
                            }
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Text(stringResource(R.string.practice_btn_start_new), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Dialog for choosing practice scope first
    if (showPracticeScopeSelectionDialog) {
        val vocabList by deckDetailViewModel.vocabulariesInSelectedDeck.collectAsState()
        val totalWordsInDeck = vocabList.size
        val deckLearningProgress by deckDetailViewModel.selectedDeckLearningProgress.collectAsState()
        val newWordsAvailable = deckLearningProgress?.newWordsAvailable ?: totalWordsInDeck
        val learnedWordsCount = (totalWordsInDeck - newWordsAvailable).coerceIn(0, totalWordsInDeck)

        AlertDialog(
            onDismissRequest = { showPracticeScopeSelectionDialog = false },
            title = {
                Text(
                    stringResource(R.string.practice_mode_selection_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: Only Learned Words
                    Card(
                        onClick = {
                            if (learnedWordsCount < 4) {
                                showToast(
                                    context,
                                    context.getString(R.string.practice_mode_not_enough_learned_words, learnedWordsCount),
                                    android.widget.Toast.LENGTH_LONG
                                )
                            } else {
                                practiceScope = "LEARNED_ONLY"
                                showPracticeScopeSelectionDialog = false
                                showSetupDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f).height(115.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0D9488).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color(0xFF0D9488),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.practice_mode_learned_words),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1C1C1A),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Card 2: All Words in Deck
                    Card(
                        onClick = {
                            if (totalWordsInDeck < 4) {
                                showToast(
                                    context,
                                    context.getString(R.string.practice_mode_not_enough_all_words, totalWordsInDeck),
                                    android.widget.Toast.LENGTH_LONG
                                )
                            } else {
                                practiceScope = "ALL"
                                showPracticeScopeSelectionDialog = false
                                showSetupDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f).height(115.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0D9488).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = Color(0xFF0D9488),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.practice_mode_all_words),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1C1C1A),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Dialog for practice setup configuration
    if (showSetupDialog) {
        val vocabList by deckDetailViewModel.vocabulariesInSelectedDeck.collectAsState()
        val totalWordsInDeck = vocabList.size
        val deckLearningProgress by deckDetailViewModel.selectedDeckLearningProgress.collectAsState()
        val newWordsAvailable = deckLearningProgress?.newWordsAvailable ?: totalWordsInDeck
        val learnedWordsCount = (totalWordsInDeck - newWordsAvailable).coerceIn(0, totalWordsInDeck)

        val maxAllowedQuestions = if (practiceScope == "LEARNED_ONLY") learnedWordsCount else totalWordsInDeck

        var questionCount by remember(maxAllowedQuestions) {
            mutableStateOf(if (maxAllowedQuestions > 10) 10 else maxAllowedQuestions)
        }

        var isMultipleChoiceChecked by remember { mutableStateOf(true) }
        var isFillInBlankChecked by remember { mutableStateOf(true) }
        var isListeningChecked by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showSetupDialog = false },
            title = {
                Text(
                    stringResource(R.string.practice_setup_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.practice_setup_num_questions),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = questionCount.toFloat(),
                            onValueChange = { questionCount = it.toInt() },
                            valueRange = 1f..maxAllowedQuestions.coerceAtLeast(1).toFloat(),
                            steps = if (maxAllowedQuestions > 1) maxAllowedQuestions - 2 else 0,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFBBF24),
                                activeTrackColor = Color(0xFFFBBF24),
                                inactiveTrackColor = Color(0xFFE5E7EB)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.practice_setup_questions_count, questionCount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        stringResource(R.string.practice_setup_question_types),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isMultipleChoiceChecked,
                            onCheckedChange = { isMultipleChoiceChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFBBF24))
                        )
                        Text(stringResource(R.string.practice_setup_type_mc), fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isFillInBlankChecked,
                            onCheckedChange = { isFillInBlankChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFBBF24))
                        )
                        Text(stringResource(R.string.practice_setup_type_fib), fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isListeningChecked,
                            onCheckedChange = { isListeningChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFBBF24))
                        )
                        Text(stringResource(R.string.practice_setup_type_listening), fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                val isAnyChecked = isMultipleChoiceChecked || isFillInBlankChecked || isListeningChecked
                Button(
                    onClick = {
                        val types = mutableListOf<String>()
                        if (isMultipleChoiceChecked) types.add("MULTIPLE_CHOICE")
                        if (isFillInBlankChecked) types.add("FILL_IN_BLANK")
                        if (isListeningChecked) types.add("LISTENING")

                        targetSetupDeckId?.let { deckId ->
                            if (totalWordsInDeck == 0) {
                                showSetupDialog = false
                                showToast(
                                    context,
                                    context.getString(R.string.practice_setup_empty_deck_warning),
                                    android.widget.Toast.LENGTH_LONG
                                )
                                return@let
                            }
                            practiceViewModel.startNewPracticeSession(deckId, types, questionCount, practiceScope)
                            showSetupDialog = false
                            currentScreen = "practice_quiz"
                        }
                    },
                    enabled = isAnyChecked,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text(stringResource(R.string.practice_setup_btn_start))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSetupDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

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
                // Empty bottomBar to allow content to scroll underneath the floating island
                contentWindowInsets = WindowInsets.statusBars
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Content screens drawing under the floating bar
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (currentScreen) {
                            "home" -> DashboardScreen(
                                viewModel = dashboardViewModel,
                                onStartDailyQuiz = {
                                    activeStudyDeckId = null
                                    studyViewModel.startDailyQuizSession()
                                    currentScreen = "study"
                                },
                                onStartDailyNew = {
                                    activeStudyDeckId = null
                                    studyViewModel.startDailyNewSession()
                                    currentScreen = "study"
                                },
                                onNavigateToDecks = {
                                    currentScreen = "decks"
                                },
                                onResumeRecentDeck = { deckId ->
                                    detailDeckId = deckId
                                    activeStudyDeckId = deckId
                                    deckDetailViewModel.selectDeck(deckId)
                                    studyViewModel.startStudySession(deckId)
                                    currentScreen = "study"
                                },
                                onOpenRecentDeck = { deckId ->
                                    deckDetailViewModel.selectDeck(deckId)
                                    detailDeckId = deckId
                                    currentScreen = "deck_detail"
                                }
                            )
                            "decks" -> DecksScreen(
                                viewModel = decksViewModel,
                                onDeckClick = { deckId ->
                                    deckDetailViewModel.selectDeck(deckId)
                                    detailDeckId = deckId
                                    currentScreen = "deck_detail"
                                }
                            )
                            "deck_detail" -> {
                                detailDeckId?.let { deckId ->
                                    DeckDetailScreen(
                                        deckId = deckId,
                                        viewModel = deckDetailViewModel,
                                        onBack = { currentScreen = "decks"; detailDeckId = null },
                                        onStartStudy = {
                                            activeStudyDeckId = deckId
                                            studyViewModel.startStudySession(deckId)
                                            currentScreen = "study"
                                        },
                                        onStartQuiz = { qType ->
                                            if (qType == deckId) {
                                                practiceViewModel.checkForActiveSession(deckId) { activeResponse ->
                                                    if (activeResponse != null) {
                                                        activeSessionResponse = activeResponse
                                                        showResumeDialog = true
                                                    } else {
                                                        targetSetupDeckId = deckId
                                                        showPracticeScopeSelectionDialog = true
                                                    }
                                                }
                                            } else {
                                                activeQuizDeckId = deckId
                                                activeQuizType = qType
                                                practiceViewModel.startNewPracticeSession(deckId, listOf(qType), 10)
                                                currentScreen = "practice_quiz"
                                            }
                                        },
                                        onSpeak = { text -> studyViewModel.speak(text) },
                                    )
                                } ?: run { currentScreen = "decks" }
                            }
                            "practice_quiz" -> {
                                val finalDeckId = detailDeckId ?: practiceViewModel.activeSession.value?.deckId
                                finalDeckId?.let { deckId ->
                                    PracticeQuizScreen(
                                        deckId = deckId,
                                        practiceType = activeQuizType,
                                        viewModel = practiceViewModel,
                                        onBack = {
                                            currentScreen = if (detailDeckId != null) "deck_detail" else "analytics"
                                        },
                                        onSpeak = { text -> studyViewModel.speak(text) },
                                    )
                                } ?: run { currentScreen = "decks" }
                            }
                            "study" -> StudyFlashcardsScreen(
                                viewModel = studyViewModel,
                                onFinish = {
                                    currentScreen = if (activeStudyDeckId != null) {
                                        "deck_detail"
                                    } else {
                                        "home"
                                    }
                                }
                            )
                            "analytics" -> AnalyticsScreen(
                                viewModel = practiceViewModel,
                                stats = databaseAnalytics,
                                onSessionClick = { sessionId ->
                                    activeQuizType = "MIXED"
                                    practiceViewModel.loadPastSessionResults(sessionId) {
                                        currentScreen = "practice_quiz"
                                    }
                                },
                                onRefreshStats = { dashboardViewModel.fetchDashboardAnalytics() },
                            )
                            "profile" -> ProfileScreen(
                                viewModel = profileSettingsViewModel,
                                onNavigateToSettings = { currentScreen = "settings" }
                            )

                            "settings" -> SettingsScreen(
                                viewModel = profileSettingsViewModel,
                                onBackClick = { currentScreen = "profile" }
                            )
                        }
                    }

                    // Floating Island Tab Bar overlay
                    if (currentScreen != "study" && currentScreen != "practice_quiz") {
                        val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                        val isThreeButtonNav = navBarHeight > 20.dp

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .then(
                                    if (isThreeButtonNav) {
                                        Modifier.background(Color.Transparent)
                                    } else {
                                        Modifier.navigationBarsPadding() // Float above gesture bar
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = if (isThreeButtonNav) {
                                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 0.dp) // Docked flat bottom
                                } else {
                                    RoundedCornerShape(24.dp) // Fully rounded capsule
                                },
                                tonalElevation = 8.dp,
                                shadowElevation = 8.dp,
                                color = Color(0xFFFFF9F2), // System warm yellow/cream background
                                border = BorderStroke(1.dp, Color(0xFFE8E2DA)),
                                modifier = Modifier
                                    .then(
                                        if (isThreeButtonNav) {
                                            Modifier.fillMaxWidth() // Stretch to full width
                                        } else {
                                            Modifier
                                                .padding(start = 24.dp, end = 24.dp, bottom = 4.dp) // Floating island aligned to cards
                                                .fillMaxWidth()
                                        }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (isThreeButtonNav) {
                                                Modifier.navigationBarsPadding() // Inner padding to place items above system buttons
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val screens = listOf("home", "decks", "analytics", "profile")
                                    screens.forEach { screen ->
                                        val isSelected = when (screen) {
                                            "home" -> currentScreen == "home"
                                            "decks" -> currentScreen == "decks" || currentScreen == "deck_detail"
                                            "analytics" -> currentScreen == "analytics"
                                            "profile" -> currentScreen == "profile"
                                            else -> false
                                        }
                                        val icon = when (screen) {
                                            "home" -> Icons.Default.Home
                                            "decks" -> Icons.Default.MenuBook
                                            "analytics" -> Icons.Default.BarChart
                                            "profile" -> Icons.Default.Person
                                            else -> Icons.Default.Home
                                        }
                                        val contentDesc = when (screen) {
                                            "home" -> stringResource(R.string.bottom_nav_home)
                                            "decks" -> stringResource(R.string.bottom_nav_decks)
                                            "analytics" -> stringResource(R.string.bottom_nav_stats)
                                            "profile" -> stringResource(R.string.bottom_nav_profile)
                                            else -> ""
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .width(72.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(
                                                    if (isSelected) Color(0xFF0D9488).copy(alpha = 0.12f) else Color.Transparent
                                                )
                                                .clickable {
                                                    currentScreen = if (screen == "decks") "decks" else screen
                                                    detailDeckId = null
                                                }
                                                .padding(vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = contentDesc,
                                                tint = if (isSelected) Color(0xFF0D9488) else Color(0xFF7C776E),
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = when (screen) {
                                                    "home" -> stringResource(R.string.bottom_nav_home)
                                                    "decks" -> stringResource(R.string.bottom_nav_decks)
                                                    "analytics" -> stringResource(R.string.bottom_nav_stats)
                                                    "profile" -> stringResource(R.string.bottom_nav_profile)
                                                    else -> ""
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color(0xFF0D9488) else Color(0xFF7C776E)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
