package com.minlish.feature.settings.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface NotificationApiService {
    @GET("notifications/settings")
    suspend fun getSettings(): NotificationSettingsResponse

    @PATCH("notifications/settings")
    suspend fun updateSettings(@Body request: UpdateSettingsRequest): NotificationSettingsResponse

    @POST("notifications/device-token")
    suspend fun registerDeviceToken(@Body request: CreateDeviceTokenRequest): Any
}