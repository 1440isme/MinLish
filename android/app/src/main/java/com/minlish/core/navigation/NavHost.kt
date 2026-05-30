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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import com.minlish.core.network.dto.CreateSessionResponse
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun MinLishAppContent(viewModel: MinLishViewModel) {
    val isOnboarded by viewModel.isOnboarded.collectAsState()
    val databaseAnalytics by viewModel.dashboardAnalytics.collectAsState()

    var currentScreen by remember { mutableStateOf("home") }
    var detailDeckId by remember { mutableStateOf<String?>(null) }
    var activeQuizDeckId by remember { mutableStateOf<String?>(null) }
    var activeQuizType by remember { mutableStateOf("MULTIPLE_CHOICE") }
    var activeStudyDeckId by remember { mutableStateOf<String?>(null) }

    // Dialog state variables
    var showResumeDialog by remember { mutableStateOf(false) }
    var activeSessionResponse by remember { mutableStateOf<CreateSessionResponse?>(null) }
    
    var showSetupDialog by remember { mutableStateOf(false) }
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
                    "Resume practice?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    "You have an incomplete practice session in this deck. Would you like to resume it or start a new one?",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResumeDialog = false
                        activeSessionResponse?.let {
                            viewModel.resumeActiveSession(it)
                            currentScreen = "practice_quiz"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Resume")
                }
            },
            dismissButton = {
                Row {
                    OutlinedButton(
                        onClick = {
                            showResumeDialog = false
                            activeSessionResponse?.session?.id?.let { sessionId ->
                                viewModel.cancelActiveSession(sessionId) {
                                    targetSetupDeckId = detailDeckId
                                    showSetupDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Text("Start New")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { showResumeDialog = false },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Cancel")
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Dialog for practice setup configuration
    if (showSetupDialog) {
        val totalWordsInDeck = viewModel.vocabulariesInSelectedDeck.value.size
        
        var questionCount by remember(totalWordsInDeck) { 
            mutableStateOf(if (totalWordsInDeck > 10) 10 else totalWordsInDeck) 
        }
        
        var isMultipleChoiceChecked by remember { mutableStateOf(true) }
        var isFillInBlankChecked by remember { mutableStateOf(true) }
        var isListeningChecked by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showSetupDialog = false },
            title = {
                Text(
                    "Practice Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Number of questions:",
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
                            valueRange = 1f..totalWordsInDeck.coerceAtLeast(1).toFloat(),
                            steps = if (totalWordsInDeck > 1) totalWordsInDeck - 2 else 0,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFBBF24),
                                activeTrackColor = Color(0xFFFBBF24),
                                inactiveTrackColor = Color(0xFFE5E7EB)
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "$questionCount questions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Question types:",
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
                        Text("Multiple Choice (Word/Meaning)", fontSize = 14.sp)
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
                        Text("Fill in the Blank", fontSize = 14.sp)
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
                        Text("Listening Practice", fontSize = 14.sp)
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
                                android.widget.Toast.makeText(
                                    context,
                                    "This deck has no words yet. Please add words before practicing!",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                return@let
                            }
                            viewModel.startNewPracticeSession(deckId, types, questionCount)
                            showSetupDialog = false
                            currentScreen = "practice_quiz"
                        }
                    },
                    enabled = isAnyChecked,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Start")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSetupDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Cancel")
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
                                    onStartQuiz = { _ ->
                                        viewModel.checkForActiveSession(deckId) { activeResponse ->
                                            if (activeResponse != null) {
                                                activeSessionResponse = activeResponse
                                                showResumeDialog = true
                                            } else {
                                                targetSetupDeckId = deckId
                                                showSetupDialog = true
                                            }
                                        }
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
                            detailDeckId?.let { deckId ->
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
