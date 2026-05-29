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
            isOnboarded = true,
            currentLevelId = userDto.currentLevelId,
            targetLevelId = userDto.targetLevelId,
            avatarUrl = userDto.avatarUrl
        )
        return userDto
    }

    suspend fun updateDailyNewWordsGoal(goal: Int): UserDto {
        val userDto = userApiService.updateProfile(UpdateProfileRequest(dailyNewWordsGoal = goal))
        tokenManager.updateDailyGoal(userDto.dailyNewWordsGoal)
        return userDto
    }

    suspend fun updateProfile(
        fullName: String? = null,
        avatarUrl: String? = null,
        learningGoal: String? = null,
        currentLevelId: String? = null,
        targetLevelId: String? = null
    ): UserDto {
        val userDto = userApiService.updateProfile(
            UpdateProfileRequest(
                fullName = if (fullName.isNullOrBlank()) null else fullName,
                avatarUrl = if (avatarUrl.isNullOrBlank()) null else avatarUrl,
                learningGoal = if (learningGoal.isNullOrBlank()) null else learningGoal,
                currentLevelId = if (currentLevelId.isNullOrBlank()) null else currentLevelId,
                targetLevelId = if (targetLevelId.isNullOrBlank()) null else targetLevelId
            )
        )
        fullName?.let { if (it.isNotBlank()) tokenManager.updateFullName(it) }
        avatarUrl?.let { tokenManager.updateAvatarUrl(it) }
        learningGoal?.let { if (it.isNotBlank()) tokenManager.updateLearningGoal(it) }
        currentLevelId?.let { if (it.isNotBlank()) tokenManager.updateCurrentLevelId(it) }
        targetLevelId?.let { if (it.isNotBlank()) tokenManager.updateTargetLevelId(it) }
        return userDto
    }
}
