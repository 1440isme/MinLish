package com.minlish.core.data.model

import com.minlish.core.network.dto.ExistingVocabularyItemDto

sealed class AddVocabularyResult {
    data class Success(val vocabulary: VocabularyEntity) : AddVocabularyResult()
    data class DuplicateExact(val message: String) : AddVocabularyResult()
    data class SameWordDifferentMeaning(
        val message: String,
        val existingItems: List<ExistingVocabularyItemDto>,
        val pendingRequest: PendingVocabularyRequest,
    ) : AddVocabularyResult()
    data class Failure(val message: String) : AddVocabularyResult()
}

data class PendingVocabularyRequest(
    val deckId: String,
    val word: String,
    val pronunciation: String,
    val partOfSpeech: String,
    val meaning: String,
    val descEn: String,
    val example: String,
    val collocation: String,
    val related: String,
    val note: String,
)

sealed class FavoriteResult {
    data object Success : FavoriteResult()
    data class Failure(val message: String) : FavoriteResult()
}
