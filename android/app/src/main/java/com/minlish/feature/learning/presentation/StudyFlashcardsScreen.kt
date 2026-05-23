package com.minlish.feature.learning.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minlish.core.presentation.MinLishViewModel

@Composable
fun StudyFlashcardsScreen(
    viewModel: MinLishViewModel,
    onFinish: () -> Unit
) {
    val cards by viewModel.activeFlashcards.collectAsState()
    val index by viewModel.currentCardIndex.collectAsState()
    val isFlipped by viewModel.isCardFlipped.collectAsState()

    val accentTeal = Color(0xFF0D9488)

    if (cards.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = accentTeal
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Daily Study Complete!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You have finished reviewing and studying all the assigned active cards for today.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Return to Dashboard", color = Color.White)
                }
            }
        }
        return
    }

    val currentCard = cards.getOrNull(index) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) Color(0xFF0F1E1B) else Color(0xFFF4F9F8))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper Progress Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Assigned Cards", fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Text("Card ${index + 1} / ${cards.size}", fontWeight = FontWeight.Bold, color = accentTeal)
        }

        LinearProgressIndicator(
            progress = { (index + 1).toFloat() / cards.size },
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape),
            color = accentTeal,
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Large animated flashcard clicker
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable { viewModel.flipCard() }
                .testTag("flashcard_container"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!isFlipped) {
                    // FRONT SIDE
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentCard.vocabulary.word,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentCard.vocabulary.pronunciation,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.speak(currentCard.vocabulary.word) }) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Play Pronunciation", tint = accentTeal)
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        Text(
                            text = "Tap on this card to reveal translation",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                } else {
                    // BACK SIDE
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentCard.vocabulary.word,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentCard.vocabulary.meaning,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentTeal,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider()

                        Spacer(modifier = Modifier.height(16.dp))

                        if (currentCard.vocabulary.descriptionEn.isNotEmpty()) {
                            Text(
                                text = currentCard.vocabulary.descriptionEn,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }

                        if (currentCard.vocabulary.example.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Example Sentence", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentTeal)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(currentCard.vocabulary.example, fontSize = 13.sp)
                                }
                            }
                        }

                        if (currentCard.vocabulary.collocation.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Collocations:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(
                                text = currentCard.vocabulary.collocation.split(";").joinToString(", "),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (currentCard.vocabulary.note.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "💡 Memory Note: ${currentCard.vocabulary.note}",
                                fontSize = 12.sp,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rating triggers block
        AnimatedVisibility(
            visible = isFlipped,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Column {
                Text(
                    text = "Rate your memories of this vocabulary:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SRSButton(
                        text = "AGAIN",
                        description = "Forgot",
                        bgColor = Color(0xFFEF4444),
                        onClick = { viewModel.submitReviewRating(currentCard.vocabulary.id, "AGAIN") },
                        modifier = Modifier.weight(1f).testTag("rating_again_btn")
                    )

                    SRSButton(
                        text = "HARD",
                        description = "Struggled",
                        bgColor = Color(0xFFF57C00),
                        onClick = { viewModel.submitReviewRating(currentCard.vocabulary.id, "HARD") },
                        modifier = Modifier.weight(1f).testTag("rating_hard_btn")
                    )

                    SRSButton(
                        text = "GOOD",
                        description = "Remembered",
                        bgColor = Color(0xFF10B981),
                        onClick = { viewModel.submitReviewRating(currentCard.vocabulary.id, "GOOD") },
                        modifier = Modifier.weight(1f).testTag("rating_good_btn")
                    )

                    SRSButton(
                        text = "EASY",
                        description = "Perfect",
                        bgColor = Color(0xFF3B82F6),
                        onClick = { viewModel.submitReviewRating(currentCard.vocabulary.id, "EASY") },
                        modifier = Modifier.weight(1f).testTag("rating_easy_btn")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exit training action
        TextButton(onClick = onFinish) {
            Text("Stop Study & Save Results", color = Color.Gray)
        }
    }
}

@Composable
fun SRSButton(
    text: String,
    description: String,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(52.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = text, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            Text(text = description, fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}
