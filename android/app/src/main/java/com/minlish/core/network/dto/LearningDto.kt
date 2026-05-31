package com.minlish.core.network.dto

import com.google.gson.annotations.SerializedName

data class ReviewCardDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("vocabularyId") val vocabularyId: String,
    @SerializedName("status") val status: String,
    @SerializedName("repetition") val repetition: Int,
    @SerializedName("intervalDays") val intervalDays: Int,
    @SerializedName("easeFactor") val easeFactor: Float,
    @SerializedName("dueAt") val dueAt: String,
    @SerializedName("lastReviewedAt") val lastReviewedAt: String? = null,
    @SerializedName("firstLearnedAt") val firstLearnedAt: String? = null,
    @SerializedName("lapses") val lapses: Int = 0,
    @SerializedName("totalReviews") val totalReviews: Int = 0,
    @SerializedName("correctReviews") val correctReviews: Int = 0,
    @SerializedName("vocabulary") val vocabulary: VocabularyPreviewDto? = null,
)

data class VocabularyPreviewDto(
    @SerializedName("id") val id: String,
    @SerializedName("deckId") val deckId: String,
    @SerializedName("word") val word: String,
    @SerializedName("meaning") val meaning: String,
    @SerializedName("pronunciation") val pronunciation: String? = null,
    @SerializedName("partOfSpeech") val partOfSpeech: String? = null,
    @SerializedName("descriptionEn") val descriptionEn: String? = null,
    @SerializedName("example") val example: String? = null,
)

data class DailyPlanResponseDto(
    @SerializedName("deckId") val deckId: String? = null,
    @SerializedName("newWordsGoal") val newWordsGoal: Int,
    @SerializedName("newWordsAvailable") val newWordsAvailable: Int,
    @SerializedName("dueReviewCount") val dueReviewCount: Int,
    @SerializedName("dueCards") val dueCards: List<ReviewCardDto>,
    @SerializedName("newWords") val newWords: List<VocabularyPreviewDto>,
)

data class RecentLearningDeckResponseDto(
    @SerializedName("hasRecentDeck") val hasRecentDeck: Boolean,
    @SerializedName("deck") val deck: DeckDto? = null,
    @SerializedName("lastStudiedAt") val lastStudiedAt: String? = null,
    @SerializedName("dueReviewCount") val dueReviewCount: Int = 0,
    @SerializedName("newWordsAvailable") val newWordsAvailable: Int = 0,
)

data class DueCardsResponseDto(
    @SerializedName("items") val items: List<ReviewCardDto>,
    @SerializedName("count") val count: Int,
    @SerializedName("limit") val limit: Int,
)

data class SubmitReviewRequest(
    @SerializedName("vocabularyId") val vocabularyId: String,
    @SerializedName("rating") val rating: String,
    @SerializedName("reviewedAt") val reviewedAt: String,
)

data class ReviewSummaryDto(
    @SerializedName("rating") val rating: String,
    @SerializedName("quality") val quality: Int,
    @SerializedName("isCorrect") val isCorrect: Boolean,
    @SerializedName("reviewedAt") val reviewedAt: String,
)

data class SubmitReviewResponseDto(
    @SerializedName("reviewCard") val reviewCard: ReviewCardDto,
    @SerializedName("summary") val summary: ReviewSummaryDto,
)
