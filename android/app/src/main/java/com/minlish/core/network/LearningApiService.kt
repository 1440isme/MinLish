package com.minlish.core.network

import com.minlish.core.network.dto.DailyPlanResponseDto
import com.minlish.core.network.dto.DueCardsResponseDto
import com.minlish.core.network.dto.RecentLearningDeckResponseDto
import com.minlish.core.network.dto.SubmitReviewRequest
import com.minlish.core.network.dto.SubmitReviewResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LearningApiService {

    @GET("learning/recent-deck")
    suspend fun getRecentDeck(): RecentLearningDeckResponseDto

    @GET("learning/daily-plan")
    suspend fun getDailyPlan(
        @Query("deckId") deckId: String? = null,
    ): DailyPlanResponseDto

    @POST("learning/decks/{deckId}/start")
    suspend fun startDeck(
        @Path("deckId") deckId: String,
        @Query("limitNewWords") limitNewWords: Int? = null,
    ): DailyPlanResponseDto

    @GET("learning/due")
    suspend fun getDueCards(
        @Query("deckId") deckId: String? = null,
        @Query("limit") limit: Int? = null,
    ): DueCardsResponseDto

    @POST("learning/review")
    suspend fun submitReview(
        @Body body: SubmitReviewRequest,
    ): SubmitReviewResponseDto
}
