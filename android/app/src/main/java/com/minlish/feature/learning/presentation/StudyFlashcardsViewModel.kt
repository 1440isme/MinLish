package com.minlish.feature.learning.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.core.audio.SpeechResult
import com.minlish.core.audio.TextToSpeechManager
import com.minlish.core.data.model.VocabularyWithReviewCard
import com.minlish.core.data.repository.SettingsRepository
import com.minlish.core.data.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private enum class StudySessionMode {
    MIXED,
    DAILY_REVIEW,
    DAILY_NEW,
}

class StudyFlashcardsViewModel(
    private val vocabularyRepository: VocabularyRepository,
    settingsRepository: SettingsRepository,
    private val textToSpeechManager: TextToSpeechManager,
) : ViewModel() {
    private val dailyNewWordsGoal = settingsRepository.dailyNewWordsGoal.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 10,
    )

    private val _activeFlashcards = MutableStateFlow<List<VocabularyWithReviewCard>>(emptyList())
    val activeFlashcards: StateFlow<List<VocabularyWithReviewCard>> = _activeFlashcards

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped

    private val _isStudyReplayMode = MutableStateFlow(false)
    val isStudyReplayMode: StateFlow<Boolean> = _isStudyReplayMode

    private val _canReplayStudySession = MutableStateFlow(false)
    val canReplayStudySession: StateFlow<Boolean> = _canReplayStudySession

    private val _isLoadingStudySession = MutableStateFlow(false)
    val isLoadingStudySession: StateFlow<Boolean> = _isLoadingStudySession

    private val _isSubmittingReview = MutableStateFlow(false)
    val isSubmittingReview: StateFlow<Boolean> = _isSubmittingReview

    private val _canContinueStudySession = MutableStateFlow(false)
    val canContinueStudySession: StateFlow<Boolean> = _canContinueStudySession

    private val _isCheckingStudyContinuation = MutableStateFlow(false)
    val isCheckingStudyContinuation: StateFlow<Boolean> = _isCheckingStudyContinuation

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage

    fun clearLastError() {
        _lastErrorMessage.value = null
    }

    private var replayableStudyCards: List<VocabularyWithReviewCard> = emptyList()
    private var activeStudySessionMode: StudySessionMode? = null
    private var activeStudySessionDeckId: String? = null

    fun speak(text: String) {
        _lastErrorMessage.value = when (val result = textToSpeechManager.speak(text)) {
            SpeechResult.Success -> null
            SpeechResult.Loading -> "Text to speech is still loading. Please try again."
            is SpeechResult.Error -> result.message
        }
    }

    fun startStudySession(deckId: String? = null) {
        activeStudySessionMode = StudySessionMode.MIXED
        activeStudySessionDeckId = deckId
        _isLoadingStudySession.value = true
        _isSubmittingReview.value = false
        _isCheckingStudyContinuation.value = false
        _canContinueStudySession.value = false
        viewModelScope.launch {
            try {
                val words = vocabularyRepository.getDueReviewAndNewWords(
                    dailyGoal = dailyNewWordsGoal.value,
                    deckId = deckId,
                )
                launchStudyCards(words)
                _lastErrorMessage.value = null
            } catch (e: Exception) {
                clearStudyCards()
                _lastErrorMessage.value = e.message ?: "Failed to load flashcards"
            }
        }
    }

    fun startDailyQuizSession(deckId: String? = null) {
        activeStudySessionMode = StudySessionMode.DAILY_REVIEW
        activeStudySessionDeckId = deckId
        _isLoadingStudySession.value = true
        _isSubmittingReview.value = false
        _isCheckingStudyContinuation.value = false
        _canContinueStudySession.value = false
        viewModelScope.launch {
            try {
                val words = vocabularyRepository.getDueReviewWords(
                    limit = dailyNewWordsGoal.value,
                    deckId = deckId,
                )
                launchStudyCards(words)
                _lastErrorMessage.value = null
            } catch (e: Exception) {
                clearStudyCards()
                _lastErrorMessage.value = e.message ?: "Failed to load review words"
            }
        }
    }

    fun startDailyNewSession(deckId: String? = null) {
        activeStudySessionMode = StudySessionMode.DAILY_NEW
        activeStudySessionDeckId = deckId
        _isLoadingStudySession.value = true
        _isSubmittingReview.value = false
        _isCheckingStudyContinuation.value = false
        _canContinueStudySession.value = false
        viewModelScope.launch {
            try {
                val words = vocabularyRepository.getNewWords(
                    dailyGoal = dailyNewWordsGoal.value,
                    deckId = deckId,
                )
                launchStudyCards(words)
                _lastErrorMessage.value = null
            } catch (e: Exception) {
                clearStudyCards()
                _lastErrorMessage.value = e.message ?: "Failed to load new words"
            }
        }
    }

    fun replayLastStudySession() {
        if (replayableStudyCards.isEmpty()) return
        _activeFlashcards.value = replayableStudyCards
        _currentCardIndex.value = 0
        _isCardFlipped.value = false
        _isStudyReplayMode.value = true
        _canReplayStudySession.value = true
        _isSubmittingReview.value = false
        _isCheckingStudyContinuation.value = false
        _lastErrorMessage.value = null
    }

    fun continueCurrentStudySession() {
        when (activeStudySessionMode) {
            StudySessionMode.MIXED -> startStudySession(activeStudySessionDeckId)
            StudySessionMode.DAILY_REVIEW -> startDailyQuizSession(activeStudySessionDeckId)
            StudySessionMode.DAILY_NEW -> startDailyNewSession(activeStudySessionDeckId)
            null -> Unit
        }
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun goToNextReplayCard() {
        if (_currentCardIndex.value < _activeFlashcards.value.size - 1) {
            _isCardFlipped.value = false
            _currentCardIndex.value += 1
        } else {
            _activeFlashcards.value = emptyList()
            _isCardFlipped.value = false
            _isStudyReplayMode.value = false
            refreshStudyContinuationAvailability()
        }
    }

    fun submitReviewRating(vocabId: String, rating: String) {
        if (_isSubmittingReview.value) return
        viewModelScope.launch {
            _isSubmittingReview.value = true
            try {
                vocabularyRepository.processVocabReview(vocabId, rating)
                if (_currentCardIndex.value < _activeFlashcards.value.size - 1) {
                    _isCardFlipped.value = false
                    _currentCardIndex.value += 1
                } else {
                    _activeFlashcards.value = emptyList()
                    refreshStudyContinuationAvailability()
                }
                _lastErrorMessage.value = null
            } catch (e: Exception) {
                _lastErrorMessage.value = e.message ?: "Failed to submit review"
            } finally {
                _isSubmittingReview.value = false
            }
        }
    }

    private fun launchStudyCards(words: List<VocabularyWithReviewCard>) {
        _activeFlashcards.value = words
        _currentCardIndex.value = 0
        _isCardFlipped.value = false
        _isStudyReplayMode.value = false
        _isLoadingStudySession.value = false
        _isSubmittingReview.value = false
        _isCheckingStudyContinuation.value = false
        replayableStudyCards = words
        _canReplayStudySession.value = words.isNotEmpty()
    }

    private fun clearStudyCards() {
        _activeFlashcards.value = emptyList()
        _currentCardIndex.value = 0
        _isCardFlipped.value = false
        _isStudyReplayMode.value = false
        _isLoadingStudySession.value = false
        _isSubmittingReview.value = false
        _isCheckingStudyContinuation.value = false
        replayableStudyCards = emptyList()
        _canReplayStudySession.value = false
        _canContinueStudySession.value = false
    }

    private fun refreshStudyContinuationAvailability() {
        val mode = activeStudySessionMode ?: run {
            _isCheckingStudyContinuation.value = false
            _canContinueStudySession.value = false
            return
        }
        val deckId = activeStudySessionDeckId
        _isCheckingStudyContinuation.value = true

        viewModelScope.launch {
            _canContinueStudySession.value = try {
                when (mode) {
                    StudySessionMode.MIXED -> vocabularyRepository.hasDueOrNewWords(
                        dailyGoal = dailyNewWordsGoal.value,
                        deckId = deckId,
                    )
                    StudySessionMode.DAILY_REVIEW -> vocabularyRepository.hasDueReviewWords(
                        deckId = deckId,
                    )
                    StudySessionMode.DAILY_NEW -> vocabularyRepository.hasNewWords(
                        dailyGoal = dailyNewWordsGoal.value,
                        deckId = deckId,
                    )
                }
            } catch (_: Exception) {
                false
            } finally {
                _isCheckingStudyContinuation.value = false
            }
        }
    }
}
