package com.minlish.core.data.repository

import com.minlish.core.data.model.DashboardAnalyticsDto
import com.minlish.feature.analytics.data.AnalyticsApiService
import com.minlish.core.data.model.PracticeSessionEntity

class AnalyticsRepository(
    private val analyticsApiService: AnalyticsApiService
) {
    // Hàm bốc dữ liệu Dashboard từ Server về cho điện thoại sử dụng
    suspend fun getDashboardAnalytics(): DashboardAnalyticsDto {
        return analyticsApiService.getDashboardAnalytics()
    }

    //lấy mảng dữ liệu cho viewmodel
    suspend fun getRemoteHistory(): List<PracticeSessionEntity> {
        return analyticsApiService.getRemotePracticeHistory()
    }
}