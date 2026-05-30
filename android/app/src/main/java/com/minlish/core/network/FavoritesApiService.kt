package com.minlish.core.network

import com.minlish.core.network.dto.FavoriteResponseDto
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoritesApiService {

    @POST("vocabularies/{id}/favorite")
    suspend fun favorite(@Path("id") vocabularyId: String): FavoriteResponseDto

    @DELETE("vocabularies/{id}/favorite")
    suspend fun unfavorite(@Path("id") vocabularyId: String)
}
