package com.minlish.core.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.minlish.core.data.mapper.buildCreateVocabularyRequest
import com.minlish.core.data.mapper.toVocabularyWithReviewCard
import com.minlish.core.data.mapper.toEntity
import com.minlish.core.data.model.AddVocabularyResult
import com.minlish.core.data.model.DashboardAnalyticsDto
import com.minlish.core.data.model.DeckEntity
import com.minlish.core.data.model.FavoriteResult
import com.minlish.core.data.model.ImportCsvResponse
import com.minlish.core.data.model.PendingVocabularyRequest
import com.minlish.core.data.model.PracticeSessionEntity
import com.minlish.core.data.model.RecentStudyDeckEntity
import com.minlish.core.data.model.VocabularyEntity
import com.minlish.core.data.model.VocabularyWithReviewCard
import com.minlish.core.network.ApiErrorParser
import com.minlish.core.network.DecksApiService
import com.minlish.core.network.FavoritesApiService
import com.minlish.core.network.ImportsApiService
import com.minlish.core.network.PracticeApiService
import com.minlish.core.network.LearningApiService
import com.minlish.core.network.VocabulariesApiService
import com.minlish.core.network.dto.CreateDeckRequest
import com.minlish.core.network.dto.CreatePracticeSessionRequest
import com.minlish.core.network.dto.CreateSessionResponse
import com.minlish.core.network.dto.FinishSessionResponse
import com.minlish.core.network.dto.PracticeAnswerDto
import com.minlish.core.network.dto.PracticeQuestionDto
import com.minlish.core.network.dto.PracticeSessionDto
import com.minlish.core.network.dto.SubmitAnswerRequest
import com.minlish.core.network.dto.UpdateDeckRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class VocabularyRepository(
    private val context: Context,
    private val decksApi: DecksApiService,
    private val vocabulariesApi: VocabulariesApiService,
    private val favoritesApi: FavoritesApiService,
    private val importsApi: ImportsApiService,


    private val practiceApi: PracticeApiService,
    private val learningApi: LearningApiService,
) {
    private var favoritesDeckId: String? = null
    private val favoritedSourceIds = mutableSetOf<String>()
    private val dueCountState = MutableStateFlow(0)

    fun getDecksByGoalFlow(goal: String): Flow<List<DeckEntity>> = flowOf(emptyList())

    fun getDueCountFlow(): Flow<Int> = dueCountState

    fun getLocalDashboardAnalytics(dailyGoal: Int): DashboardAnalyticsDto {
        return DashboardAnalyticsDto(0, 0, 0, dailyGoal, 0f, 0, 0)
    }

    fun getPracticeSessionsFlow(): Flow<List<PracticeSessionEntity>> = flowOf(emptyList())

    suspend fun seedDatabaseAsNecessary() {}

    suspend fun listDecks(search: String? = null): List<DeckEntity> {
        val response = decksApi.listDecks(
            type = "ALL",
            search = search?.takeIf { it.isNotBlank() },
            page = 1,
            pageSize = 50,
        )
        return response.items.map { it.toEntity() }
    }

    suspend fun getDeckById(deckId: String): DeckEntity? {
        return try {
            decksApi.getDeck(deckId).toEntity()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getFavoritesDeck(): DeckEntity {
        val deck = decksApi.getFavoritesDeck().toEntity()
        favoritesDeckId = deck.id
        return deck
    }

    fun getFavoritesDeckId(): String? = favoritesDeckId

    fun isFavorited(sourceVocabularyId: String): Boolean =
        favoritedSourceIds.contains(sourceVocabularyId)

    fun getFavoritedSourceIds(): Set<String> = favoritedSourceIds.toSet()

    suspend fun refreshFavoritedSourceIds() {
        val favoritesDeck = try {
            getFavoritesDeck()
        } catch (_: Exception) {
            return
        }
        val ids = mutableSetOf<String>()
        var page = 1
        var total = Int.MAX_VALUE
        while ((page - 1) * 50 < total) {
            val response = vocabulariesApi.listByDeck(
                deckId = favoritesDeck.id,
                page = page,
                pageSize = 50,
            )
            response.items.forEach { vocab ->
                vocab.sourceVocabularyId?.let { ids.add(it) }
            }
            total = response.meta.total
            page++
            if (response.items.isEmpty()) break
        }
        favoritedSourceIds.clear()
        favoritedSourceIds.addAll(ids)
    }

    suspend fun getVocabulariesInDeck(deckId: String): List<VocabularyEntity> {
        val all = mutableListOf<VocabularyEntity>()
        var page = 1
        var total = Int.MAX_VALUE
        while ((page - 1) * 50 < total) {
            val response = vocabulariesApi.listByDeck(deckId = deckId, page = page, pageSize = 50)
            all.addAll(response.items.map { it.toEntity() })
            total = response.meta.total
            page++
            if (response.items.isEmpty()) break
        }
        return all
    }

    fun getVocabulariesInDeckFlow(deckId: String): Flow<List<VocabularyEntity>> = flowOf(emptyList())

    suspend fun createDeck(name: String, description: String, tags: List<String>) {
        decksApi.createDeck(
            CreateDeckRequest(
                name = name,
                description = description.takeIf { it.isNotBlank() },
                tags = tags.takeIf { it.isNotEmpty() },
            )
        )
    }

    suspend fun deleteDeck(deckId: String) {
        decksApi.deleteDeck(deckId)
    }

    suspend fun updateDeck(deckId: String, name: String, description: String, tags: List<String>) {
        decksApi.updateDeck(
            deckId,
            UpdateDeckRequest(
                name = name.trim(),
                description = description.takeIf { it.isNotBlank() },
                tags = tags.takeIf { it.isNotEmpty() },
            )
        )
    }

    suspend fun addVocabularyToDeck(
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
        allowSameWordDifferentMeaning: Boolean = false,
    ): AddVocabularyResult {
        val request = buildCreateVocabularyRequest(
            word, pronunciation, partOfSpeech, meaning, descEn, example, collocation, related, note,
            allowSameWordDifferentMeaning,
        )
        return try {
            val created = vocabulariesApi.createInDeck(deckId, request)
            AddVocabularyResult.Success(created.toEntity())
        } catch (e: Exception) {
            mapAddVocabularyError(
                e, deckId, word, pronunciation, partOfSpeech, meaning, descEn, example, collocation, related, note,
            )
        }
    }

    private fun mapAddVocabularyError(
        e: Exception,
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
    ): AddVocabularyResult {
        val apiError = ApiErrorParser.parse(e)
        val code = apiError?.code
        val message = apiError?.message ?: ApiErrorParser.humanMessage(e)
        return when (code) {
            "DUPLICATE_VOCABULARY" -> AddVocabularyResult.DuplicateExact(message)
            "WORD_EXISTS_WITH_DIFFERENT_MEANING" -> AddVocabularyResult.SameWordDifferentMeaning(
                message = message,
                existingItems = apiError.existingItems.orEmpty(),
                pendingRequest = PendingVocabularyRequest(
                    deckId, word, pronunciation, partOfSpeech, meaning, descEn, example, collocation, related, note,
                ),
            )
            else -> AddVocabularyResult.Failure(message)
        }
    }

    suspend fun confirmAddVocabularyWithDifferentMeaning(
        pending: PendingVocabularyRequest,
    ): AddVocabularyResult = addVocabularyToDeck(
        deckId = pending.deckId,
        word = pending.word,
        pronunciation = pending.pronunciation,
        partOfSpeech = pending.partOfSpeech,
        meaning = pending.meaning,
        descEn = pending.descEn,
        example = pending.example,
        collocation = pending.collocation,
        related = pending.related,
        note = pending.note,
        allowSameWordDifferentMeaning = true,
    )

    suspend fun deleteVocabulary(vocabId: String) {
        vocabulariesApi.delete(vocabId)
    }

    suspend fun updateVocabulary(
        id: String,
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
        fun opt(value: String): String? = value.trim().takeIf { it.isNotEmpty() }
        vocabulariesApi.update(
            id,
            com.minlish.core.network.dto.UpdateVocabularyRequest(
                word = word.trim(),
                pronunciation = opt(pronunciation),
                partOfSpeech = opt(partOfSpeech),
                meaning = meaning.trim(),
                descriptionEn = opt(descEn),
                example = opt(example),
                collocation = opt(collocation),
                relatedWords = opt(related),
                note = opt(note),
            ),
        )
    }

    suspend fun favoriteVocabulary(originalVocabularyId: String): FavoriteResult {
        return try {
            favoritesApi.favorite(originalVocabularyId)
            favoritedSourceIds.add(originalVocabularyId)
            FavoriteResult.Success
        } catch (e: Exception) {
            FavoriteResult.Failure(ApiErrorParser.humanMessage(e))
        }
    }

    suspend fun unfavoriteVocabulary(originalVocabularyId: String): FavoriteResult {
        return try {
            favoritesApi.unfavorite(originalVocabularyId)
            favoritedSourceIds.remove(originalVocabularyId)
            FavoriteResult.Success
        } catch (e: Exception) {
            FavoriteResult.Failure(ApiErrorParser.humanMessage(e))
        }
    }

    fun favoriteSourceIdFor(vocab: VocabularyEntity): String =
        vocab.sourceVocabularyId ?: vocab.id

    suspend fun importCsvFile(deckId: String, fileUri: Uri): ImportCsvResponse {
        val fileName = resolveDisplayName(fileUri)
        if (!fileName.lowercase().endsWith(".csv")) {
            return ImportCsvResponse(
                success = false,
                importedCount = 0,
                errors = listOf("Please choose a .csv file."),
            )
        }

        val tempFile = File(context.cacheDir, "import_${System.currentTimeMillis()}_$fileName")
        return try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: return ImportCsvResponse(
                    success = false,
                    importedCount = 0,
                    errors = listOf("Could not read the selected CSV file."),
                )
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            val requestBody = tempFile.asRequestBody("text/csv".toMediaType())
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
            val response = importsApi.importCsv(deckId, part)
            response.toEntity()
        } catch (e: Exception) {
            ImportCsvResponse(
                success = false,
                importedCount = 0,
                errors = listOf(ApiErrorParser.humanMessage(e)),
            )
        } finally {
            tempFile.delete()
        }
    }

    private fun resolveDisplayName(fileUri: Uri): String {
        context.contentResolver.query(
            fileUri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                val displayName = cursor.getString(nameIndex)
                if (!displayName.isNullOrBlank()) {
                    return displayName
                }
            }
        }
        return "import.csv"
    }

    suspend fun getDueReviewAndNewWords(
        dailyGoal: Int,
        deckId: String? = null,
    ): List<VocabularyWithReviewCard> {
        val response = if (deckId.isNullOrBlank()) {
            learningApi.getDailyPlan()
        } else {
            learningApi.startDeck(deckId, dailyGoal)
        }

        dueCountState.value = response.dueReviewCount

        val dueCards = response.dueCards.mapNotNull { it.toVocabularyWithReviewCard() }
        val dueIds = dueCards.map { it.vocabulary.id }.toSet()
        val newCards = response.newWords
            .filterNot { it.id in dueIds }
            .map { vocabulary ->
                VocabularyWithReviewCard(
                    vocabulary = vocabulary.toEntity(),
                    reviewCard = null,
                )
            }

        return dueCards + newCards
    }

    suspend fun getDueReviewWords(
        limit: Int,
        deckId: String? = null,
    ): List<VocabularyWithReviewCard> {
        val response = learningApi.getDueCards(
            deckId = deckId,
            limit = limit,
        )
        dueCountState.value = response.count
        return response.items.mapNotNull { it.toVocabularyWithReviewCard() }
    }

    suspend fun getRecentStudyDeck(): RecentStudyDeckEntity? {
        return learningApi.getRecentDeck().toEntity()
    }

    suspend fun getNewWords(
        dailyGoal: Int,
        deckId: String? = null,
    ): List<VocabularyWithReviewCard> {
        val response = if (deckId.isNullOrBlank()) {
            learningApi.getDailyPlan()
        } else {
            learningApi.startDeck(deckId, dailyGoal)
        }

        dueCountState.value = response.dueReviewCount

        return response.newWords.map { vocabulary ->
            VocabularyWithReviewCard(
                vocabulary = vocabulary.toEntity(),
                reviewCard = null,
            )
        }
    }

    suspend fun processVocabReview(vocabId: String, rating: String) {
        learningApi.submitReview(
            body = com.minlish.core.network.dto.SubmitReviewRequest(
                vocabularyId = vocabId,
                rating = rating,
                reviewedAt = java.time.Instant.now().toString(),
            ),
        )
        dueCountState.value = (dueCountState.value - 1).coerceAtLeast(0)
    }

    suspend fun savePracticeSession(deckId: String, type: String, total: Int, correct: Int) {}

    // Practice Remote API Integrations
    suspend fun createPracticeSession(
        deckId: String,
        practiceTypes: List<String>?,
        totalQuestions: Int?
    ): CreateSessionResponse {
        return practiceApi.createSession(
            CreatePracticeSessionRequest(deckId, practiceTypes, totalQuestions)
        )
    }

    suspend fun getActivePracticeSession(deckId: String): CreateSessionResponse? {
        return try {
            practiceApi.getActiveSession(deckId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPracticeQuestions(sessionId: String): List<PracticeQuestionDto> {
        return practiceApi.getQuestions(sessionId)
    }

    suspend fun submitPracticeAnswer(
        sessionId: String,
        questionIndex: Int,
        userAnswer: String?
    ): PracticeAnswerDto {
        return practiceApi.submitAnswer(sessionId, SubmitAnswerRequest(questionIndex, userAnswer))
    }

    suspend fun finishPracticeSession(sessionId: String): FinishSessionResponse {
        return practiceApi.finishSession(sessionId)
    }

    suspend fun cancelPracticeSession(sessionId: String): PracticeSessionDto {
        return practiceApi.cancelSession(sessionId)
    }
}
