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
import com.minlish.core.network.ApiErrorParser
import com.minlish.core.network.dto.CreateSessionResponse
import com.minlish.core.network.dto.ExistingVocabularyItemDto
import com.minlish.core.network.dto.PracticeAnswerDto
import com.minlish.core.network.dto.PracticeQuestionDto
import com.minlish.core.network.dto.PracticeSessionDto
import com.minlish.core.network.dto.PracticeSessionSummaryDto
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

    private val _isLoadingDecks = MutableStateFlow(false)
    val isLoadingDecks: StateFlow<Boolean> = _isLoadingDecks
    private var currentDeckSearchQuery: String? = null

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

    private val _quizQuestions = MutableStateFlow<List<PracticeQuestionDto>>(emptyList())
    val quizQuestions: StateFlow<List<PracticeQuestionDto>> = _quizQuestions

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex

    private val _quizCorrectCount = MutableStateFlow(0)
    val quizCorrectCount: StateFlow<Int> = _quizCorrectCount

    private val _quizFinished = MutableStateFlow(false)
    val quizFinished: StateFlow<Boolean> = _quizFinished

    private val _activeSession = MutableStateFlow<PracticeSessionDto?>(null)
    val activeSession: StateFlow<PracticeSessionDto?> = _activeSession

    private val _lastSubmitResult = MutableStateFlow<PracticeAnswerDto?>(null)
    val lastSubmitResult: StateFlow<PracticeAnswerDto?> = _lastSubmitResult

    private val _finishSummary = MutableStateFlow<PracticeSessionSummaryDto?>(null)
    val finishSummary: StateFlow<PracticeSessionSummaryDto?> = _finishSummary

    private val _practiceError = MutableStateFlow<String?>(null)
    val practiceError: StateFlow<String?> = _practiceError

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

    fun refreshDecks(searchQuery: String? = currentDeckSearchQuery) {
        viewModelScope.launch {
            _isLoadingDecks.value = true
            try {
                currentDeckSearchQuery = searchQuery?.trim()?.takeIf { it.isNotEmpty() }
                val decks = vocabularyRepository.listDecks(currentDeckSearchQuery)
                _decksList.value = decks
                _lastErrorMessage.value = null
            } catch (e: Exception) {
                _lastErrorMessage.value = ApiErrorParser.humanMessage(e, "Failed to load decks")
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
                _lastErrorMessage.value = ApiErrorParser.humanMessage(e, "Failed to load deck")
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
                _lastErrorMessage.value = ApiErrorParser.humanMessage(e, "Failed to create deck")
            }
        }
    }

    fun deleteCustomDeck(deckId: String) {
        viewModelScope.launch {
            try {
                vocabularyRepository.deleteDeck(deckId)
                refreshDecks()
            } catch (e: Exception) {
                _lastErrorMessage.value = ApiErrorParser.humanMessage(e, "Failed to delete deck")
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
                _lastErrorMessage.value = ApiErrorParser.humanMessage(e, "Failed to update deck")
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
                _lastErrorMessage.value = ApiErrorParser.humanMessage(e, "Failed to delete vocabulary")
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
                _lastErrorMessage.value = ApiErrorParser.humanMessage(e, "Failed to update vocabulary")
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
    fun checkForActiveSession(deckId: String, onResult: (CreateSessionResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                _practiceError.value = null
                val response = vocabularyRepository.getActivePracticeSession(deckId)
                onResult(response)
            } catch (e: Exception) {
                _practiceError.value = e.localizedMessage
                onResult(null)
            }
        }
    }

    fun startNewPracticeSession(deckId: String, practiceTypes: List<String>, totalQuestions: Int) {
        viewModelScope.launch {
            try {
                _practiceError.value = null
                _quizQuestions.value = emptyList()
                val response = vocabularyRepository.createPracticeSession(deckId, practiceTypes, totalQuestions)
                _activeSession.value = response.session
                _quizQuestions.value = response.questions
                _currentQuizIndex.value = 0
                _quizCorrectCount.value = 0
                _quizFinished.value = false
                _lastSubmitResult.value = null
                _finishSummary.value = null
            } catch (e: Exception) {
                _practiceError.value = "Failed to start practice session: ${e.localizedMessage}"
            }
        }
    }

    fun resumeActiveSession(response: CreateSessionResponse) {
        _activeSession.value = response.session
        _quizQuestions.value = response.questions
        val unansweredIndex = response.questions.indexOfFirst { !it.answered }
        _currentQuizIndex.value = if (unansweredIndex != -1) unansweredIndex else 0
        _quizCorrectCount.value = response.session.correctAnswers
        _quizFinished.value = false
        _lastSubmitResult.value = null
        _finishSummary.value = null
    }

    fun cancelActiveSession(sessionId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _practiceError.value = null
                vocabularyRepository.cancelPracticeSession(sessionId)
                _activeSession.value = null
                _quizQuestions.value = emptyList()
                _currentQuizIndex.value = 0
                _quizCorrectCount.value = 0
                _quizFinished.value = false
                _lastSubmitResult.value = null
                _finishSummary.value = null
                onComplete()
            } catch (e: Exception) {
                _practiceError.value = "Failed to cancel session: ${e.localizedMessage}"
            }
        }
    }

    fun submitPracticeAnswer(userAnswer: String?) {
        val session = _activeSession.value ?: return
        val index = _currentQuizIndex.value
        viewModelScope.launch {
            try {
                _practiceError.value = null
                val result = vocabularyRepository.submitPracticeAnswer(session.id, index, userAnswer)
                _lastSubmitResult.value = result
                if (result.isCorrect) {
                    _quizCorrectCount.value += 1
                }
            } catch (e: Exception) {
                _practiceError.value = "Failed to submit answer: ${e.localizedMessage}"
            }
        }
    }

    fun advanceToNextQuestion() {
        val questions = _quizQuestions.value
        val currentIndex = _currentQuizIndex.value
        _lastSubmitResult.value = null
        
        if (currentIndex < questions.size - 1) {
            _currentQuizIndex.value += 1
        } else {
            finishCurrentSession()
        }
    }

    fun finishCurrentSession() {
        val session = _activeSession.value ?: return
        viewModelScope.launch {
            try {
                _practiceError.value = null
                val response = vocabularyRepository.finishPracticeSession(session.id)
                _finishSummary.value = response.summary
                _quizFinished.value = true
            } catch (e: Exception) {
                _practiceError.value = "Failed to finish session: ${e.localizedMessage}"
            }
        }
    }

    fun clearPracticeError() {
        _practiceError.value = null
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
