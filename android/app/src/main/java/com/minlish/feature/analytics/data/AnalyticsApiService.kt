package com.minlish.feature.analytics.data

import com.minlish.core.data.model.DashboardAnalyticsDto
import com.minlish.core.data.model.PracticeSessionEntity
import retrofit2.http.GET

interface AnalyticsApiService {
    @GET("analytics/dashboard")
    suspend fun getDashboardAnalytics(): DashboardAnalyticsDto

    @GET("analytics/history")
    suspend fun getRemotePracticeHistory(): List<PracticeSessionEntity>
}