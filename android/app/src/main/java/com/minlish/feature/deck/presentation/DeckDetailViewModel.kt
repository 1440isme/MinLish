package com.minlish.feature.deck.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.core.audio.SpeechResult
import com.minlish.core.audio.TextToSpeechManager
import com.minlish.core.data.model.AddVocabularyResult
import com.minlish.core.data.model.DeckEntity
import com.minlish.core.data.model.DeckLearningProgressEntity
import com.minlish.core.data.model.ExportCsvResult
import com.minlish.core.data.model.FavoriteResult
import com.minlish.core.data.model.ImportCsvResponse
import com.minlish.core.data.model.PendingVocabularyRequest
import com.minlish.core.data.model.VocabularyEntity
import com.minlish.core.data.repository.VocabularyRepository
import com.minlish.core.network.ApiErrorParser
import com.minlish.core.network.dto.ExistingVocabularyItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeckDetailViewModel(
    private val vocabularyRepository: VocabularyRepository,
    private val textToSpeechManager: TextToSpeechManager,
) : ViewModel() {
    private val _selectedDeck = MutableStateFlow<DeckEntity?>(null)
    val selectedDeck: StateFlow<DeckEntity?> = _selectedDeck

    private val _vocabulariesInSelectedDeck = MutableStateFlow<List<VocabularyEntity>>(emptyList())
    val vocabulariesInSelectedDeck: StateFlow<List<VocabularyEntity>> = _vocabulariesInSelectedDeck

    private val _selectedDeckLearningProgress = MutableStateFlow<DeckLearningProgressEntity?>(null)
    val selectedDeckLearningProgress: StateFlow<DeckLearningProgressEntity?> = _selectedDeckLearningProgress

    private val _isLoadingDeckDetail = MutableStateFlow(false)
    val isLoadingDeckDetail: StateFlow<Boolean> = _isLoadingDeckDetail

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage

    private val _favoritedSourceIds = MutableStateFlow<Set<String>>(emptySet())
    val favoritedSourceIds: StateFlow<Set<String>> = _favoritedSourceIds

    fun clearLastError() {
        _lastErrorMessage.value = null
    }

    fun speak(text: String) {
        _lastErrorMessage.value = when (val result = textToSpeechManager.speak(text)) {
            SpeechResult.Success -> null
            SpeechResult.Loading -> "Text to speech is still loading. Please try again."
            is SpeechResult.Error -> result.message
        }
    }

    fun selectDeck(deckId: String) {
        viewModelScope.launch {
            _isLoadingDeckDetail.value = true
            try {
                val deck = vocabularyRepository.getDeckById(deckId)
                _selectedDeck.value = deck
                if (deck != null) {
                    _vocabulariesInSelectedDeck.value = vocabularyRepository.getVocabulariesInDeck(deckId)
                    _selectedDeckLearningProgress.value = vocabularyRepository.getDeckLearningProgress(deckId)
                } else {
                    _vocabulariesInSelectedDeck.value = emptyList()
                    _selectedDeckLearningProgress.value = null
                }
                vocabularyRepository.refreshFavoritedSourceIds()
                _favoritedSourceIds.value = vocabularyRepository.getFavoritedSourceIds()
                _lastErrorMessage.value = null
            } catch (e: Exception) {
                _selectedDeckLearningProgress.value = null
                _lastErrorMessage.value = ApiErrorParser.humanMessage(e, "Failed to load deck")
            } finally {
                _isLoadingDeckDetail.value = false
            }
        }
    }

    fun refreshFavoritedIds() {
        viewModelScope.launch {
            try {
                vocabularyRepository.refreshFavoritedSourceIds()
                _favoritedSourceIds.value = vocabularyRepository.getFavoritedSourceIds()
            } catch (_: Exception) {
                // Favorites deck may not exist until register side-effect is done.
            }
        }
    }

    fun isVocabFavorited(vocab: VocabularyEntity): Boolean {
        val sourceId = vocabularyRepository.favoriteSourceIdFor(vocab)
        return favoritedSourceIds.value.contains(sourceId) ||
            vocabularyRepository.isFavorited(sourceId)
    }

    fun deleteCustomDeck(deckId: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                vocabularyRepository.deleteDeck(deckId)
                onDeleted()
            } catch (e: Exception) {
                _lastErrorMessage.value = ApiErrorParser.humanMessage(e, "Failed to delete deck")
            }
        }
    }

    fun updateCustomDeck(
        deckId: String,
        name: String,
        description: String,
        tags: List<String>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        viewModelScope.launch {
            try {
                vocabularyRepository.updateDeck(deckId, name, description, tags)
                _lastErrorMessage.value = null
                selectDeck(deckId)
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = deckActionMessage(e, "Failed to update deck")
                _lastErrorMessage.value = null
                onError(errorMessage)
            }
        }
    }

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
                deckId,
                word,
                pronunciation,
                partOfSpeech,
                meaning,
                descEn,
                example,
                collocation,
                related,
                note,
            )
            when (result) {
                is AddVocabularyResult.Success -> {
                    selectDeck(deckId)
                }
                else -> Unit
            }
            onResult(result)
        }
    }

    fun confirmAddDifferentMeaning(
        pending: PendingVocabularyRequest,
        onResult: (AddVocabularyResult) -> Unit,
    ) {
        viewModelScope.launch {
            val result = vocabularyRepository.confirmAddVocabularyWithDifferentMeaning(pending)
            when (result) {
                is AddVocabularyResult.Success -> {
                    selectDeck(pending.deckId)
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
        note: String,
    ) {
        viewModelScope.launch {
            try {
                vocabularyRepository.updateVocabulary(
                    id,
                    word,
                    pronunciation,
                    partOfSpeech,
                    meaning,
                    descEn,
                    example,
                    collocation,
                    related,
                    note,
                )
                selectDeck(deckId)
            } catch (e: Exception) {
                _lastErrorMessage.value = ApiErrorParser.humanMessage(e, "Failed to update vocabulary")
            }
        }
    }

    fun importCsv(deckId: String, fileUri: Uri, onComplete: (ImportCsvResponse) -> Unit) {
        viewModelScope.launch {
            try {
                val response = vocabularyRepository.importCsvFile(deckId, fileUri)
                selectDeck(deckId)
                onComplete(response)
            } catch (e: Exception) {
                onComplete(
                    ImportCsvResponse(
                        success = false,
                        importedCount = 0,
                        errors = listOf(e.message ?: "Import failed"),
                    ),
                )
            }
        }
    }

    fun exportCsv(deckId: String, fileUri: Uri, onComplete: (ExportCsvResult) -> Unit) {
        viewModelScope.launch {
            onComplete(vocabularyRepository.exportCsvFile(deckId, fileUri))
        }
    }
}

private fun deckActionMessage(error: Exception, fallback: String): String {
    val apiError = ApiErrorParser.parse(error)
    return when (apiError?.code) {
        "DECK_NAME_DUPLICATE" -> "A deck with this name already exists."
        else -> apiError?.message ?: error.localizedMessage ?: fallback
    }
}

data class SameWordWarningState(
    val existingItems: List<ExistingVocabularyItemDto>,
    val pending: PendingVocabularyRequest,
)
