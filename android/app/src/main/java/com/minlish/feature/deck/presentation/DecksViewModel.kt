package com.minlish.feature.deck.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.core.data.model.DeckEntity
import com.minlish.core.data.repository.VocabularyRepository
import com.minlish.core.network.ApiErrorParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DecksViewModel(
    private val vocabularyRepository: VocabularyRepository,
) : ViewModel() {
    private val _decksList = MutableStateFlow<List<DeckEntity>>(emptyList())
    val decksList: StateFlow<List<DeckEntity>> = _decksList

    private val _isLoadingDecks = MutableStateFlow(false)
    val isLoadingDecks: StateFlow<Boolean> = _isLoadingDecks

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage

    private var currentDeckSearchQuery: String? = null

    fun clearLastError() {
        _lastErrorMessage.value = null
    }

    fun refreshDecks(searchQuery: String? = currentDeckSearchQuery) {
        viewModelScope.launch {
            _isLoadingDecks.value = true
            try {
                currentDeckSearchQuery = searchQuery?.trim()?.takeIf { it.isNotEmpty() }
                _decksList.value = vocabularyRepository.listDecks(currentDeckSearchQuery)
                _lastErrorMessage.value = null
            } catch (e: Exception) {
                _lastErrorMessage.value = ApiErrorParser.humanMessage(e, "Failed to load decks")
            } finally {
                _isLoadingDecks.value = false
            }
        }
    }

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
}
