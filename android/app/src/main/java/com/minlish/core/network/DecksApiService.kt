package com.minlish.core.network

import com.minlish.core.network.dto.CreateDeckRequest
import com.minlish.core.network.dto.DeckDto
import com.minlish.core.network.dto.PaginatedResponseDto
import com.minlish.core.network.dto.UpdateDeckRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DecksApiService {

    @GET("decks")
    suspend fun listDecks(
        @Query("type") type: String? = "ALL",
        @Query("search") search: String? = null,
        @Query("page") page: Int? = 1,
        @Query("pageSize") pageSize: Int? = 50,
    ): PaginatedResponseDto<DeckDto>

    @GET("decks/favorites")
    suspend fun getFavoritesDeck(): DeckDto

    @GET("decks/{id}")
    suspend fun getDeck(@Path("id") id: String): DeckDto

    @POST("decks")
    suspend fun createDeck(@Body body: CreateDeckRequest): DeckDto

    @PATCH("decks/{id}")
    suspend fun updateDeck(
        @Path("id") id: String,
        @Body body: UpdateDeckRequest,
    ): DeckDto

    @DELETE("decks/{id}")
    suspend fun deleteDeck(@Path("id") id: String)
}
