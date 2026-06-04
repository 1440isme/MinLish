package com.minlish.feature.settings.presentation

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import com.minlish.core.utils.showToast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.minlish.core.notification.NotificationScheduler
import com.minlish.feature.profile.presentation.ProfileSettingsViewModel
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.minlish.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ProfileSettingsViewModel, onBackClick: () -> Unit) {
    val notiSettings by viewModel.notificationSettings.collectAsState()

    val accentTeal = Color(0xFF0D9488)
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Lấy chuỗi thông báo lỗi bằng hàm chuẩn của Compose để triệt tiêu vết bôi đỏ Lint hoàn toàn
    val permissionDeniedMessage = stringResource(R.string.settings_notif_permission_denied)

    // Lắng nghe trạng thái xem người dùng đang muốn kích hoạt công tắc nào để bọc lót cấp lịch sau khi xin quyền thành công
    var pendingActionType by remember { mutableStateOf<String?>(null) } // "daily" hoặc "review"

    // Bộ phóng hệ thống xin quyền Runtime chuẩn Jetpack Compose
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Đã bật quyền thông báo thành công!", Toast.LENGTH_SHORT).show()

            // Thực thi tiếp tục hành động còn dang dở sau khi người dùng bấm "Cho phép"
            when (pendingActionType) {
                "daily" -> {
                    viewModel.updateNotificationToggle(dailyEnabled = true)
                    notiSettings?.dailyReminderTime?.let { timeStr ->
                        val cleanTime = if (timeStr.contains("T")) timeStr.substringAfter("T")
                            .substringBefore(".") else timeStr.substringBefore(".")
                        NotificationScheduler.scheduleDailyReminder(context, cleanTime)
                    }
                }

                "review" -> {
                    viewModel.updateNotificationToggle(dueEnabled = true)
                    NotificationScheduler.scheduleReviewCheck(context)
                }
            }
        }
        else {
            // ✅ ĐÃ SỬA: Thay thế context.getString() thành biến permissionDeniedMessage cực kỳ sạch lỗi
            showToast(context, permissionDeniedMessage, Toast.LENGTH_LONG)
        }
        pendingActionType = null // Reset trạng thái chờ
    }

    // Tự động kéo cấu hình thông báo mới nhất từ Server khi mở màn hình
    LaunchedEffect(Unit) {
        viewModel.fetchNotificationSettings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = accentTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isSystemInDarkTheme()) Color(0xFF0F1E1B) else Color(0xFFFFF9F2))
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tiêu đề hệ thống thông báo
            Text(stringResource(R.string.settings_section_reminders), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accentTeal)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (notiSettings == null) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = accentTeal)
                        }
                    } else {
                        val currentNoti = notiSettings!!

                        // Công tấc 1: Nhắc nhở học tập hằng ngày
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = accentTeal)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(stringResource(R.string.settings_daily_reminder), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(stringResource(R.string.settings_daily_reminder_desc), fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            Switch(
                                checked = currentNoti.dailyReminderEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        // Kiểm tra quyền : Nếu máy chạy Android 13+ mà chưa cấp quyền thông báo, kích hoạt hộp thoại xin quyền
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                                        {
                                            pendingActionType = "daily"
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                        else
                                        {
                                            viewModel.updateNotificationToggle(dailyEnabled = true)
                                            val cleanTime = currentNoti.dailyReminderTime.substringAfter("T").substringBefore(".")
                                            NotificationScheduler.scheduleDailyReminder(context, cleanTime)
                                        }
                                    } else {
                                        viewModel.updateNotificationToggle(dailyEnabled = false)
                                        NotificationScheduler.cancelDailyReminder(context)
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = accentTeal, checkedTrackColor = accentTeal.copy(alpha = 0.3f))
                            )
                        }

                        // Thiết lập time_picker dialog để chọn giờ
                        if (currentNoti.dailyReminderEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 36.dp, top = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.settings_preferred_time), fontSize = 13.sp, color = Color.DarkGray)

                                val cleanTime = currentNoti.dailyReminderTime.substringAfter("T").substringBeforeLast(":")

                                TextButton(
                                    onClick = {
                                        val calendar = Calendar.getInstance()
                                        val dialog = TimePickerDialog(
                                            context,
                                            { _, hourOfDay, minute ->
                                                val formattedTime = String.format(Locale.US, "%02d:%02d:00", hourOfDay, minute)
                                                // 1. Đẩy lệnh PATCH lên Server Cloud
                                                viewModel.updateNotificationToggle(timeStr = formattedTime)
                                                // 2. Đồng bộ đặt lịch báo thức ngầm dưới chip máy di động ngay lập tức
                                                NotificationScheduler.scheduleDailyReminder(context, formattedTime)
                                            },
                                            calendar.get(Calendar.HOUR_OF_DAY),
                                            calendar.get(Calendar.MINUTE),
                                            true // Định dạng 24h
                                        )
                                        dialog.show()
                                    }
                                )
                                {
                                    Text(text = cleanTime, fontWeight = FontWeight.Bold, color = accentTeal, fontSize = 15.sp)
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Black.copy(alpha = 0.05f))

                        // Công tắc 2: Nhắc nhở từ vựng đến hạn ôn tập
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = accentTeal)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(stringResource(R.string.settings_review_due), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(stringResource(R.string.settings_review_due_desc), fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            Switch(
                                checked = currentNoti.dueReviewReminderEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        // Kiểm tra gác cổng quyền hệ điều hành Android 13+ cho luồng Review
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                                            // Gán biến chờ cho luồng Review để tránh bị null chặn luồng local
                                            pendingActionType = "review"
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            viewModel.updateNotificationToggle(dueEnabled = true)
                                            NotificationScheduler.scheduleReviewCheck(context)
                                        }
                                    } else {
                                        viewModel.updateNotificationToggle(dueEnabled = false)
                                        NotificationScheduler.cancelReviewCheck(context)
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = accentTeal, checkedTrackColor = accentTeal.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }

        }
    }
}