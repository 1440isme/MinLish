package com.minlish.core.data.model

data class DeckEntity(
    val id: String,
    val name: String,
    val description: String,
    val tags: String, // Semicolon separated
    val deckType: String, // SYSTEM, USER
    val targetLevel: String,
    val learningGoal: String,
    val isDefault: Boolean = false,
    val totalWords: Int = 0,
) {
    val isFavoritesDeck: Boolean get() = isDefault && name.equals("Favorites", ignoreCase = true)
}

data class VocabularyEntity(
    val id: String,
    val deckId: String,
    val sourceVocabularyId: String? = null,
    val word: String,
    val pronunciation: String,
    val partOfSpeech: String,
    val meaning: String,
    val descriptionEn: String,
    val example: String,
    val collocation: String,
    val relatedWords: String,
    val note: String
)

data class ReviewCardEntity(
    val id: String,
    val vocabularyId: String,
    val repetition: Int,
    val intervalDays: Int,
    val easeFactor: Float,
    val dueAt: Long
)

data class VocabularyWithReviewCard(
    val vocabulary: VocabularyEntity,
    val reviewCard: ReviewCardEntity?
)

data class DeckLearningProgressEntity(
    val newWordsAvailable: Int,
    val dueReviewCount: Int,
)

data class PracticeSessionEntity(
    val id: String,
    val deckId: String,
    val deckName: String? = null,
    val practiceType: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val finishedAt: Long,

    val status: String,
)

data class DashboardAnalyticsDto(
    val totalLearned: Int,
    val totalReview: Int,
    val dueToday: Int,
    val dailyGoal: Int,
    val accuracy: Float,
    val streak: Int,
    val progressPercent: Int,
    val weeklyActiveDays: List<Boolean> = List(7) { false }, // mảng 7 ngày lấy từ server
    val totalPractices: Int = 0,   //tổng số lượng baài practice đã làm
    val weeklyPracticeCounts: List<Int> = List(7) { 0 },
    val weeklyAccuracyHistory: List<Float> = emptyList()
)

data class RecentStudyDeckEntity(
    val deck: DeckEntity,
    val dueReviewCount: Int,
    val newWordsAvailable: Int,
    val lastStudiedAt: Long,
)

data class QuizQuestion(
    val vocabulary: VocabularyEntity,
    val questionType: String,
    val questionText: String,
    val choices: List<String>,
    val correctAnswer: String
)

data class ImportCsvResponse(
    val success: Boolean,
    val importedCount: Int,
    val duplicateCount: Int = 0,
    val failedCount: Int = 0,
    val totalRows: Int = 0,
    val status: String = "",
    val errors: List<String>? = null,
    val duplicateSamples: List<String>? = null,
)
