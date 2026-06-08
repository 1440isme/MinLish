package com.minlish.core.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.minlish.MinLishApplication
import com.minlish.R
import com.minlish.core.network.dto.CreateSessionResponse
import com.minlish.core.data.model.DashboardAnalyticsDto
import com.minlish.core.presentation.MainViewModel
import com.minlish.core.utils.showToast
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
import com.minlish.feature.settings.presentation.SettingsScreen

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
        if (isOnboarded == true) {
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
        when (isOnboarded) {
            null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFF9F2)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF0D9488))
                }
            }

            false -> {
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
            }

            true -> {
                AuthenticatedMinLishAppContent(
                    dashboardViewModel = dashboardViewModel,
                    decksViewModel = decksViewModel,
                    deckDetailViewModel = deckDetailViewModel,
                    studyViewModel = studyViewModel,
                    practiceViewModel = practiceViewModel,
                    profileSettingsViewModel = profileSettingsViewModel,
                    databaseAnalytics = databaseAnalytics,
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedMinLishAppContent(
    dashboardViewModel: DashboardViewModel,
    decksViewModel: DecksViewModel,
    deckDetailViewModel: DeckDetailViewModel,
    studyViewModel: StudyFlashcardsViewModel,
    practiceViewModel: PracticeQuizViewModel,
    profileSettingsViewModel: ProfileSettingsViewModel,
    databaseAnalytics: DashboardAnalyticsDto,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState() // lay phan tu hien tai tren top cua back stack (man hinh hien tai)
    val currentRoute = navBackStackEntry?.destination?.route // safe call
    val studyLastErrorMessage by studyViewModel.lastErrorMessage.collectAsState()

    var detailDeckId by remember { mutableStateOf<String?>(null) }
    var activeQuizType by remember { mutableStateOf("MULTIPLE_CHOICE") }

    val decksLazyListState = rememberLazyListState()
    var decksSelectedGoalFilter by remember { mutableStateOf("MY_DECKS") }
    var decksSearchKey by remember { mutableStateOf("") }

    var showResumeDialog by remember { mutableStateOf(false) }
    var activeSessionResponse by remember { mutableStateOf<CreateSessionResponse?>(null) }
    var showSetupDialog by remember { mutableStateOf(false) }
    var showPracticeScopeSelectionDialog by remember { mutableStateOf(false) }
    var practiceScope by remember { mutableStateOf("LEARNED_ONLY") }
    var targetSetupDeckId by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    LaunchedEffect(studyLastErrorMessage) {
        studyLastErrorMessage?.let { message ->
            showToast(context, message, android.widget.Toast.LENGTH_LONG)
            studyViewModel.clearLastError()
        }
    }

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
                            navController.navigate(AppDestination.PRACTICE)
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

    if (showPracticeScopeSelectionDialog) {
        val vocabList by deckDetailViewModel.vocabulariesInSelectedDeck.collectAsState()
        val totalWordsInDeck = vocabList.size
        val deckLearningProgress by deckDetailViewModel.selectedDeckLearningProgress.collectAsState()
        val newWordsAvailable = deckLearningProgress?.newWordsAvailable ?: totalWordsInDeck
        val learnedWordsCount = (totalWordsInDeck - newWordsAvailable).coerceIn(0, totalWordsInDeck)

        val notEnoughLearnedWordsMsg = stringResource(
            R.string.practice_mode_not_enough_learned_words,
            learnedWordsCount
        )
        val notEnoughAllWordsMsg = stringResource(
            R.string.practice_mode_not_enough_all_words,
            totalWordsInDeck
        )

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = {
                            if (learnedWordsCount < 4) {
                                showToast(
                                    context,
                                    notEnoughLearnedWordsMsg,
                                    android.widget.Toast.LENGTH_LONG
                                )
                            } else {
                                practiceScope = "LEARNED_ONLY"
                                showPracticeScopeSelectionDialog = false
                                showSetupDialog = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
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

                    Card(
                        onClick = {
                            if (totalWordsInDeck < 4) {
                                showToast(
                                    context,
                                    notEnoughAllWordsMsg,
                                    android.widget.Toast.LENGTH_LONG
                                )
                            } else {
                                practiceScope = "ALL"
                                showPracticeScopeSelectionDialog = false
                                showSetupDialog = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
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

    if (showSetupDialog) {
        val vocabList by deckDetailViewModel.vocabulariesInSelectedDeck.collectAsState()
        val totalWordsInDeck = vocabList.size
        val deckLearningProgress by deckDetailViewModel.selectedDeckLearningProgress.collectAsState()
        val newWordsAvailable = deckLearningProgress?.newWordsAvailable ?: totalWordsInDeck
        val learnedWordsCount = (totalWordsInDeck - newWordsAvailable).coerceIn(0, totalWordsInDeck)
        val maxAllowedQuestions = if (practiceScope == "LEARNED_ONLY") learnedWordsCount else totalWordsInDeck

        val emptyDeckWarning = stringResource(R.string.practice_setup_empty_deck_warning)

        var questionCount by remember(maxAllowedQuestions) {
            mutableIntStateOf(if (maxAllowedQuestions > 10) 10 else maxAllowedQuestions)
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
                            text = stringResource(
                                R.string.practice_setup_questions_count,
                                questionCount
                            ),
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

                    PracticeSetupCheckboxRow(
                        checked = isMultipleChoiceChecked,
                        onCheckedChange = { isMultipleChoiceChecked = it },
                        label = stringResource(R.string.practice_setup_type_mc)
                    )
                    PracticeSetupCheckboxRow(
                        checked = isFillInBlankChecked,
                        onCheckedChange = { isFillInBlankChecked = it },
                        label = stringResource(R.string.practice_setup_type_fib)
                    )
                    PracticeSetupCheckboxRow(
                        checked = isListeningChecked,
                        onCheckedChange = { isListeningChecked = it },
                        label = stringResource(R.string.practice_setup_type_listening)
                    )
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
                                    emptyDeckWarning,
                                    android.widget.Toast.LENGTH_LONG
                                )
                                return@let
                            }

                            detailDeckId = deckId
                            practiceViewModel.startNewPracticeSession(
                                deckId,
                                types,
                                questionCount,
                                practiceScope
                            )
                            showSetupDialog = false
                            navController.navigate(AppDestination.PRACTICE)
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

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = AppDestination.HOME,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(AppDestination.HOME) {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onStartDailyQuiz = {
                            detailDeckId = null
                            studyViewModel.startDailyQuizSession()
                            navController.navigate(AppDestination.STUDY)
                        },
                        onStartDailyNew = {
                            detailDeckId = null
                            studyViewModel.startDailyNewSession()
                            navController.navigate(AppDestination.STUDY)
                        },
                        onNavigateToDecks = {
                            navigateToTopLevel(navController, AppDestination.DECKS)
                        },
                        onResumeRecentDeck = { deckId ->
                            detailDeckId = deckId
                            deckDetailViewModel.selectDeck(deckId)
                            studyViewModel.startStudySession(deckId)
                            navController.navigate(AppDestination.STUDY)
                        },
                        onOpenRecentDeck = { deckId ->
                            detailDeckId = deckId
                            deckDetailViewModel.selectDeck(deckId)
                            navController.navigate(AppDestination.deckDetail(deckId))
                        }
                    )
                }

                composable(AppDestination.DECKS) {
                    DecksScreen(
                        viewModel = decksViewModel,
                        onDeckClick = { deckId ->
                            detailDeckId = deckId
                            deckDetailViewModel.selectDeck(deckId)
                            navController.navigate(AppDestination.deckDetail(deckId))
                        },
                        lazyListState = decksLazyListState,
                        selectedGoalFilter = decksSelectedGoalFilter,
                        onGoalFilterChange = { decksSelectedGoalFilter = it },
                        searchKey = decksSearchKey,
                        onSearchKeyChange = { decksSearchKey = it }
                    )
                }

                composable(
                    route = AppDestination.DECK_DETAIL,
                    arguments = listOf(
                        navArgument(AppDestination.DECK_ID_ARG) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val deckId = backStackEntry.arguments?.getString(AppDestination.DECK_ID_ARG)
                    if (deckId == null) {
                        LaunchedEffect(Unit) {
                            detailDeckId = null
                            navController.popBackStack()
                        }
                    } else {
                        LaunchedEffect(deckId) {
                            detailDeckId = deckId
                        }
                        DeckDetailScreen(
                            deckId = deckId,
                            viewModel = deckDetailViewModel,
                            onBack = {
                                detailDeckId = null
                                navController.popBackStack()
                            },
                            onStartStudy = {
                                detailDeckId = deckId
                                studyViewModel.startStudySession(deckId)
                                navController.navigate(AppDestination.STUDY)
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
                                    detailDeckId = deckId
                                    activeQuizType = qType
                                    practiceViewModel.startNewPracticeSession(
                                        deckId,
                                        listOf(qType),
                                        10
                                    )
                                    navController.navigate(AppDestination.PRACTICE)
                                }
                            },
                            onSpeak = { text -> deckDetailViewModel.speak(text) },
                        )
                    }
                }

                composable(AppDestination.STUDY) {
                    StudyFlashcardsScreen(
                        viewModel = studyViewModel,
                        onFinish = { navController.popBackStack() }
                    )
                }

                composable(AppDestination.PRACTICE) {
                    val activeSession by practiceViewModel.activeSession.collectAsState()
                    val finalDeckId = detailDeckId ?: activeSession?.deckId
                    if (finalDeckId == null) {
                        LaunchedEffect(Unit) {
                            navigateToTopLevel(navController, AppDestination.DECKS)
                        }
                    } else {
                        PracticeQuizScreen(
                            deckId = finalDeckId,
                            practiceType = activeQuizType,
                            viewModel = practiceViewModel,
                            onBack = { navController.popBackStack() },
                            onSpeak = { text -> practiceViewModel.speak(text) },
                        )
                    }
                }

                composable(AppDestination.ANALYTICS) {
                    AnalyticsScreen(
                        viewModel = practiceViewModel,
                        stats = databaseAnalytics,
                        onSessionClick = { sessionId ->
                            detailDeckId = null
                            activeQuizType = "MIXED"
                            practiceViewModel.loadPastSessionResults(sessionId) {
                                navController.navigate(AppDestination.PRACTICE)
                            }
                        },
                        onRefreshStats = { dashboardViewModel.fetchDashboardAnalytics() },
                    )
                }

                composable(AppDestination.PROFILE) {
                    ProfileScreen(
                        viewModel = profileSettingsViewModel,
                        onNavigateToSettings = {
                            navController.navigate(AppDestination.SETTINGS)
                        }
                    )
                }

                composable(AppDestination.SETTINGS) {
                    SettingsScreen(
                        viewModel = profileSettingsViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            if (currentRoute != AppDestination.STUDY && currentRoute != AppDestination.PRACTICE) {
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
                                Modifier.navigationBarsPadding()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = if (isThreeButtonNav) {
                            RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        } else {
                            RoundedCornerShape(24.dp)
                        },
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                        color = Color(0xFFFFF9F2),
                        border = BorderStroke(1.dp, Color(0xFFE8E2DA)),
                        modifier = Modifier.then(
                            if (isThreeButtonNav) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier
                                    .padding(start = 24.dp, end = 24.dp, bottom = 4.dp)
                                    .fillMaxWidth()
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isThreeButtonNav) {
                                        Modifier.navigationBarsPadding()
                                    } else {
                                        Modifier
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val screens = listOf(
                                BottomNavItem(
                                    route = AppDestination.HOME,
                                    icon = Icons.Default.Home,
                                    label = stringResource(R.string.bottom_nav_home)
                                ),
                                BottomNavItem(
                                    route = AppDestination.DECKS,
                                    icon = Icons.AutoMirrored.Filled.MenuBook,
                                    label = stringResource(R.string.bottom_nav_decks)
                                ),
                                BottomNavItem(
                                    route = AppDestination.ANALYTICS,
                                    icon = Icons.Default.BarChart,
                                    label = stringResource(R.string.bottom_nav_stats)
                                ),
                                BottomNavItem(
                                    route = AppDestination.PROFILE,
                                    icon = Icons.Default.Person,
                                    label = stringResource(R.string.bottom_nav_profile)
                                ),
                            )

                            screens.forEach { screen ->
                                val isSelected = when (screen.route) {
                                    AppDestination.HOME -> currentRoute == AppDestination.HOME
                                    AppDestination.DECKS -> {
                                        currentRoute == AppDestination.DECKS ||
                                            currentRoute == AppDestination.DECK_DETAIL
                                    }
                                    AppDestination.ANALYTICS -> currentRoute == AppDestination.ANALYTICS
                                    AppDestination.PROFILE -> currentRoute == AppDestination.PROFILE
                                    else -> false
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .width(72.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isSelected) {
                                                Color(0xFF0D9488).copy(alpha = 0.12f)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .clickable {
                                            detailDeckId = null
                                            navigateToTopLevel(navController, screen.route)
                                        }
                                        .padding(vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.label,
                                        tint = if (isSelected) {
                                            Color(0xFF0D9488)
                                        } else {
                                            Color(0xFF7C776E)
                                        },
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = screen.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Medium
                                        },
                                        color = if (isSelected) {
                                            Color(0xFF0D9488)
                                        } else {
                                            Color(0xFF7C776E)
                                        }
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

@Composable
private fun PracticeSetupCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFBBF24))
        )
        Text(label, fontSize = 14.sp)
    }
}

private fun navigateToTopLevel(
    navController: androidx.navigation.NavHostController,
    route: String,
) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id)
        launchSingleTop = true
    }
}

private data class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
)
