package com.minlish.core.network

import com.minlish.core.network.dto.ImportCsvResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ImportsApiService {

    @Multipart
    @POST("decks/{deckId}/import-csv")
    suspend fun importCsv(
        @Path("deckId") deckId: String,
        @Part file: MultipartBody.Part,
    ): ImportCsvResponseDto
}
