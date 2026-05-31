package com.minlish.feature.settings.data

import com.google.gson.annotations.SerializedName

// Biểu mẫu dùng làm Body gửi lệnh cập nhật lên Server NestJS
data class UpdateSettingsRequest(
    @SerializedName("dailyReminderEnabled") val dailyReminderEnabled: Boolean? = null,
    @SerializedName("dailyReminderTime") val dailyReminderTime: String? = null,
    @SerializedName("dueReviewReminderEnabled") val dueReviewReminderEnabled: Boolean? = null,
    @SerializedName("pushEnabled") val pushEnabled: Boolean? = null,
    @SerializedName("emailEnabled") val emailEnabled: Boolean? = null
)

// Biểu mẫu dùng để hứng trọn gói dữ liệu cấu hình trả về từ Server
data class NotificationSettingsResponse(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("dailyReminderEnabled") val dailyReminderEnabled: Boolean,
    @SerializedName("dailyReminderTime") val dailyReminderTime: String, // Chuỗi ISO từ Server
    @SerializedName("dueReviewReminderEnabled") val dueReviewReminderEnabled: Boolean,
    @SerializedName("pushEnabled") val pushEnabled: Boolean,
    @SerializedName("emailEnabled") val emailEnabled: Boolean
)

// Biểu mẫu gửi mã Token máy lên Server
data class CreateDeviceTokenRequest(
    @SerializedName("token") val token: String,
    @SerializedName("platform") val platform: String = "ANDROID",
    @SerializedName("deviceName") val deviceName: String
)