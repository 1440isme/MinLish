package com.minlish.feature.learning.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minlish.core.component.GiraffeMascot
import com.minlish.core.data.model.VocabularyEntity
import com.minlish.core.data.model.VocabularyWithReviewCard
import com.minlish.core.presentation.MinLishViewModel

private val WarmBackground = Color(0xFFF9F6EE)
private val CardBorder = Color(0x14000000)
private val TextPrimary = Color(0xFF1C1C1A)
private val TextSecondary = Color(0xFF7C776E)
private val HeroSoft = Color(0xFFFEFBF4)
private val AccentGold = Color(0xFFFBBF24)
private val AccentOrange = Color(0xFFFF9F43)
private val AccentGreen = Color(0xFF10B981)
private val AccentBlue = Color(0xFF3B82F6)
private val AccentRed = Color(0xFFEF4444)

private data class RatingUi(
    val label: String,
    val description: String,
    val borderColor: Color,
    val fillColor: Color,
)

@Composable
fun StudyFlashcardsScreen(
    viewModel: MinLishViewModel,
    onFinish: () -> Unit
) {
    val cards by viewModel.activeFlashcards.collectAsState()
    val index by viewModel.currentCardIndex.collectAsState()
    val isFlipped by viewModel.isCardFlipped.collectAsState()

    if (cards.isEmpty()) {
        StudyCompletedState(onFinish = onFinish)
        return
    }

    val currentCard = cards.getOrNull(index) ?: return
    val progress = ((index + 1).coerceAtMost(cards.size)).toFloat() / cards.size.toFloat()
    val reviewedCount = index

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        StudyTopBar(
            current = index + 1,
            total = cards.size,
            onFinish = onFinish,
        )

        Spacer(modifier = Modifier.height(16.dp))

        StudyHeroCard(
            reviewedCount = reviewedCount,
            totalCount = cards.size,
            progress = progress,
        )

        Spacer(modifier = Modifier.height(18.dp))

        FlashcardSurface(
            card = currentCard,
            isFlipped = isFlipped,
            onFlip = { viewModel.flipCard() },
            onSpeak = { viewModel.speak(currentCard.vocabulary.word) },
        )

        Spacer(modifier = Modifier.height(18.dp))

        AnimatedVisibility(
            visible = isFlipped,
            enter = slideInVertically(initialOffsetY = { it / 3 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 3 }) + fadeOut(),
        ) {
            RatingPanel(
                onRate = { rating ->
                    viewModel.submitReviewRating(currentCard.vocabulary.id, rating)
                }
            )
        }

        if (!isFlipped) {
            Surface(
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Chạm vào thẻ để lật và chấm mức độ nhớ.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onFinish,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(
                text = "Dừng phiên học",
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StudyTopBar(
    current: Int,
    total: Int,
    onFinish: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onFinish,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close study",
                tint = TextPrimary,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black)
                .padding(horizontal = 14.dp, vertical = 9.dp),
        ) {
            Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Thẻ $current / $total",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun StudyHeroCard(
    reviewedCount: Int,
    totalCount: Int,
    progress: Float,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Flashcard Review",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ôn tập nhịp ngắn, chấm nhanh, giữ streak đều mỗi ngày.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(HeroSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    GiraffeMascot(modifier = Modifier.size(60.dp))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50.dp)),
                color = AccentGold,
                trackColor = Color(0xFFECE7DE),
            )

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StudyMetricPill(
                    title = "Đã đi qua",
                    value = "$reviewedCount/$totalCount",
                    tint = AccentOrange,
                )
                StudyMetricPill(
                    title = "Tiến độ",
                    value = "${(progress * 100).toInt()}%",
                    tint = AccentGold,
                )
            }
        }
    }
}

@Composable
private fun StudyMetricPill(
    title: String,
    value: String,
    tint: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(tint.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(tint)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$title: $value",
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun FlashcardSurface(
    card: VocabularyWithReviewCard,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSpeak: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onFlip)
            .testTag("flashcard_container"),
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFFFFCF7))
                    )
                )
                .padding(22.dp)
        ) {
            FlashcardMetaRow(card = card)

            Spacer(modifier = Modifier.height(18.dp))

            AnimatedContent(
                targetState = isFlipped,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "flashcard-face",
            ) { flipped ->
                if (!flipped) {
                    FlashcardFront(
                        vocabulary = card.vocabulary,
                        onSpeak = onSpeak,
                    )
                } else {
                    FlashcardBack(vocabulary = card.vocabulary)
                }
            }
        }
    }
}

@Composable
private fun FlashcardMetaRow(card: VocabularyWithReviewCard) {
    val reviewCard = card.reviewCard
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FlashcardChip(
                text = if (reviewCard == null) "Từ mới" else "Đến hạn ôn",
                background = if (reviewCard == null) AccentOrange.copy(alpha = 0.15f) else AccentBlue.copy(alpha = 0.14f),
                content = if (reviewCard == null) AccentOrange else AccentBlue,
            )

            if ((card.vocabulary.relatedWords).isNotBlank()) {
                FlashcardChip(
                    text = "Có liên hệ",
                    background = Color(0xFFE8E3F5),
                    content = TextPrimary,
                )
            }
        }

        if (reviewCard != null) {
            Text(
                text = "EF ${String.format("%.2f", reviewCard.easeFactor)}",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun FlashcardChip(
    text: String,
    background: Color,
    content: Color,
) {
    Text(
        text = text,
        color = content,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun FlashcardFront(
    vocabulary: VocabularyEntity,
    onSpeak: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(370.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = vocabulary.word,
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )

        if (vocabulary.pronunciation.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = vocabulary.pronunciation,
                color = TextSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedButton(
            onClick = onSpeak,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, CardBorder),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = HeroSoft),
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Play pronunciation",
                tint = TextPrimary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Nghe phát âm",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Chạm để xem nghĩa và ví dụ",
            color = TextSecondary,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun FlashcardBack(vocabulary: VocabularyEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(370.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = vocabulary.word,
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = vocabulary.meaning,
            color = AccentGreen,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 30.sp,
        )

        if (vocabulary.descriptionEn.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = vocabulary.descriptionEn,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
            )
        }

        if (vocabulary.example.isNotBlank()) {
            Spacer(modifier = Modifier.height(18.dp))
            StudyInfoCard(
                title = "Ví dụ",
                accent = AccentBlue,
                body = vocabulary.example,
            )
        }

        if (vocabulary.collocation.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            StudyInfoCard(
                title = "Collocations",
                accent = AccentGold,
                body = vocabulary.collocation.split(";").joinToString(", "),
            )
        }

        if (vocabulary.relatedWords.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            StudyInfoCard(
                title = "Related words",
                accent = AccentOrange,
                body = vocabulary.relatedWords,
            )
        }

        if (vocabulary.note.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            StudyInfoCard(
                title = "Ghi nhớ nhanh",
                accent = AccentRed,
                body = vocabulary.note,
            )
        }
    }
}

@Composable
private fun StudyInfoCard(
    title: String,
    accent: Color,
    body: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.10f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.20f)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                color = TextPrimary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun RatingPanel(
    onRate: (String) -> Unit,
) {
    val ratings = remember {
        listOf(
            RatingUi("AGAIN", "Quên hẳn", AccentRed, AccentRed.copy(alpha = 0.12f)),
            RatingUi("HARD", "Khá khó", AccentOrange, AccentOrange.copy(alpha = 0.14f)),
            RatingUi("GOOD", "Nhớ được", AccentGreen, AccentGreen.copy(alpha = 0.12f)),
            RatingUi("EASY", "Rất chắc", AccentBlue, AccentBlue.copy(alpha = 0.12f)),
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Mức độ bạn nhớ từ này thế nào?",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Chọn một mức để hệ thống tính lịch ôn tiếp theo.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF0EBE2))
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ratings.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowItems.forEach { rating ->
                            RatingChoiceButton(
                                rating = rating,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("rating_${rating.label.lowercase()}_btn"),
                                onClick = { onRate(rating.label) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingChoiceButton(
    rating: RatingUi,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = rating.fillColor),
        border = BorderStroke(1.5.dp, rating.borderColor.copy(alpha = 0.45f)),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = rating.label,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = rating.description,
                color = TextSecondary,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun StudyCompletedState(
    onFinish: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(36.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CardBorder),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(HeroSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    GiraffeMascot(modifier = Modifier.size(120.dp))
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(14.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AccentGreen),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Hoàn thành phiên học",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Bạn đã ôn xong các thẻ đang hoạt động hôm nay. Tiếp tục đều đặn để giữ nhịp học thật nhẹ mà bền.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StudyMetricPill(
                        title = "Hôm nay",
                        value = "Done",
                        tint = AccentGreen,
                    )
                    StudyMetricPill(
                        title = "Nhịp học",
                        value = "Ổn định",
                        tint = AccentGold,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        tint = Color.White,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quay về Dashboard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
