package com.minlish.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.minlish.MinLishApplication
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val actualTime = java.util.Calendar.getInstance().time
        val notiType = intent.getStringExtra("notification_type") ?: "daily"
        android.util.Log.d("MINLISH_NOTI", "!!! NOTIFICATION RECEIVER TRIGGERED AT: $actualTime | Type: $notiType")

        // Phân luồng xử lý
        if (notiType == "review") {
            // Bật cổng chạy bất đồng bộ ngầm để gọi API không gây đơ máy
            val pendingResult = goAsync()
            val app = context.applicationContext as MinLishApplication

            GlobalScope.launch {
                try {
                    // 1. Phóng API lên server NestJS đếm số lượng từ đến hạn thời gian thực
                    val stats = app.analyticsRepository.getDashboardAnalytics()

                    //Lấy logcat
                    android.util.Log.d("MINLISH_NOTI", "Server Response Cloud -> dueToday = ${stats.dueToday}")

                    // 2. Nếu database Prisma đếm ra có từ cần học (> 0)
                    if (stats.dueToday > 0) {
                        showNotification(context, "Review Cards Due!", "You have vocabulary words ready for review. Keep your streak alive!", 992, 2002)
                    }
                    else{
                        android.util.Log.d("MINLISH_NOTI", "Silent Check: No due words today (dueToday = 0). Staying quiet.")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MINLISH_NOTI", "CRITICAL ERROR IN BACKGROUND API CALL: ", e)
                } finally {
                    // Quan trọng : Sau khi check xong, tự động đặt lệnh hẹn giờ tiếp theo sau 2 phút,
                    // tạo thành vòng lặp tuần hoàn vĩnh cửu
                    NotificationScheduler.scheduleReviewCheck(context)
                    pendingResult.finish() // Đóng cổng an toàn
                }
            }
        } else {
            // Thông báo hằng ngày: Phát ngay lập tức không cần check API
            showNotification(
                context,
                "Time to expand your vocabulary! 🔥",
                "Your daily English challenge is waiting. Open MinLish now!",
                991,
                2001
            )
        }
    }

    // Hàm phụ trợ đúc giao diện hộp thông báo
    private fun showNotification(context: Context, title: String, content: String, notiId: Int, requestCode: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "minlish_reminders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "MinLish Alerts", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Cloud and Local notification updates for MinLish learners"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context, requestCode, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notiId, notification)
    }
}