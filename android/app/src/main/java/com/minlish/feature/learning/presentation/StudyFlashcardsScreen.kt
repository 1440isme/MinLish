package com.minlish.feature.learning.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
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
private val ButtonSurface = Color(0xFFFFF8F0)

private data class RatingUi(
    val label: String,
    val borderColor: Color,
    val fillColor: Color,
)

@Composable
fun StudyFlashcardsScreen(
    viewModel: MinLishViewModel,
    onFinish: () -> Unit,
) {
    val cards by viewModel.activeFlashcards.collectAsState()
    val index by viewModel.currentCardIndex.collectAsState()
    val isFlipped by viewModel.isCardFlipped.collectAsState()
    val isReplayMode by viewModel.isStudyReplayMode.collectAsState()
    val canReplay by viewModel.canReplayStudySession.collectAsState()

    if (cards.isEmpty()) {
        StudyCompletedState(
            onFinish = onFinish,
            onReplay = { viewModel.replayLastStudySession() },
            canReplay = canReplay,
        )
        return
    }

    val currentCard = cards.getOrNull(index) ?: return
    val progress = ((index + 1).coerceAtMost(cards.size)).toFloat() / cards.size.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        StudyTopBar(onFinish = onFinish)

        Spacer(modifier = Modifier.height(10.dp))

        StudyHeroCard(
            reviewedCount = index,
            totalCount = cards.size,
            progress = progress,
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlashcardSurface(
            card = currentCard,
            isFlipped = isFlipped,
            onFlip = { viewModel.flipCard() },
            onSpeak = { viewModel.speak(currentCard.vocabulary.word) },
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (isReplayMode) {
            ReplayPanel(
                isLastCard = index == cards.lastIndex,
                onNext = { viewModel.goToNextReplayCard() },
            )
        } else {
            RatingPanel(
                onRate = { rating ->
                    viewModel.submitReviewRating(currentCard.vocabulary.id, rating)
                },
            )
        }

    }
}

@Composable
private fun StudyTopBar(
    onFinish: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onFinish,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close study",
                tint = TextPrimary,
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
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ôn tập nhịp ngắn, chấm nhanh, giữ streak đều mỗi ngày.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(HeroSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    GiraffeMascot(modifier = Modifier.size(44.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50.dp)),
                color = AccentGold,
                trackColor = Color(0xFFECE7DE),
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StudyMetricPill(
                    title = "Đang ở thẻ",
                    value = "${(reviewedCount + 1).coerceAtMost(totalCount)}/$totalCount",
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
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(tint),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$title: $value",
            color = TextPrimary,
            fontSize = 11.sp,
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
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "flashcard-rotation",
    )
    val density = LocalDensity.current
    val cardShape = RoundedCornerShape(36.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable(onClick = onFlip)
            .testTag("flashcard_container"),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFFFFCF7)),
                    ),
                )
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            FlashcardMetaRow(card = card)

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 28f * density.density
                    },
            ) {
                if (rotation <= 90f) {
                    FlashcardFront(
                        vocabulary = card.vocabulary,
                        onSpeak = onSpeak,
                    )
                } else {
                    Box(
                        modifier = Modifier.graphicsLayer {
                            rotationY = 180f
                        },
                    ) {
                        FlashcardBack(vocabulary = card.vocabulary)
                    }
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
            .fillMaxSize(),
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
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = vocabulary.pronunciation,
                color = TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Chạm để xem nghĩa",
            color = TextSecondary,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun FlashcardBack(vocabulary: VocabularyEntity) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = vocabulary.word,
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = vocabulary.meaning,
                    style = MaterialTheme.typography.displayLarge,
                    color = AccentGreen,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                if (vocabulary.descriptionEn.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = vocabulary.descriptionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        maxLines = 3,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        if (vocabulary.example.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            StudyInfoCard(
                title = "Ví dụ",
                accent = AccentBlue,
                body = vocabulary.example,
                maxLines = 3,
            )
        }

        if (vocabulary.note.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            StudyInfoCard(
                title = "Ghi nhớ",
                accent = AccentRed,
                body = vocabulary.note,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun StudyInfoCard(
    title: String,
    accent: Color,
    body: String,
    maxLines: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.10f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.20f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = body,
                color = TextPrimary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = maxLines,
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
            RatingUi("AGAIN", AccentRed, ButtonSurface),
            RatingUi("HARD", AccentOrange, ButtonSurface),
            RatingUi("GOOD", AccentGreen, ButtonSurface),
            RatingUi("EASY", AccentBlue, ButtonSurface),
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ratings.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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

@Composable
private fun RatingChoiceButton(
    rating: RatingUi,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = rating.fillColor),
        border = BorderStroke(1.dp, rating.borderColor.copy(alpha = 0.24f)),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 5.dp,
            pressedElevation = 2.dp,
        ),
    ) {
        Text(
            text = rating.label,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun ReplayPanel(
    isLastCard: Boolean,
    onNext: () -> Unit,
) {
    Button(
        onClick = onNext,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
        shape = RoundedCornerShape(24.dp),
    ) {
        Icon(
            imageVector = if (isLastCard) Icons.Default.CheckCircle else Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Color.White,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isLastCard) "Kết thúc lượt xem lại" else "Xem thẻ tiếp theo",
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StudyCompletedState(
    onFinish: () -> Unit,
    onReplay: () -> Unit,
    canReplay: Boolean,
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

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (canReplay) {
                        OutlinedButton(
                            onClick = onReplay,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(32.dp),
                            border = BorderStroke(1.dp, CardBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = HeroSoft,
                                contentColor = TextPrimary,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = null,
                                tint = TextPrimary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Học lại phiên này",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Button(
                        onClick = onFinish,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(32.dp),
                    ) {
                        Text(
                            text = "Quay về",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
