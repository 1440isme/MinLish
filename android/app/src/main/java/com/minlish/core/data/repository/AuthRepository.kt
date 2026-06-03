package com.minlish.core.data.repository

import com.minlish.core.datastore.TokenManager
import com.minlish.core.network.AuthApiService
import com.minlish.core.network.dto.LoginRequest
import com.minlish.core.network.dto.RegisterRequest
import com.minlish.core.network.dto.GoogleLoginRequest
import com.minlish.core.network.dto.AuthResponse

class AuthRepository(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) {
    suspend fun loginWithGoogle(idToken: String): AuthResponse {
        val response = authApiService.loginWithGoogle(GoogleLoginRequest(idToken))
        val isNewUser = response.user.learningGoal.isNullOrBlank()
        tokenManager.saveAuthResponse(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            fullName = response.user.fullName,
            email = response.user.email,
            learningGoal = response.user.learningGoal,
            dailyGoal = response.user.dailyNewWordsGoal,
            isOnboarded = true,
            currentLevelId = response.user.currentLevelId,
            targetLevelId = response.user.targetLevelId,
            hasShownGoalSetup = !isNewUser
        )
        return response
    }

    suspend fun login(email: String, password: String): AuthResponse {
        val response = authApiService.login(LoginRequest(email, password))
        tokenManager.saveAuthResponse(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            fullName = response.user.fullName,
            email = response.user.email,
            learningGoal = response.user.learningGoal,
            dailyGoal = response.user.dailyNewWordsGoal,
            isOnboarded = true,
            currentLevelId = response.user.currentLevelId,
            targetLevelId = response.user.targetLevelId,
            hasShownGoalSetup = true
        )
        return response
    }

    suspend fun register(email: String, password: String, fullName: String): AuthResponse {
        val response = authApiService.register(RegisterRequest(email, password, fullName))
        tokenManager.saveAuthResponse(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            fullName = response.user.fullName,
            email = response.user.email,
            learningGoal = response.user.learningGoal,
            dailyGoal = response.user.dailyNewWordsGoal,
            isOnboarded = true,
            currentLevelId = response.user.currentLevelId,
            targetLevelId = response.user.targetLevelId,
            hasShownGoalSetup = false
        )
        return response
    }

    suspend fun logout() {
        tokenManager.clearAuth()
    }
}
