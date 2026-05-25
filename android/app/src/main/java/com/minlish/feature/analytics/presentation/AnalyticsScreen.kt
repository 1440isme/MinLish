package com.minlish.feature.analytics.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
    stats: DashboardAnalyticsDto
) {
    val listPractices by viewModel.practiceSessions.collectAsState()

    val accentTeal = Color(0xFF0D9488)

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
                    val volumes = listOf(4f, 8f, 5f, 12f, 7f, 15f, 9f)
                    val maxVolume = 15f

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
                        drawRoundRect(
                            color = accentTeal,
                            topLeft = Offset(startX, startY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
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
                    text = "Historical Accuracy Metrics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

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
                            text = "Based on continuous SM-2 ratings and quiz results logs compiled locally.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Practice session logs list
        Text(
            text = "Activity History logs",
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
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.practiceType.replace("_", " "),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                val dateFormat = SimpleDateFormat("HH:mm - MMM dd, yyyy", Locale.US)
                                Text(
                                    text = dateFormat.format(Date(item.finishedAt)),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Text(
                                text = "${item.correctAnswers} / ${item.totalQuestions} answers",
                                fontWeight = FontWeight.Bold,
                                color = accentTeal,
                                fontSize = 13.sp
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
