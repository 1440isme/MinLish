package com.minlish.core.network

import com.minlish.core.network.dto.CreateVocabularyRequest
import com.minlish.core.network.dto.PaginatedResponseDto
import com.minlish.core.network.dto.UpdateVocabularyRequest
import com.minlish.core.network.dto.VocabularyDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VocabulariesApiService {

    @GET("decks/{deckId}/vocabularies")
    suspend fun listByDeck(
        @Path("deckId") deckId: String,
        @Query("page") page: Int? = 1,
        @Query("pageSize") pageSize: Int? = 50,
        @Query("search") search: String? = null,
    ): PaginatedResponseDto<VocabularyDto>

    @POST("decks/{deckId}/vocabularies")
    suspend fun createInDeck(
        @Path("deckId") deckId: String,
        @Body body: CreateVocabularyRequest,
    ): VocabularyDto

    @PATCH("vocabularies/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body body: UpdateVocabularyRequest,
    ): VocabularyDto

    @DELETE("vocabularies/{id}")
    suspend fun delete(@Path("id") id: String)
}
