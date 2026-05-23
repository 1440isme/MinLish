package com.minlish.core.network

import com.minlish.core.network.dto.UpdateProfileRequest
import com.minlish.core.network.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserApiService {
    @GET("users/me")
    suspend fun getProfile(): UserDto

    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UserDto
}
