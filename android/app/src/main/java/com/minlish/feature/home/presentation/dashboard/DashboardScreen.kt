package com.minlish.feature.home.presentation.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
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
import com.minlish.core.data.model.RecentStudyDeckEntity
import com.minlish.core.component.GiraffeMascot
import com.minlish.ui.theme.MinLishTheme
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import com.minlish.R

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onStartDailyQuiz: () -> Unit,
    onStartDailyNew: () -> Unit,
    onNavigateToDecks: () -> Unit,
    onResumeRecentDeck: (String) -> Unit,
    onOpenRecentDeck: (String) -> Unit,
) {
    val name by viewModel.fullName.collectAsState()
    val stats by viewModel.dashboardAnalytics.collectAsState()
    val wordsGoal by viewModel.dailyNewWordsGoal.collectAsState()
    val recentStudyDeck by viewModel.recentStudyDeck.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchDashboardAnalytics()
        viewModel.refreshRecentStudyDeck()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9F2))
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 100.dp)
    ) {
        // Rovio Streak Weekly tracker
        //Thêm mảng 7 ngày vào tracker
        RovioWeeklyTracker(
            streakCount = stats.streak,
            weeklyActiveDays = stats.weeklyActiveDays
        )

        // Greeting Header
        Text(
            text = stringResource(R.string.dashboard_greeting, name),
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
                            contentDescription = stringResource(R.string.dashboard_daily_quiz),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.dashboard_daily_quiz),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.dashboard_daily_quiz_desc),
                    fontSize = 14.sp,
                    color = Color(0xFF7C776E)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(168.dp)
                    .clickable { onStartDailyNew() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFF000000).copy(alpha = 0.05f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_daily_new),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1A),
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFEFBF4))
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.profile_words_count, wordsGoal),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1A)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(62.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFE7F8F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FiberNew,
                                contentDescription = null,
                                tint = Color(0xFF0D9488),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(168.dp)
                    .clickable { onNavigateToDecks() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFF000000).copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_deck_learning),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1A),
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.dashboard_learn_with_decks),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF7C776E),
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFFFF3DE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = Color(0xFFB7791F),
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
        }

        recentStudyDeck?.let { recentDeck ->
            RecentDeckCard(
                recentDeck = recentDeck,
                onResume = { onResumeRecentDeck(recentDeck.deck.id) },
                onOpenDeck = { onOpenRecentDeck(recentDeck.deck.id) },
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RecentDeckCard(
    recentDeck: RecentStudyDeckEntity,
    onResume: () -> Unit,
    onOpenDeck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalWords = recentDeck.deck.totalWords
    val learnedWords = (totalWords - recentDeck.newWordsAvailable).coerceIn(0, totalWords)
    val isCompleted = recentDeck.dueReviewCount == 0 && recentDeck.newWordsAvailable == 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isCompleted) {
                    Modifier.clickable(onClick = onOpenDeck)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFF000000).copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.dashboard_recent_deck),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = recentDeck.deck.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1C1A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.dashboard_recent_deck_desc, recentDeck.dueReviewCount, learnedWords, totalWords),
                        fontSize = 13.sp,
                        color = Color(0xFF7C776E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = if (isCompleted) onOpenDeck else onResume,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D9488),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = if (isCompleted) stringResource(R.string.dashboard_no_cards_due) else stringResource(R.string.dashboard_continue),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MinLishTheme {
        // Mock data for preview
    }
}

@Composable
fun RovioWeeklyTracker(streakCount: Int, weeklyActiveDays: List<Boolean>) {
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
                    text = stringResource(R.string.dashboard_weekly_progress),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1A)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.dashboard_streak_days, streakCount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7C776E)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = stringResource(R.string.dashboard_streak_label),
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Danh sách 7 ngày hiển thị chữ cái đầu
                val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")


                daysOfWeek.forEachIndexed { index, dayLabel ->
                    // Tránh lỗi Index nếu danh sách mảng chưa kịp update
                    val isActive = weeklyActiveDays.getOrElse(index) { false }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = dayLabel,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isActive) Color(0xFF1C1C1A) else Color(0xFF7C776E).copy(alpha = 0.6f)
                        )
                        
                        // Circle Indicator
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) Color(0xFFFBBF24) else Color.White
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isActive) Color(0xFFFBBF24) else Color(0xFFE0E7FF),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isActive) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
