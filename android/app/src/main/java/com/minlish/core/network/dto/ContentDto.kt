package com.minlish.core.network.dto

import com.google.gson.annotations.SerializedName

// --- Pagination ---

data class PaginationMetaDto(
    @SerializedName("page") val page: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("total") val total: Int,
)

data class PaginatedResponseDto<T>(
    @SerializedName("meta") val meta: PaginationMetaDto,
    @SerializedName("items") val items: List<T>,
)

// --- Learning path / level ---

data class LearningPathDto(
    @SerializedName("id") val id: String,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
)

data class LearningLevelDto(
    @SerializedName("id") val id: String,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("learningPathId") val learningPathId: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("learningPath") val learningPath: LearningPathDto? = null,
)

// --- Deck ---

data class DeckDto(
    @SerializedName("id") val id: String,
    @SerializedName("ownerUserId") val ownerUserId: String? = null,
    @SerializedName("learningLevelId") val learningLevelId: String? = null,
    @SerializedName("deckType") val deckType: String,
    @SerializedName("visibility") val visibility: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("displayOrder") val displayOrder: Int = 0,
    @SerializedName("totalWords") val totalWords: Int = 0,
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("learningLevel") val learningLevel: LearningLevelDto? = null,
)

data class CreateDeckRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("learningLevelId") val learningLevelId: String? = null,
)

data class UpdateDeckRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("learningLevelId") val learningLevelId: String? = null,
)

// --- Vocabulary ---

data class VocabularyDto(
    @SerializedName("id") val id: String,
    @SerializedName("deckId") val deckId: String,
    @SerializedName("sourceVocabularyId") val sourceVocabularyId: String? = null,
    @SerializedName("word") val word: String,
    @SerializedName("pronunciation") val pronunciation: String? = null,
    @SerializedName("partOfSpeech") val partOfSpeech: String? = null,
    @SerializedName("meaning") val meaning: String,
    @SerializedName("descriptionEn") val descriptionEn: String? = null,
    @SerializedName("example") val example: String? = null,
    @SerializedName("collocation") val collocation: String? = null,
    @SerializedName("relatedWords") val relatedWords: String? = null,
    @SerializedName("note") val note: String? = null,
)

data class CreateVocabularyRequest(
    @SerializedName("word") val word: String,
    @SerializedName("pronunciation") val pronunciation: String? = null,
    @SerializedName("partOfSpeech") val partOfSpeech: String? = null,
    @SerializedName("meaning") val meaning: String,
    @SerializedName("descriptionEn") val descriptionEn: String? = null,
    @SerializedName("example") val example: String? = null,
    @SerializedName("collocation") val collocation: String? = null,
    @SerializedName("relatedWords") val relatedWords: String? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("allowSameWordDifferentMeaning") val allowSameWordDifferentMeaning: Boolean? = null,
)

data class UpdateVocabularyRequest(
    @SerializedName("word") val word: String? = null,
    @SerializedName("pronunciation") val pronunciation: String? = null,
    @SerializedName("partOfSpeech") val partOfSpeech: String? = null,
    @SerializedName("meaning") val meaning: String? = null,
    @SerializedName("descriptionEn") val descriptionEn: String? = null,
    @SerializedName("example") val example: String? = null,
    @SerializedName("collocation") val collocation: String? = null,
    @SerializedName("relatedWords") val relatedWords: String? = null,
    @SerializedName("note") val note: String? = null,
)

data class ExistingVocabularyItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("word") val word: String,
    @SerializedName("meaning") val meaning: String,
)

// --- Favorites ---

data class FavoriteResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("favoriteVocabularyId") val favoriteVocabularyId: String,
)

// --- Import ---

data class ImportDuplicateDto(
    @SerializedName("row") val row: Int,
    @SerializedName("word") val word: String,
    @SerializedName("meaning") val meaning: String,
    @SerializedName("reason") val reason: String,
)

data class ImportErrorRowDto(
    @SerializedName("row") val row: Int,
    @SerializedName("field") val field: String,
    @SerializedName("message") val message: String,
)

data class ImportCsvResponseDto(
    @SerializedName("importJobId") val importJobId: String,
    @SerializedName("totalRows") val totalRows: Int,
    @SerializedName("successRows") val successRows: Int,
    @SerializedName("duplicateRows") val duplicateRows: Int,
    @SerializedName("failedRows") val failedRows: Int,
    @SerializedName("status") val status: String,
    @SerializedName("duplicates") val duplicates: List<ImportDuplicateDto>,
    @SerializedName("errors") val errors: List<ImportErrorRowDto>,
)
