package com.minlish.feature.practice.presentation

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minlish.core.component.BeeMascot
import com.minlish.core.presentation.MinLishViewModel

@Composable
fun PracticeQuizScreen(
    deckId: String,
    practiceType: String,
    viewModel: MinLishViewModel,
    onBack: () -> Unit
) {
    val questions by viewModel.quizQuestions.collectAsState()
    val index by viewModel.currentQuizIndex.collectAsState()
    val correctCount by viewModel.quizCorrectCount.collectAsState()
    val isFinished by viewModel.quizFinished.collectAsState()

    var selectedChoice by remember { mutableStateOf("") }
    var clozeAnswer by remember { mutableStateOf("") }
    
    // Ticking stopwatch
    var elapsedSeconds by remember { mutableIntStateOf(297) } // 04:57
    LaunchedEffect(key1 = isFinished) {
        while (!isFinished) {
            kotlinx.coroutines.delay(1000L)
            elapsedSeconds--
            if (elapsedSeconds <= 0) break
        }
    }

    val context = LocalContext.current

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Preparing practice dataset...")
            }
        }
        return
    }

    if (isFinished) {
        // QUIZ SUCCESS SCREEN
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F6EE))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = Color(0xFFF59E0B)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Practice Completed!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1A)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val accuracy = (correctCount.toFloat() / questions.size * 100f).toInt()

                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.dp, Color(0xFF000000).copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Accuracy: $accuracy%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Correct Answers: $correctCount / ${questions.size}",
                            fontSize = 14.sp,
                            color = Color(0xFF7C776E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text("Return", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val currentQuestion = questions.getOrNull(index) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F6EE))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(24.dp)
    ) {
        // Rovio top bar with Timer Capsule & Close Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFF000000).copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Exit Quiz", tint = Color(0xFF1C1C1A))
            }
            
            // Time left Black Capsule
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Time left: ${formatSeconds(elapsedSeconds)}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Choose correct answer subheading
        Text(
            text = "Choose the correct answer",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1A),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Bee Mascot
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            BeeMascot(modifier = Modifier.fillMaxSize())
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress indicators
        Text(
            text = "Question ${index + 1} of ${questions.size}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF7C776E),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { (index + 1).toFloat() / questions.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = Color(0xFFFBBF24),
            trackColor = Color(0xFFE2E8F0)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Question Card Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF000000).copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentQuestion.questionText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1A),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Choices / Inputs in Rovio capsule look
        if (practiceType == "MULTIPLE_CHOICE") {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                currentQuestion.choices.forEach { choice ->
                    val isSelected = selectedChoice == choice

                    OutlinedButton(
                        onClick = { selectedChoice = choice },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) Color(0xFFFEFBF4) else Color.White
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFFFBBF24) else Color(0xFF000000).copy(alpha = 0.08f)
                        )
                    ) {
                        Text(
                            text = choice,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1C1C1A),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // Fill in the blank
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = clozeAnswer,
                    onValueChange = { clozeAnswer = it },
                    placeholder = { Text("Type English word here...") },
                    modifier = Modifier.fillMaxWidth().testTag("quiz_cloze_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(32.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF000000).copy(alpha = 0.2f),
                        unfocusedBorderColor = Color(0xFF000000).copy(alpha = 0.08f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Hint translation: ${currentQuestion.vocabulary.meaning}",
                    fontSize = 13.sp,
                    color = Color(0xFF7C776E),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        // Action Submit Pill Button
        Button(
            onClick = {
                val ans = if (practiceType == "MULTIPLE_CHOICE") selectedChoice else clozeAnswer
                if (ans.trim().isEmpty()) {
                    Toast.makeText(context, "Please configure your answer first!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.submitQuizAnswer(deckId, practiceType, ans)
                    selectedChoice = ""
                    clozeAnswer = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("quiz_submit_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(32.dp)
        ) {
            Text("SUBMIT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

fun formatSeconds(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
