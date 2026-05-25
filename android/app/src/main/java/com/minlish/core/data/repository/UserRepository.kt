package com.minlish.core.data.repository

import com.minlish.core.datastore.TokenManager
import com.minlish.core.network.UserApiService
import com.minlish.core.network.dto.UpdateProfileRequest
import com.minlish.core.network.dto.UserDto

class UserRepository(
    private val userApiService: UserApiService,
    private val tokenManager: TokenManager
) {
    suspend fun getProfile(): UserDto {
        val userDto = userApiService.getProfile()
        tokenManager.saveAuthResponse(
            accessToken = tokenManager.getAccessTokenBlocking() ?: "",
            refreshToken = tokenManager.getRefreshTokenBlocking() ?: "",
            fullName = userDto.fullName,
            email = userDto.email,
            learningGoal = userDto.learningGoal,
            dailyGoal = userDto.dailyNewWordsGoal,
            isOnboarded = true
        )
        return userDto
    }

    suspend fun updateDailyNewWordsGoal(goal: Int): UserDto {
        val userDto = userApiService.updateProfile(UpdateProfileRequest(dailyNewWordsGoal = goal))
        tokenManager.updateDailyGoal(userDto.dailyNewWordsGoal)
        return userDto
    }

    suspend fun updateProfile(fullName: String?, learningGoal: String?): UserDto {
        val userDto = userApiService.updateProfile(
            UpdateProfileRequest(
                fullName = fullName,
                learningGoal = learningGoal
            )
        )
        fullName?.let { tokenManager.updateFullName(it) }
        learningGoal?.let { tokenManager.updateLearningGoal(it) }
        return userDto
    }
}
