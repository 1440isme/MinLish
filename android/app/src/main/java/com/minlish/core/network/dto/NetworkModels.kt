package com.minlish.core.network.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("fullName") val fullName: String
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("authProvider") val authProvider: String,
    @SerializedName("learningGoal") val learningGoal: String?,
    @SerializedName("dailyNewWordsGoal") val dailyNewWordsGoal: Int,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("currentLevelId") val currentLevelId: String?,
    @SerializedName("targetLevelId") val targetLevelId: String?
)

data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("user") val user: UserDto
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class UpdateProfileRequest(
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("learningGoal") val learningGoal: String? = null,
    @SerializedName("dailyNewWordsGoal") val dailyNewWordsGoal: Int? = null,
    @SerializedName("timezone") val timezone: String? = null,
    @SerializedName("currentLevelId") val currentLevelId: String? = null,
    @SerializedName("targetLevelId") val targetLevelId: String? = null
)

data class GoogleLoginRequest(
    @SerializedName("idToken") val idToken: String
)
