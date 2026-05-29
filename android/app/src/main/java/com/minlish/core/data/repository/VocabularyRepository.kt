package com.minlish.core.data.repository

import com.minlish.core.data.model.*
import com.minlish.core.network.PracticeApiService
import com.minlish.core.network.dto.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class VocabularyRepository(private val practiceApiService: PracticeApiService) {
    fun getDecksByGoalFlow(goal: String): Flow<List<DeckEntity>> = flowOf(emptyList())
    
    fun getDueCountFlow(): Flow<Int> = flowOf(0)
    
    fun getLocalDashboardAnalytics(dailyGoal: Int): DashboardAnalyticsDto {
        return DashboardAnalyticsDto(0, 0, 0, dailyGoal, 0f, 0, 0)
    }
    
    fun getPracticeSessionsFlow(): Flow<List<PracticeSessionEntity>> = flowOf(emptyList())
    
    suspend fun seedDatabaseAsNecessary() {}
    
    suspend fun getDeckById(deckId: String): DeckEntity? = null
    
    fun getVocabulariesInDeckFlow(deckId: String): Flow<List<VocabularyEntity>> = flowOf(emptyList())
    
    suspend fun createDeck(name: String, description: String, tags: List<String>, goal: String, level: String) {}
    
    suspend fun deleteDeck(deckId: String) {}
    
    suspend fun addVocabularyToDeck(
        deckId: String, word: String, pronunciation: String, meaning: String,
        descEn: String, example: String, collocation: String, related: String, note: String
    ) {}
    
    suspend fun deleteVocabulary(vocabId: String) {}
    
    suspend fun updateVocabulary(
        id: String, word: String, pronunciation: String, meaning: String,
        descEn: String, example: String, collocation: String, related: String, note: String
    ) {}
    
    suspend fun importCsvContent(deckId: String, csvContent: String): ImportCsvResponse {
        return ImportCsvResponse(true, 0)
    }
    
    suspend fun getDueReviewAndNewWords(dailyGoal: Int): List<VocabularyWithReviewCard> = emptyList()
    
    suspend fun processVocabReview(vocabId: String, rating: String) {}
    
    suspend fun getVocabulariesInDeck(deckId: String): List<VocabularyEntity> = emptyList()
    
    suspend fun savePracticeSession(deckId: String, type: String, total: Int, correct: Int) {}

    // Practice Remote API Integrations
    suspend fun createPracticeSession(
        deckId: String,
        practiceTypes: List<String>?,
        totalQuestions: Int?
    ): CreateSessionResponse {
        return practiceApiService.createSession(
            CreatePracticeSessionRequest(deckId, practiceTypes, totalQuestions)
        )
    }

    suspend fun getActivePracticeSession(deckId: String): CreateSessionResponse? {
        return try {
            practiceApiService.getActiveSession(deckId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPracticeQuestions(sessionId: String): List<PracticeQuestionDto> {
        return practiceApiService.getQuestions(sessionId)
    }

    suspend fun submitPracticeAnswer(
        sessionId: String,
        questionIndex: Int,
        userAnswer: String?
    ): PracticeAnswerDto {
        return practiceApiService.submitAnswer(sessionId, SubmitAnswerRequest(questionIndex, userAnswer))
    }

    suspend fun finishPracticeSession(sessionId: String): FinishSessionResponse {
        return practiceApiService.finishSession(sessionId)
    }

    suspend fun cancelPracticeSession(sessionId: String): PracticeSessionDto {
        return practiceApiService.cancelSession(sessionId)
    }
}
