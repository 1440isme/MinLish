package com.minlish.core.network

import com.minlish.core.network.dto.ImportCsvResponseDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.GET
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

    @GET("decks/{deckId}/export-csv")
    suspend fun exportCsv(
        @Path("deckId") deckId: String,
    ): Response<ResponseBody>
}
