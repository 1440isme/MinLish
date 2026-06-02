package com.minlish.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar
import androidx.work.*
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val DAILY_REQUEST_CODE = 5001
    private const val REVIEW_REQUEST_CODE = 5002

    //1. Hệ thống thông báo hằng ngày
    fun scheduleDailyReminder(context: Context, timeStr: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Trích xuất Giờ và Phút an toàn
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_type", "daily")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Kiểm tra quyền gác cổng Exact Alarm hệ thống
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                // Nếu máy đã cho phép quyền gác cổng, nổ thông báo ĐÚNG CHÍNH XÁC TỪNG GIÂY
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                android.util.Log.d("MINLISH_NOTI", "🔥 Scheduled with EXACT precision at: ${calendar.time}")
            } else {
                // Nếu bị hệ thống hạn chế (chế độ tiết kiệm pin cực hạn), hạ cấp xuống linh hoạt tránh sập app
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                android.util.Log.d("MINLISH_NOTI", "⚠️ Scheduled with Inexact precision due to OS restrictions")
            }
        } else {
            // Các đời máy cũ Android 11 trở xuống: Mặc định chạy chính xác tuyệt đối không cần xin quyền
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

    }

    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    // 2. Hệ thống thông báo thẻ ến hạn
    // Khung giờ tĩnh
    fun scheduleReviewCheck(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_type", "review")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REVIEW_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        //Cài đặt cứ sau 2 phút, chip máy tự động thức giấc quét API Server một lần
        //val intervalMinutes = 2
        //val triggerAt = System.currentTimeMillis() + (intervalMinutes * 60 * 1000)

        // thuaạt toán định vị: Tìm mốc giờ tiếp theo (9h, 15h, 21h)
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)

        val checkpointHours = listOf(9, 15, 21)
        var targetHour = -1

        for (hour in checkpointHours) {
            if (currentHour < hour) {
                targetHour = hour
                break
            }
        }

        if (targetHour != -1) {
            // Trường hợp A: Vẫn còn mốc giờ vàng trong ngày hôm nay
            cal.set(Calendar.HOUR_OF_DAY, targetHour)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        } else {
            // Trường hợp B: Đã qua 21h đêm, mốc tiếp theo bắt buộc phải là 9h sáng mai
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }

        val triggerAt = cal.timeInMillis

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAt, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)

            android.util.Log.d("MINLISH_NOTI", " [FIXED CHECKPOINT] Next Silent Review Check scheduled precisely at: ${cal.time}")        } catch (e: Exception) {
            // Cổng dự phòng an toàn cho các đời máy cũ
            //alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            //android.util.Log.e("MINLISH_NOTI", "Fallback to inexact alarm due to error: ${e.localizedMessage}")
        }
         catch (e: Exception) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancelReviewCheck(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, REVIEW_REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
        android.util.Log.d("MINLISH_NOTI", "Review Check Alarm Canceled")
    }
}