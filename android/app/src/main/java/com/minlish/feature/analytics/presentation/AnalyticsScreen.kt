package com.minlish.feature.analytics.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
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
import com.minlish.feature.practice.presentation.PracticeQuizViewModel
import androidx.compose.ui.res.stringResource
import com.minlish.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: PracticeQuizViewModel,
    stats: DashboardAnalyticsDto,
    onSessionClick: (String) -> Unit,
    onRefreshStats: () -> Unit,
) {
    val listPractices by viewModel.practiceSessions.collectAsState()

    //Kích hoạt tự động mỗi khi bấm vào trang Stats để load lại dữ liệu mới
    LaunchedEffect(Unit) {
        onRefreshStats()
        viewModel.fetchPracticeHistory()
    }
    val accentTeal = Color(0xFF0D9488)

    //Chữ in số lượng trên đỉnh biểu đồ
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
    //Nhận thẳng mảng số lượng bài làm đã gộp theo tuần từ bảng DailyActivity
    val volumes = remember(stats.weeklyPracticeCounts) {
        stats.weeklyPracticeCounts.map { it.toFloat() }
    }

    //Phòng trường hợp chia cho 0 nếu tuần này chưa làm bài nào
    val maxVolume = remember(volumes) { volumes.maxOrNull()?.coerceAtLeast(1f) ?: 1f }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) Color(0xFF0F1E1B) else Color(0xFFFFF9F2))
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
    ) {
        Text(
            text = stringResource(R.string.analytics_title),
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
                    text = stringResource(R.string.analytics_weekly_frequency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    val days = listOf(
                        "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
                    )

                    val barWidth = 32.dp.toPx()
                    val spacing = (size.width - (barWidth * days.size)) / (days.size + 1)

                    days.forEachIndexed { i, _ ->
                        val volume = volumes.getOrElse(i) {0f}
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
                    val daysLabels = listOf(
                        stringResource(R.string.day_mon),
                        stringResource(R.string.day_tue),
                        stringResource(R.string.day_wed),
                        stringResource(R.string.day_thu),
                        stringResource(R.string.day_fri),
                        stringResource(R.string.day_sat),
                        stringResource(R.string.day_sun)
                    )
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

        // Accuracy dialing info card : accuracy theo tuần + lịch sử so sánh 4 tuần + tổng số bài làm
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF000000).copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.analytics_weekly_stats_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1A)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Dòng 1: tỉ lệ chính xác
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = accentTeal,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        val formattedAccuracy = String.format(Locale.US, "%.1f", stats.accuracy)
                        Text(
                            text = stringResource(R.string.analytics_current_week_accuracy, formattedAccuracy),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = accentTeal
                        )
                        Text(
                            text = stringResource(R.string.analytics_current_week_desc),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Biểu đồ hàng ngang so sánh accuracy
                if (stats.weeklyAccuracyHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.Black.copy(alpha = 0.03f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.analytics_performance_trend_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C776E)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        stats.weeklyAccuracyHistory.forEachIndexed { idx, acc ->
                            val label = when(idx) {
                                3 -> stringResource(R.string.analytics_trend_this_week)
                                else -> stringResource(R.string.analytics_trend_weeks_ago, 3 - idx)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (idx == 3) accentTeal.copy(alpha = 0.1f) else Color(0xFFF3F4F6))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$acc%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (idx == 3) accentTeal else Color(0xFF1C1C1A)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = label, fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(16.dp))

                // Dòng 2: Tổng số bài làm đó giờ
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        val weeklyTotalSessions = stats.weeklyPracticeCounts.sum()
                        Text(
                            text = stringResource(R.string.analytics_sessions_completed, weeklyTotalSessions),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFFD97706)
                        )
                        Text(
                            text = stringResource(R.string.analytics_sessions_desc),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Practice session list
        Text(
            text = stringResource(R.string.analytics_history_title),
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
                        Text(stringResource(R.string.analytics_no_history), color = Color.Gray, fontSize = 12.sp)
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
                                        stringResource(R.string.analytics_deck_quiz, item.deckName)
                                    } else {
                                        stringResource(R.string.analytics_practice_quiz)
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 2 // Cho phép tự động xuống dòng nếu tên quá dài
                                )
                                // Tự động nhân 1000 nếu server trả về giây ( chống lỗi 1970 )
                                val finishedMillis = if (item.finishedAt < 1000000000000L) item.finishedAt * 1000 else item.finishedAt
                                val locale = if (Locale.getDefault().language == "vi") Locale("vi", "VN") else Locale.US
                                val dateFormat = SimpleDateFormat("HH:mm - dd/MM/yyyy", locale)
                                Text(
                                    text = dateFormat.format(Date(finishedMillis)),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            // Giữ không gian cố định cho kết quả câu hỏi
                            val isCancelled = item.status.toString().uppercase(Locale.US) == "CANCELLED"

                            Text(
                                text = if (isCancelled) stringResource(R.string.analytics_cancelled) else {
                                    stringResource(R.string.analytics_answers_count, item.correctAnswers, item.totalQuestions)
                                },
                                fontWeight = FontWeight.Bold,
                                color = if (isCancelled) Color(0xFFEF4444) else accentTeal, //pratice bị huỷ thì sẽ hiện đỏ
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
