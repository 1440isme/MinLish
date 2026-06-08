package com.minlish.feature.practice.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.core.audio.SpeechResult
import com.minlish.core.audio.TextToSpeechManager
import com.minlish.core.data.model.PracticeSessionEntity
import com.minlish.core.data.model.VocabularyEntity
import com.minlish.core.data.repository.AnalyticsRepository
import com.minlish.core.data.repository.VocabularyRepository
import com.minlish.core.network.ApiErrorParser
import com.minlish.core.network.dto.CreateSessionResponse
import com.minlish.core.network.dto.PracticeAnswerDto
import com.minlish.core.network.dto.PracticeQuestionDto
import com.minlish.core.network.dto.PracticeSessionDto
import com.minlish.core.network.dto.PracticeSessionSummaryDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PracticeQuizViewModel(
    private val vocabularyRepository: VocabularyRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val textToSpeechManager: TextToSpeechManager,
) : ViewModel() {
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

    private val _finishAnswers = MutableStateFlow<List<PracticeAnswerDto>>(emptyList())
    val finishAnswers: StateFlow<List<PracticeAnswerDto>> = _finishAnswers

    private val _vocabulariesInSelectedDeck = MutableStateFlow<List<VocabularyEntity>>(emptyList())
    val vocabulariesInSelectedDeck: StateFlow<List<VocabularyEntity>> = _vocabulariesInSelectedDeck

    val isPracticeSoundEnabled = MutableStateFlow(true)

    private val _practiceError = MutableStateFlow<String?>(null)
    val practiceError: StateFlow<String?> = _practiceError

    fun loadDeckVocabularies(deckId: String) {
        viewModelScope.launch {
            try {
                _vocabulariesInSelectedDeck.value = vocabularyRepository.getVocabulariesInDeck(deckId)
            } catch (_: Exception) {
                _vocabulariesInSelectedDeck.value = emptyList()
            }
        }
    }

    fun fetchPracticeHistory() {
        viewModelScope.launch {
            try {
                val remoteHistory = analyticsRepository.getRemoteHistory()
                _practiceSessions.value = remoteHistory
                android.util.Log.d("MINLISH_STATS", "Successfully loaded ${remoteHistory.size} remote sessions!")
            } catch (e: Exception) {
                android.util.Log.e("MINLISH_STATS", "CRITICAL ERROR FETCHING HISTORY: ", e)
            }
        }
    }

    fun checkForActiveSession(deckId: String, onResult: (CreateSessionResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                _practiceError.value = null
                val response = vocabularyRepository.getActivePracticeSession(deckId)
                onResult(response)
            } catch (e: Exception) {
                e.printStackTrace()
                _practiceError.value = practiceStartErrorMessage(e)
                onResult(null)
            }
        }
    }

    fun startNewPracticeSession(
        deckId: String,
        practiceTypes: List<String>,
        totalQuestions: Int,
        scope: String = "LEARNED_ONLY",
    ) {
        viewModelScope.launch {
            try {
                _practiceError.value = null
                _quizQuestions.value = emptyList()
                val response = vocabularyRepository.createPracticeSession(
                    deckId,
                    practiceTypes,
                    totalQuestions,
                    scope,
                )
                _activeSession.value = response.session
                _quizQuestions.value = response.questions
                _currentQuizIndex.value = 0
                _quizCorrectCount.value = 0
                _quizFinished.value = false
                _lastSubmitResult.value = null
                _finishSummary.value = null
                _finishAnswers.value = emptyList()
                loadDeckVocabularies(deckId)
            } catch (e: Exception) {
                e.printStackTrace()
                _practiceError.value = practiceStartErrorMessage(e)
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
        _finishAnswers.value = emptyList()
        response.session.deckId?.let { loadDeckVocabularies(it) }
    }

    fun loadPastSessionResults(sessionId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _practiceError.value = null
                _quizQuestions.value = emptyList()
                val response = vocabularyRepository.getPastPracticeSessionResults(sessionId)
                _activeSession.value = response.session
                _finishAnswers.value = response.answers
                _finishSummary.value = response.summary
                _quizFinished.value = true
                response.session.deckId?.let { loadDeckVocabularies(it) }
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
                val apiError = ApiErrorParser.parse(e)
                _practiceError.value = apiError?.message ?: e.localizedMessage ?: "Failed to load past session results"
            }
        }
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
                _finishAnswers.value = emptyList()
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
                val apiError = ApiErrorParser.parse(e)
                _practiceError.value = apiError?.message ?: e.localizedMessage ?: "Failed to cancel session"
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
                e.printStackTrace()
                val apiError = ApiErrorParser.parse(e)
                _practiceError.value = apiError?.message ?: e.localizedMessage ?: "Failed to submit answer"
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
                _finishAnswers.value = response.answers
                _quizFinished.value = true
                fetchPracticeHistory()
            } catch (e: Exception) {
                e.printStackTrace()
                val apiError = ApiErrorParser.parse(e)
                _practiceError.value = apiError?.message ?: e.localizedMessage ?: "Failed to finish session"
            }
        }
    }

    fun clearPracticeError() {
        _practiceError.value = null
    }

    fun speak(text: String) {
        _practiceError.value = when (val result = textToSpeechManager.speak(text)) {
            SpeechResult.Success -> null
            SpeechResult.Loading -> "Text to speech is still loading. Please try again."
            is SpeechResult.Error -> result.message
        }
    }

    private fun practiceStartErrorMessage(error: Exception): String {
        val apiError = ApiErrorParser.parse(error)
        val errorMsg = apiError?.message ?: apiError?.code
        return when (errorMsg) {
            "PRACTICE_NOT_ENOUGH_VOCABULARY", "deckId must be a UUID" ->
                "This deck does not have enough vocabulary to start this practice mode. (Minimum 4 words needed)."
            "PRACTICE_NOT_ENOUGH_LEARNED_VOCABULARY" ->
                "This deck does not have enough learned vocabulary. Please study at least 4 words first."
            "PRACTICE_DECK_NOT_FOUND" ->
                "The selected deck was not found or has been deleted."
            else -> apiError?.message ?: error.localizedMessage ?: "Failed to start practice session"
        }
    }
}
