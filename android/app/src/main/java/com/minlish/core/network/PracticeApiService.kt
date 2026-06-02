package com.minlish.core.network

import com.minlish.core.network.dto.*
import retrofit2.http.*

interface PracticeApiService {
    @POST("practice/sessions")
    suspend fun createSession(@Body request: CreatePracticeSessionRequest): CreateSessionResponse

    @GET("practice/sessions/active")
    suspend fun getActiveSession(@Query("deckId") deckId: String): CreateSessionResponse?

    @GET("practice/sessions/{sessionId}/questions")
    suspend fun getQuestions(@Path("sessionId") sessionId: String): List<PracticeQuestionDto>

    @POST("practice/sessions/{sessionId}/answers")
    suspend fun submitAnswer(
        @Path("sessionId") sessionId: String,
        @Body request: SubmitAnswerRequest
    ): PracticeAnswerDto

    @POST("practice/sessions/{sessionId}/finish")
    suspend fun finishSession(@Path("sessionId") sessionId: String): FinishSessionResponse

    @GET("practice/sessions/{sessionId}/results")
    suspend fun getSessionResults(@Path("sessionId") sessionId: String): FinishSessionResponse

    @POST("practice/sessions/{sessionId}/cancel")
    suspend fun cancelSession(@Path("sessionId") sessionId: String): PracticeSessionDto
}
