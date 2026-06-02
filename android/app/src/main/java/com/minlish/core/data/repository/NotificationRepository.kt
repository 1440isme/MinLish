package com.minlish.core.data.repository

import com.minlish.feature.settings.data.CreateDeviceTokenRequest
import com.minlish.feature.settings.data.NotificationApiService
import com.minlish.feature.settings.data.NotificationSettingsResponse
import com.minlish.feature.settings.data.UpdateSettingsRequest

class NotificationRepository(
    private val notificationApiService: NotificationApiService
) {
    // 1. Hàm lấy cấu hình giờ nhắc nhở hiện tại
    suspend fun getSettings(): NotificationSettingsResponse {
        return notificationApiService.getSettings()
    }

    // 2. Hàm gửi lệnh cập nhật trạng thái bật/tắt hoặc thay đổi giờ giấc nhắc học
    suspend fun updateSettings(request: UpdateSettingsRequest): NotificationSettingsResponse {
        return notificationApiService.updateSettings(request)
    }

    // 3. Hàm đăng ký Token thiết bị di động lên Server
    suspend fun registerDeviceToken(token: String, deviceName: String) {
        val request = CreateDeviceTokenRequest(token = token, deviceName = deviceName)
        notificationApiService.registerDeviceToken(request)
    }
}