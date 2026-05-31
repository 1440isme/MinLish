package com.minlish.feature.home.presentation.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minlish.core.component.GiraffeMascot
import com.minlish.core.presentation.MinLishViewModel
import com.minlish.ui.theme.MinLishTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: MinLishViewModel,
    onStartDailyQuiz: () -> Unit,
    onStartDailyNew: () -> Unit,
    onNavigateToDecks: () -> Unit,
) {
    val name by viewModel.fullName.collectAsState()
    val stats by viewModel.dashboardAnalytics.collectAsState()
    val wordsGoal by viewModel.dailyNewWordsGoal.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F6EE))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Rovio Streak Weekly tracker at the very top!
        RovioWeeklyTracker(streakCount = stats.streak)

        // Greeting Header
        Text(
            text = "Hello, $name!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1A),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Rovio Styled Daily Quiz Card (Mascot illustration card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .clickable { onStartDailyQuiz() },
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFF000000).copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFFEFBF4)),
                    contentAlignment = Alignment.Center
                ) {
                    GiraffeMascot(modifier = Modifier.fillMaxSize())

                    // Badge "5 min"
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "5 min",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1A)
                        )
                    }

                    // Circle Play button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .clickable { onStartDailyQuiz() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Quiz",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Daily Quiz",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Your daily vocabulary challenge is waiting!",
                    fontSize = 14.sp,
                    color = Color(0xFF7C776E)
                )
            }
        }

        // Active Statistics Track
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStartDailyNew() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFF000000).copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily New",
                        fontSize = 12.sp,
                        color = Color(0xFF7C776E),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$wordsGoal words",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1A)
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFF000000).copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Study Streak",
                        fontSize = 12.sp,
                        color = Color(0xFF7C776E),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\uD83D\uDD25${stats.streak} Days",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1A)
                    )
                }
            }
        }

        // Return browse decks
        Button(
            onClick = onNavigateToDecks,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text("Browse Study Decks", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MinLishTheme {
        // Mock data for preview could be added here
    }
}

@Composable
fun RovioWeeklyTracker(streakCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFF000000).copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Progress",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1A)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star Progress",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7C776E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val daysList = remember(streakCount) {
                    val list = mutableListOf<Triple<String, String, Boolean>>()
                    val sdfNum = SimpleDateFormat("dd", Locale.US)
                    val sdfName = SimpleDateFormat("E", Locale.US)

                    for (i in 3 downTo 0) {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, -i)

                        // Nếu số ngày i nằm trong phạm vi chuỗi ngày streak thật, tự thắp sáng chấm tròn vàng
                        val finished = i < streakCount
                        list.add(Triple(sdfNum.format(cal.time), sdfName.format(cal.time), finished))
                    }
                    list
                }

                daysList.forEach { (num, day, finished) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            num,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (finished) Color(0xFF1C1C1A) else Color(0xFF7C776E).copy(alpha = 0.6f)
                        )
                        Text(
                            day,
                            fontSize = 11.sp,
                            color = Color(0xFF7C776E)
                        )
                        
                        // Circle Indicator
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (finished) Color(0xFFFBBF24) else Color.White
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (finished) Color(0xFFFBBF24) else Color(0xFFE0E7FF),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}
