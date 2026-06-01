package com.minlish.feature.analytics.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minlish.core.data.model.DashboardAnalyticsDto
import com.minlish.core.presentation.MinLishViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: MinLishViewModel,
    stats: DashboardAnalyticsDto,
    onSessionClick: (String) -> Unit
) {
    val listPractices by viewModel.practiceSessions.collectAsState()

    //Kích hoạt tự động mỗi khi bấm vào trang Stats để load lại dữ liệu mới
    LaunchedEffect(Unit) {
        viewModel.fetchDashboardAnalytics()
    }
    val accentTeal = Color(0xFF0D9488)

    //Tổng số practice đã thực hiện :
    val density = androidx.compose.ui.platform.LocalDensity.current
    val textPaint = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#0D9488") // Màu Teal cho số liệu
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = with(density) { 13.sp.toPx() } // Chữ tự co giãn theo tỷ lệ màn hình
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            isAntiAlias = true
        }
    }

    //Tính toán tần suất học trong tuần
    val volumes = remember(listPractices) {
        val counts = FloatArray(7) { 0f } // Khởi tạo mảng 7 ngày: Mon=0, Tue=1 ... Sun=6

        // 1. Tìm mốc 00:00:00 của ngày Thứ Hai đầu tuần này
        val currentCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Ép hệ thống coi Thứ Hai là ngày đầu tuần theo chuẩn Việt Nam/Anh
            firstDayOfWeek = Calendar.MONDAY

            // Lùi độ số ngày về đúng Thứ Hai đầu tuần gần nhất
            while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                add(Calendar.DAY_OF_YEAR, -1)
            }
        }

        val startOfWeekMs = currentCal.timeInMillis // Điểm bắt đầu: Thứ Hai 00:00
        val endOfWeekMs = startOfWeekMs + (7L * 24 * 60 * 60 * 1000) // Điểm kết thúc: Chủ Nhật 23:59 (Sau 7 ngày)

        // 2. Vòng lặp phân loại bài tập vào đúng các thứ trong tuần
        listPractices.forEach { session ->
            // Bảo vệ an toàn: Kiểm tra nếu mốc bài làm nằm trọn trong tuần này
            if (session.finishedAt in startOfWeekMs until endOfWeekMs) {
                val sessionCal = Calendar.getInstance().apply { timeInMillis = session.finishedAt }
                val dayOfWeek = sessionCal.get(Calendar.DAY_OF_WEEK)

                // Ánh xạ ngày chuẩn xác sang index mảng (Mon=0, Tue=1 ... Sun=6)
                val index = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
                if (index in 0..6) {
                    counts[index] += 1f // Tăng chiều cao thêm 1 đơn vị cho cột thứ đó
                }
            }
        }
        counts.toList()
    }

    //Phòng trường hợp chia cho 0 nếu tuần này chưa làm bài nào
    val maxVolume = remember(volumes) { volumes.maxOrNull()?.coerceAtLeast(1f) ?: 1f }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) Color(0xFF0F1E1B) else Color(0xFFF4F9F8))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Progress Insights",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 1: Visual bar analytics
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Weekly Practice Frequency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                    val barWidth = 32.dp.toPx()
                    val spacing = (size.width - (barWidth * days.size)) / (days.size + 1)

                    days.forEachIndexed { i, _ ->
                        val volume = volumes[i]
                        val barHeight = (volume / maxVolume) * (size.height - 30.dp.toPx())

                        val startX = spacing + i * (barWidth + spacing)
                        val startY = size.height - 20.dp.toPx() - barHeight

                        // Draw background track
                        drawRoundRect(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            topLeft = Offset(startX, 0f),
                            size = Size(barWidth, size.height - 20.dp.toPx()),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )

                        // Draw active value bar
                        if (barHeight > 0f) {
                            drawRoundRect(
                                color = accentTeal,
                                topLeft = Offset(startX, startY),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                            // Thêm số lieệu cụ thể lên đỉnh cột
                            drawContext.canvas.nativeCanvas.drawText(
                                volume.toInt().toString(), // In con số thực tế
                                startX + (barWidth / 2),   // Căn giữa cột
                                startY - 12.dp.toPx(),     // Nổi lên trên đỉnh cột 12dp
                                textPaint
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val daysLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    daysLabels.forEach { label ->
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Accuracy dialing info card
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Overall Practice Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Dòng 1: tỉ lệ chính xác
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = accentTeal,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        val formattedAccuracy = String.format(Locale.US, "%.1f", stats.accuracy)
                        Text(
                            text = "$formattedAccuracy% Accuracy Rate",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = accentTeal
                        )
                        Text(
                            text = "Calculated from your overall performance across all completed quiz sessions.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(16.dp))

                // Dòng 2: Tổng số bài làm
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "${stats.totalPractices} Sessions Completed",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFFD97706)
                        )
                        Text(
                            text = "Total practice quizzes taken so far.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Practice session      list
        Text(
            text = "Practice History (5 most recent)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (listPractices.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No completed practices found yet.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    listPractices.take(5).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSessionClick(item.id) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thêm Modifier.weight(1f) để ép Column chỉ lấy phần không gian còn lại,
                            // nhường chỗ cho text bên phải không bao giờ bị chèn
                            Column(modifier = Modifier.weight(1f).padding(end = 16.dp))
                            {
                                Text(
                                    text = if (!item.deckName.isNullOrBlank()) {
                                        "${item.deckName} quiz"
                                    } else {
                                        "Practice quiz"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 2 // Cho phép tự động xuống dòng nếu tên quá dài
                                )
                                // Tự động nhân 1000 nếu server trả về giây ( chống lỗi 1970 )
                                val finishedMillis = if (item.finishedAt < 1000000000000L) item.finishedAt * 1000 else item.finishedAt
                                val dateFormat = SimpleDateFormat("HH:mm - MMM dd, yyyy", Locale.US)
                                Text(
                                    text = dateFormat.format(Date(item.finishedAt)),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            // Giữ không gian cố định cho kết quả câu hỏi
                            val isCancelled = item.status.toString().uppercase(Locale.US) == "CANCELLED"

                            Text(
                                text = if (isCancelled) "Cancelled" else { "${item.correctAnswers} / ${item.totalQuestions} answers" },
                                //text = "${item.correctAnswers} / ${item.totalQuestions} answers",
                                fontWeight = FontWeight.Bold,
                                color = if (isCancelled) Color(0xFFEF4444) else accentTeal, //pratice bị huỷ thì sẽ hiện đỏ
                                //color =  accentTeal, //pratice bị huỷ thì sẽ hiện đỏ
                                fontSize = 13.sp,
                                maxLines = 1 // Ép giữ trên một dòng duy nhất
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}