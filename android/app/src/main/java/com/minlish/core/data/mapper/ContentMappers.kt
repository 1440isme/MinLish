package com.minlish.core.data.mapper

import com.minlish.core.data.model.DeckEntity
import com.minlish.core.data.model.ImportCsvResponse
import com.minlish.core.data.model.RecentStudyDeckEntity
import com.minlish.core.data.model.ReviewCardEntity
import com.minlish.core.data.model.VocabularyEntity
import com.minlish.core.data.model.VocabularyWithReviewCard
import com.minlish.core.network.dto.CreateVocabularyRequest
import com.minlish.core.network.dto.DeckDto
import com.minlish.core.network.dto.ImportCsvResponseDto
import com.minlish.core.network.dto.RecentLearningDeckResponseDto
import com.minlish.core.network.dto.ReviewCardDto
import com.minlish.core.network.dto.VocabularyPreviewDto
import com.minlish.core.network.dto.VocabularyDto
import java.time.Instant

fun DeckDto.toEntity(): DeckEntity {
    val tagString = tags?.joinToString(";") ?: ""
    val levelName = learningLevel?.name ?: ""
    val goalCode = learningLevel?.learningPath?.code
        ?: learningLevel?.learningPath?.name
        ?: ""
    return DeckEntity(
        id = id,
        name = name,
        description = description.orEmpty(),
        tags = tagString,
        deckType = deckType,
        targetLevel = levelName,
        learningGoal = goalCode,
        isDefault = isDefault,
        totalWords = totalWords,
    )
}

fun VocabularyDto.toEntity(): VocabularyEntity = VocabularyEntity(
    id = id,
    deckId = deckId,
    sourceVocabularyId = sourceVocabularyId,
    word = word,
    pronunciation = pronunciation.orEmpty(),
    partOfSpeech = partOfSpeech.orEmpty(),
    meaning = meaning,
    descriptionEn = descriptionEn.orEmpty(),
    example = example.orEmpty(),
    collocation = collocation.orEmpty(),
    relatedWords = relatedWords.orEmpty(),
    note = note.orEmpty(),
)

fun VocabularyPreviewDto.toEntity(): VocabularyEntity = VocabularyEntity(
    id = id,
    deckId = deckId,
    sourceVocabularyId = null,
    word = word,
    pronunciation = pronunciation.orEmpty(),
    partOfSpeech = partOfSpeech.orEmpty(),
    meaning = meaning,
    descriptionEn = "",
    example = "",
    collocation = "",
    relatedWords = "",
    note = "",
)

fun ReviewCardDto.toEntity(): ReviewCardEntity = ReviewCardEntity(
    id = id,
    vocabularyId = vocabularyId,
    repetition = repetition,
    intervalDays = intervalDays,
    easeFactor = easeFactor,
    dueAt = dueAt.toEpochMillis(),
)

fun ReviewCardDto.toVocabularyWithReviewCard(): VocabularyWithReviewCard? {
    val vocabularyDto = vocabulary ?: return null
    return VocabularyWithReviewCard(
        vocabulary = vocabularyDto.toEntity(),
        reviewCard = toEntity(),
    )
}

private fun String.toEpochMillis(): Long {
    return runCatching { Instant.parse(this).toEpochMilli() }.getOrDefault(0L)
}

fun ImportCsvResponseDto.toEntity(): ImportCsvResponse = ImportCsvResponse(
    success = status == "COMPLETED" || status == "PARTIAL_SUCCESS",
    importedCount = successRows,
    duplicateCount = duplicateRows,
    failedCount = failedRows,
    totalRows = totalRows,
    status = status,
    errors = errors.map { "Row ${it.row} (${it.field}): ${it.message}" },
    duplicateSamples = duplicates.take(5).map { "Row ${it.row}: ${it.word} — ${it.meaning}" },
)

fun RecentLearningDeckResponseDto.toEntity(): RecentStudyDeckEntity? {
    val deckDto = deck ?: return null
    if (!hasRecentDeck) return null

    return RecentStudyDeckEntity(
        deck = deckDto.toEntity(),
        dueReviewCount = dueReviewCount,
        newWordsAvailable = newWordsAvailable,
        lastStudiedAt = lastStudiedAt?.toEpochMillis() ?: 0L,
    )
}

fun buildCreateVocabularyRequest(
    word: String,
    pronunciation: String,
    partOfSpeech: String,
    meaning: String,
    descEn: String,
    example: String,
    collocation: String,
    related: String,
    note: String,
    allowSameWordDifferentMeaning: Boolean = false,
): CreateVocabularyRequest {
    fun opt(value: String): String? = value.trim().takeIf { it.isNotEmpty() }
    return CreateVocabularyRequest(
        word = word.trim(),
        pronunciation = opt(pronunciation),
        partOfSpeech = opt(partOfSpeech),
        meaning = meaning.trim(),
        descriptionEn = opt(descEn),
        example = opt(example),
        collocation = opt(collocation),
        relatedWords = opt(related),
        note = opt(note),
        allowSameWordDifferentMeaning = if (allowSameWordDifferentMeaning) true else null,
    )
}
