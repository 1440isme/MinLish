package com.minlish.core.presentation

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.minlish.core.data.model.*
import com.minlish.core.data.repository.SettingsRepository
import com.minlish.core.data.repository.VocabularyRepository
import com.minlish.core.data.repository.AuthRepository
import com.minlish.core.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MinLishViewModel(
    application: Application,
    private val vocabularyRepository: VocabularyRepository,
    val settingsRepository: SettingsRepository,
    val authRepository: AuthRepository,
    val userRepository: UserRepository
) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    // General app states
    val fullName = settingsRepository.fullName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "Guest"
    )
    val avatarUrl = settingsRepository.avatarUrl.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ""
    )
    val email = settingsRepository.email.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ""
    )
    val learningGoal = settingsRepository.learningGoal.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "TOEIC"
    )
    val targetLevel = settingsRepository.targetLevel.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "TOEIC 600+"
    )
    val targetLevelId = settingsRepository.targetLevelId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ""
    )
    val dailyNewWordsGoal = settingsRepository.dailyNewWordsGoal.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 10
    )
    val dailyReminderTime = settingsRepository.dailyReminderTime
    val isOnboarded = settingsRepository.isOnboarded.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )
    val isDarkTheme = settingsRepository.isDarkTheme
    val isMockMode = settingsRepository.isMockServiceOn

    // TTS Engine
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    // Decks
    private val _decksList = MutableStateFlow<List<DeckEntity>>(emptyList())
    val decksList: StateFlow<List<DeckEntity>> = _decksList

    // Selected deck detail and words
    private val _selectedDeck = MutableStateFlow<DeckEntity?>(null)
    val selectedDeck: StateFlow<DeckEntity?> = _selectedDeck

    private val _vocabulariesInSelectedDeck = MutableStateFlow<List<VocabularyEntity>>(emptyList())
    val vocabulariesInSelectedDeck: StateFlow<List<VocabularyEntity>> = _vocabulariesInSelectedDeck

    // Active Study Flashcards
    private val _activeFlashcards = MutableStateFlow<List<VocabularyWithReviewCard>>(emptyList())
    val activeFlashcards: StateFlow<List<VocabularyWithReviewCard>> = _activeFlashcards

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped

    // Practice Session
    private val _practiceSessions = MutableStateFlow<List<PracticeSessionEntity>>(emptyList())
    val practiceSessions: StateFlow<List<PracticeSessionEntity>> = _practiceSessions

    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex

    private val _quizCorrectCount = MutableStateFlow(0)
    val quizCorrectCount: StateFlow<Int> = _quizCorrectCount

    private val _quizFinished = MutableStateFlow(false)
    val quizFinished: StateFlow<Boolean> = _quizFinished

    // Dashboard Statistics
    private val _dashboardAnalytics = MutableStateFlow(
        DashboardAnalyticsDto(0, 0, 0, 0, 80.0f, 5, 0)
    )
    val dashboardAnalytics: StateFlow<DashboardAnalyticsDto> = _dashboardAnalytics

    init {
        // Initialize TTS
        tts = TextToSpeech(application, this)

        // Sync Decks dynamically based on chosen goal
        viewModelScope.launch {
            learningGoal.flatMapLatest { goal ->
                vocabularyRepository.getDecksByGoalFlow(goal)
            }.collect { list ->
                _decksList.value = list
            }
        }

        // Keep local dashboard aggregates up-to-date
        viewModelScope.launch {
            combine(
                learningGoal,
                dailyNewWordsGoal,
                vocabularyRepository.getDueCountFlow()
            ) { _, dailyGoal, due ->
                val stats = vocabularyRepository.getLocalDashboardAnalytics(dailyGoal)
                _dashboardAnalytics.value = stats.copy(dueToday = due)
            }
        }

        // Monitor practice sessions
        viewModelScope.launch {
            vocabularyRepository.getPracticeSessionsFlow().collect { list ->
                _practiceSessions.value = list
            }
        }

        // Synchronize profile if already onboarded
        viewModelScope.launch {
            isOnboarded.collect { onboarded ->
                if (onboarded) {
                    fetchUserProfile()
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsInitialized = true
            }
        }
    }

    fun speak(text: String) {
        if (isTtsInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }

    // ONBOARDING PROFILE CONFIG
    fun performOnboarding(name: String, goal: String, level: String, words: Int, reminder: String) {
        viewModelScope.launch {
            settingsRepository.saveOnboarding(name, goal, level, words, reminder)
            // Re-seed if needed, or trigger visual updates immediately
            vocabularyRepository.seedDatabaseAsNecessary()
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                userRepository.getProfile()
            } catch (e: Exception) {
                // Silently fail if network is not ready
            }
        }
    }

    fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            try {
                userRepository.updateDailyNewWordsGoal(goal)
            } catch (e: Exception) {
                // Handle or ignore network issue
            }
        }
    }

    fun updateTargetLevel(levelId: String) {
        viewModelScope.launch {
            try {
                userRepository.updateProfile(targetLevelId = levelId)
            } catch (e: Exception) {
                // Handle or ignore network issue
            }
        }
    }

    fun updateLearningGoal(goal: String) {
        viewModelScope.launch {
            try {
                val defaultTargetLevelId = if (goal == "TOEIC") {
                    "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2" // TOEIC 600+
                } else {
                    "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3" // IELTS 6.5+
                }
                userRepository.updateProfile(
                    learningGoal = goal,
                    targetLevelId = defaultTargetLevelId
                )
            } catch (e: Exception) {
                // Handle or ignore network issue
            }
        }
    }

    fun updateProfile(newName: String, newAvatarUrl: String) {
        viewModelScope.launch {
            try {
                userRepository.updateProfile(fullName = newName, avatarUrl = newAvatarUrl)
            } catch (e: Exception) {
                // Handle or ignore network issue
            }
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

    fun resetAppData() {
        viewModelScope.launch {
            settingsRepository.clearOnboarding()
            vocabularyRepository.deleteDeck("") // dummy trigger or can add clean DB trigger
        }
    }

    // SELECT DECK
    fun selectDeck(deckId: String) {
        viewModelScope.launch {
            val deck = vocabularyRepository.getDeckById(deckId)
            _selectedDeck.value = deck
            if (deck != null) {
                vocabularyRepository.getVocabulariesInDeckFlow(deckId).collect { list ->
                    _vocabulariesInSelectedDeck.value = list
                }
            }
        }
    }

    // SYSTEM / USER DECK ACTIONS
    fun createCustomDeck(name: String, description: String, tags: List<String>) {
        viewModelScope.launch {
            vocabularyRepository.createDeck(name, description, tags, learningGoal.value, targetLevel.value)
        }
    }

    fun deleteCustomDeck(deckId: String) {
        viewModelScope.launch {
            vocabularyRepository.deleteDeck(deckId)
        }
    }

    // CUSTOM VOCAB CRUD
    fun addCustomVocabulary(
        deckId: String,
        word: String,
        pronunciation: String,
        meaning: String,
        descEn: String,
        example: String,
        collocation: String,
        related: String,
        note: String
    ) {
        viewModelScope.launch {
            vocabularyRepository.addVocabularyToDeck(
                deckId, word, pronunciation, meaning, descEn, example, collocation, related, note
            )
            // Trigger selection update
            selectDeck(deckId)
        }
    }

    fun deleteCustomVocabulary(vocabId: String, deckId: String) {
        viewModelScope.launch {
            vocabularyRepository.deleteVocabulary(vocabId)
            selectDeck(deckId)
        }
    }

    fun updateCustomVocabulary(
        id: String,
        deckId: String,
        word: String,
        pronunciation: String,
        meaning: String,
        descEn: String,
        example: String,
        collocation: String,
        related: String,
        note: String
    ) {
        viewModelScope.launch {
            vocabularyRepository.updateVocabulary(
                id, word, pronunciation, meaning, descEn, example, collocation, related, note
            )
            selectDeck(deckId)
        }
    }

    // CSV IMPORT TRIGGER
    fun importCsv(deckId: String, csvContent: String, onComplete: (ImportCsvResponse) -> Unit) {
        viewModelScope.launch {
            val response = vocabularyRepository.importCsvContent(deckId, csvContent)
            selectDeck(deckId)
            onComplete(response)
        }
    }

    // FLASHCARD LEARNING SESSIONS
    fun startStudySession() {
        viewModelScope.launch {
            val words = vocabularyRepository.getDueReviewAndNewWords(dailyNewWordsGoal.value)
            _activeFlashcards.value = words
            _currentCardIndex.value = 0
            _isCardFlipped.value = false
        }
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun submitReviewRating(vocabId: String, rating: String) {
        viewModelScope.launch {
            vocabularyRepository.processVocabReview(vocabId, rating)
            
            // Advance to next card or end
            if (_currentCardIndex.value < _activeFlashcards.value.size - 1) {
                _currentCardIndex.value += 1
                _isCardFlipped.value = false
            } else {
                // End session
                _activeFlashcards.value = emptyList()
            }
        }
    }

    // MINI PRACTICE QUIZ ENGINE
    fun startQuizPractice(deckId: String, type: String) {
        viewModelScope.launch {
            val vocabs = vocabularyRepository.getVocabulariesInDeck(deckId)
            if (vocabs.isEmpty()) {
                _quizQuestions.value = emptyList()
                return@launch
            }

            // Generate questions
            val quizList = mutableListOf<QuizQuestion>()
            val allMeanings = vocabs.map { it.meaning }

            vocabs.shuffled().take(10).forEach { item ->
                if (type == "MULTIPLE_CHOICE") {
                    // Random choices
                    val distractors = allMeanings.filter { it != item.meaning }.shuffled().take(3)
                    val choices = (distractors + item.meaning).shuffled()
                    quizList.add(
                        QuizQuestion(
                            vocabulary = item,
                            questionType = "WORD_TO_MEANING",
                            questionText = "What is the meaning of the word '${item.word}'?",
                            choices = choices,
                            correctAnswer = item.meaning
                        )
                    )
                } else {
                    // Cloze Test
                    val textQuestion = if (item.example.isNotEmpty() && item.example.contains(item.word, ignoreCase = true)) {
                        // Hide word in example
                        item.example.replace(item.word, "________", ignoreCase = true)
                    } else {
                        "Complete the English vocabulary definition: ${item.descriptionEn.ifEmpty { "A key term meaning: " + item.meaning }}"
                    }
                    quizList.add(
                        QuizQuestion(
                            vocabulary = item,
                            questionType = "FILL_IN_BLANK",
                            questionText = textQuestion,
                            choices = emptyList(),
                            correctAnswer = item.word
                        )
                    )
                }
            }

            _quizQuestions.value = quizList
            _currentQuizIndex.value = 0
            _quizCorrectCount.value = 0
            _quizFinished.value = false
        }
    }

    fun submitQuizAnswer(deckId: String, type: String, userAnswer: String) {
        val currentQuestion = _quizQuestions.value.getOrNull(_currentQuizIndex.value) ?: return
        val isCorrect = userAnswer.trim().equals(currentQuestion.correctAnswer.trim(), ignoreCase = true)

        if (isCorrect) {
            _quizCorrectCount.value += 1
        }

        if (_currentQuizIndex.value < _quizQuestions.value.size - 1) {
            _currentQuizIndex.value += 1
        } else {
            // Last question complete! Save practice session statistics to DB.
            _quizFinished.value = true
            viewModelScope.launch {
                vocabularyRepository.savePracticeSession(
                    deckId = deckId,
                    type = type,
                    total = _quizQuestions.value.size,
                    correct = _quizCorrectCount.value
                )
            }
        }
    }
}

class MinLishViewModelFactory(
    private val application: Application,
    private val vocabularyRepository: VocabularyRepository,
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MinLishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MinLishViewModel(
                application = application,
                vocabularyRepository = vocabularyRepository,
                settingsRepository = settingsRepository,
                authRepository = authRepository,
                userRepository = userRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
