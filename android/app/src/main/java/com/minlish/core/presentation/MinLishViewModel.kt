package com.minlish.core.presentation

import android.app.Application
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.minlish.core.data.model.*
import com.minlish.core.data.repository.AuthRepository
import com.minlish.core.data.repository.SettingsRepository
import com.minlish.core.data.repository.UserRepository
import com.minlish.core.data.repository.VocabularyRepository
import com.minlish.core.network.dto.ExistingVocabularyItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

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
    val targetLevel = settingsRepository.targetLevel
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

    private val _isLoadingDecks = MutableStateFlow(false)
    val isLoadingDecks: StateFlow<Boolean> = _isLoadingDecks

    private val _isLoadingDeckDetail = MutableStateFlow(false)
    val isLoadingDeckDetail: StateFlow<Boolean> = _isLoadingDeckDetail

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage

    private val _sameWordWarning = MutableStateFlow<SameWordWarningState?>(null)
    val sameWordWarning: StateFlow<SameWordWarningState?> = _sameWordWarning

    private val _favoritedSourceIds = MutableStateFlow<Set<String>>(emptySet())
    val favoritedSourceIds: StateFlow<Set<String>> = _favoritedSourceIds

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
                    refreshDecks()
                    refreshFavoritedIds()
                }
            }
        }
    }

    fun clearLastError() {
        _lastErrorMessage.value = null
    }

    fun dismissSameWordWarning() {
        _sameWordWarning.value = null
    }

    fun refreshDecks() {
        viewModelScope.launch {
            _isLoadingDecks.value = true
            try {
                val decks = vocabularyRepository.listDecks()
                _decksList.value = decks
                _lastErrorMessage.value = null
            } catch (e: Exception) {
                _lastErrorMessage.value = e.message ?: "Failed to load decks"
            } finally {
                _isLoadingDecks.value = false
            }
        }
    }

    fun refreshFavoritedIds() {
        viewModelScope.launch {
            try {
                vocabularyRepository.refreshFavoritedSourceIds()
                _favoritedSourceIds.value = vocabularyRepository.getFavoritedSourceIds()
            } catch (_: Exception) {
                // Favorites deck may not exist until register side-effect is done
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
            _isLoadingDeckDetail.value = true
            try {
                val deck = vocabularyRepository.getDeckById(deckId)
                _selectedDeck.value = deck
                if (deck != null) {
                    val list = vocabularyRepository.getVocabulariesInDeck(deckId)
                    _vocabulariesInSelectedDeck.value = list
                }
                vocabularyRepository.refreshFavoritedSourceIds()
                _favoritedSourceIds.value = vocabularyRepository.getFavoritedSourceIds()
                _lastErrorMessage.value = null
            } catch (e: Exception) {
                _lastErrorMessage.value = e.message ?: "Failed to load deck"
            } finally {
                _isLoadingDeckDetail.value = false
            }
        }
    }

    fun isVocabFavorited(vocab: VocabularyEntity): Boolean {
        val sourceId = vocabularyRepository.favoriteSourceIdFor(vocab)
        return favoritedSourceIds.value.contains(sourceId) ||
            vocabularyRepository.isFavorited(sourceId)
    }

    // SYSTEM / USER DECK ACTIONS
    fun createCustomDeck(name: String, description: String, tags: List<String>) {
        viewModelScope.launch {
            try {
                vocabularyRepository.createDeck(name, description, tags)
                refreshDecks()
            } catch (e: Exception) {
                _lastErrorMessage.value = e.message ?: "Failed to create deck"
            }
        }
    }

    fun deleteCustomDeck(deckId: String) {
        viewModelScope.launch {
            try {
                vocabularyRepository.deleteDeck(deckId)
                refreshDecks()
            } catch (e: Exception) {
                _lastErrorMessage.value = e.message ?: "Failed to delete deck"
            }
        }
    }

    fun updateCustomDeck(deckId: String, name: String, description: String, tags: List<String>) {
        viewModelScope.launch {
            try {
                vocabularyRepository.updateDeck(deckId, name, description, tags)
                refreshDecks()
                selectDeck(deckId)
            } catch (e: Exception) {
                _lastErrorMessage.value = e.message ?: "Failed to update deck"
            }
        }
    }

    // CUSTOM VOCAB CRUD
    fun addCustomVocabulary(
        deckId: String,
        word: String,
        pronunciation: String,
        partOfSpeech: String,
        meaning: String,
        descEn: String,
        example: String,
        collocation: String,
        related: String,
        note: String,
        onResult: (AddVocabularyResult) -> Unit,
    ) {
        viewModelScope.launch {
            val result = vocabularyRepository.addVocabularyToDeck(
                deckId, word, pronunciation, partOfSpeech, meaning, descEn, example, collocation, related, note,
            )
            when (result) {
                is AddVocabularyResult.Success -> {
                    selectDeck(deckId)
                    _sameWordWarning.value = null
                }
                is AddVocabularyResult.SameWordDifferentMeaning -> {
                    _sameWordWarning.value = SameWordWarningState(
                        message = result.message,
                        existingItems = result.existingItems,
                        pending = result.pendingRequest,
                    )
                }
                else -> Unit
            }
            onResult(result)
        }
    }

    fun confirmAddDifferentMeaning(onResult: (AddVocabularyResult) -> Unit) {
        val warning = _sameWordWarning.value ?: return
        viewModelScope.launch {
            val result = vocabularyRepository.confirmAddVocabularyWithDifferentMeaning(warning.pending)
            when (result) {
                is AddVocabularyResult.Success -> {
                    selectDeck(warning.pending.deckId)
                    _sameWordWarning.value = null
                }
                is AddVocabularyResult.SameWordDifferentMeaning -> {
                    _sameWordWarning.value = SameWordWarningState(
                        message = result.message,
                        existingItems = result.existingItems,
                        pending = result.pendingRequest,
                    )
                }
                else -> Unit
            }
            onResult(result)
        }
    }

    fun toggleFavorite(vocab: VocabularyEntity, onResult: (Boolean, String?) -> Unit) {
        val sourceId = vocabularyRepository.favoriteSourceIdFor(vocab)
        viewModelScope.launch {
            val isCurrentlyFavorited = isVocabFavorited(vocab)
            val result = if (isCurrentlyFavorited) {
                vocabularyRepository.unfavoriteVocabulary(sourceId)
            } else {
                vocabularyRepository.favoriteVocabulary(sourceId)
            }
            when (result) {
                is FavoriteResult.Success -> {
                    _favoritedSourceIds.value = vocabularyRepository.getFavoritedSourceIds()
                    _selectedDeck.value?.let { deck ->
                        if (deck.isFavoritesDeck) selectDeck(deck.id)
                    }
                    onResult(!isCurrentlyFavorited, null)
                }
                is FavoriteResult.Failure -> onResult(isCurrentlyFavorited, result.message)
            }
        }
    }

    fun deleteCustomVocabulary(vocabId: String, deckId: String) {
        viewModelScope.launch {
            try {
                vocabularyRepository.deleteVocabulary(vocabId)
                selectDeck(deckId)
                refreshDecks()
            } catch (e: Exception) {
                _lastErrorMessage.value = e.message ?: "Failed to delete vocabulary"
            }
        }
    }

    fun updateCustomVocabulary(
        id: String,
        deckId: String,
        word: String,
        pronunciation: String,
        partOfSpeech: String,
        meaning: String,
        descEn: String,
        example: String,
        collocation: String,
        related: String,
        note: String
    ) {
        viewModelScope.launch {
            try {
                vocabularyRepository.updateVocabulary(
                    id, word, pronunciation, partOfSpeech, meaning, descEn, example, collocation, related, note
                )
                selectDeck(deckId)
            } catch (e: Exception) {
                _lastErrorMessage.value = e.message ?: "Failed to update vocabulary"
            }
        }
    }

    // CSV IMPORT TRIGGER
    fun importCsv(deckId: String, fileUri: Uri, onComplete: (ImportCsvResponse) -> Unit) {
        viewModelScope.launch {
            try {
                val response = vocabularyRepository.importCsvFile(deckId, fileUri)
                selectDeck(deckId)
                refreshDecks()
                onComplete(response)
            } catch (e: Exception) {
                onComplete(
                    ImportCsvResponse(
                        success = false,
                        importedCount = 0,
                        errors = listOf(e.message ?: "Import failed"),
                    )
                )
            }
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

data class SameWordWarningState(
    val message: String,
    val existingItems: List<ExistingVocabularyItemDto>,
    val pending: PendingVocabularyRequest,
)

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
