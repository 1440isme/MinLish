package com.minlish.core.network

import com.minlish.core.network.dto.LearningLevelDto
import retrofit2.http.GET

interface LevelsApiService {
    @GET("levels")
    suspend fun getLevels(): List<LearningLevelDto>
}
