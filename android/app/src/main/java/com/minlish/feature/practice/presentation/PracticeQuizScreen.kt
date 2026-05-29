package com.minlish.feature.practice.presentation

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.VolumeUp
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
import com.minlish.core.network.dto.PracticeQuestionDto
import com.minlish.core.network.dto.PracticeAnswerDto

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
    val lastSubmitResult by viewModel.lastSubmitResult.collectAsState()
    val finishSummary by viewModel.finishSummary.collectAsState()
    val vocabularies by viewModel.vocabulariesInSelectedDeck.collectAsState()

    var selectedChoice by remember { mutableStateOf("") }
    var clozeAnswer by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Handle auto-advance for correct answer
    LaunchedEffect(lastSubmitResult) {
        val result = lastSubmitResult
        if (result != null && result.isCorrect) {
            kotlinx.coroutines.delay(3000L)
            // Ensure we are still looking at the same result before auto-advancing
            if (viewModel.lastSubmitResult.value == result) {
                viewModel.advanceToNextQuestion()
                selectedChoice = ""
                clozeAnswer = ""
            }
        }
    }

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFFFBBF24))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Preparing practice dataset...", color = Color(0xFF7C776E))
            }
        }
        return
    }

    if (isFinished) {
        // SUMMARY / RESULTS SCREEN
        val summary = finishSummary
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F6EE))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFFF59E0B)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Luyện tập hoàn tất!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1A)
                )

                Spacer(modifier = Modifier.height(16.dp))

                summary?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFF000000).copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Tỉ lệ đúng: ${it.accuracy.toInt()}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (it.accuracy >= 80) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Số câu đúng:", color = Color(0xFF7C776E))
                                Text("${it.correctAnswers} câu", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Số câu sai:", color = Color(0xFF7C776E))
                                Text("${it.wrongAnswers} câu", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Số câu bỏ qua:", color = Color(0xFF7C776E))
                                Text("${it.unanswered} câu", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Thời gian làm:", color = Color(0xFF7C776E))
                                val minutes = it.timeTakenSeconds / 60
                                val seconds = it.timeTakenSeconds % 60
                                Text(String.format("%02d:%02d", minutes, seconds), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } ?: run {
                    CircularProgressIndicator(color = Color(0xFFFBBF24))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Quay lại", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        return
    }

    val currentQuestion = questions.getOrNull(index) ?: return
    val vocab = remember(currentQuestion.vocabularyId) {
        vocabularies.find { it.id == currentQuestion.vocabularyId }
    }

    // Trigger TTS for listening question format
    LaunchedEffect(currentQuestion.index, currentQuestion.questionType) {
        if (currentQuestion.questionType == "LISTENING_WORD" && vocab != null) {
            viewModel.speak(vocab.word)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F6EE))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(24.dp)
    ) {
        // Top bar
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
                Icon(Icons.Default.Close, contentDescription = "Exit", tint = Color(0xFF1C1C1A))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Đúng: $correctCount",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress text & indicator
        Text(
            text = "Câu ${index + 1} / ${questions.size}",
            fontSize = 14.sp,
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

        Spacer(modifier = Modifier.height(16.dp))

        // Mascot
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            contentAlignment = Alignment.Center
        ) {
            BeeMascot(modifier = Modifier.fillMaxSize())
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Question format header
        val headerText = when (currentQuestion.questionType) {
            "WORD_TO_MEANING" -> "Chọn nghĩa đúng của từ sau"
            "MEANING_TO_WORD" -> "Chọn từ tiếng Anh đúng với nghĩa sau"
            "FILL_IN_BLANK" -> "Điền từ còn thiếu vào chỗ trống"
            "LISTENING_WORD" -> "Nghe và chọn nghĩa tiếng Việt đúng"
            else -> "Luyện tập từ vựng"
        }

        Text(
            text = headerText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7C776E),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Question Card Display
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
                if (currentQuestion.questionType == "LISTENING_WORD") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                if (vocab != null) viewModel.speak(vocab.word)
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7))
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Play Audio",
                                modifier = Modifier.size(36.dp),
                                tint = Color(0xFFD97706)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to listen again",
                            fontSize = 12.sp,
                            color = Color(0xFF7C776E)
                        )
                    }
                } else {
                    Text(
                        text = currentQuestion.questionText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1A),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Question choices or input box
        val isSubmitted = lastSubmitResult != null
        val correctAns = lastSubmitResult?.correctAnswer

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (currentQuestion.questionType == "FILL_IN_BLANK") {
                // FILL_IN_BLANK Input box
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = clozeAnswer,
                        onValueChange = { if (!isSubmitted) clozeAnswer = it },
                        placeholder = { Text("Nhập từ tiếng Anh...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_cloze_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(32.dp),
                        enabled = !isSubmitted,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color(0xFFF3F4F6),
                            focusedBorderColor = if (isSubmitted) {
                                if (lastSubmitResult?.isCorrect == true) Color(0xFF10B981) else Color(0xFFEF4444)
                            } else Color(0xFFFBBF24),
                            unfocusedBorderColor = Color(0xFF000000).copy(alpha = 0.08f),
                            disabledBorderColor = if (lastSubmitResult?.isCorrect == true) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isSubmitted) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (lastSubmitResult?.isCorrect == true) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (lastSubmitResult?.isCorrect == true) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (lastSubmitResult?.isCorrect == true) "Chính xác!" else "Chưa chính xác!",
                                    fontWeight = FontWeight.Bold,
                                    color = if (lastSubmitResult?.isCorrect == true) Color(0xFF065F46) else Color(0xFF991B1B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Đáp án đúng: $correctAns",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1C1C1A)
                                )
                                if (vocab != null && vocab.meaning.isNotEmpty()) {
                                    Text(
                                        text = "Nghĩa: ${vocab.meaning}",
                                        fontSize = 13.sp,
                                        color = Color(0xFF7C776E)
                                    )
                                }
                            }
                        }
                    } else {
                        // Translation hint for fill in blank
                        vocab?.let {
                            Text(
                                text = "Gợi ý nghĩa: ${it.meaning}",
                                fontSize = 13.sp,
                                color = Color(0xFF7C776E),
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            } else {
                // MULTIPLE CHOICE / LISTENING OPTIONS
                val options = currentQuestion.options ?: emptyList()
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    options.forEach { choice ->
                        val isSelected = selectedChoice == choice

                        // Style calculations post submission
                        val containerColor = when {
                            isSubmitted && choice == correctAns -> Color(0xFFECFDF5) // Always green for correct answer
                            isSubmitted && isSelected && choice != correctAns -> Color(0xFFFEF2F2) // Red if wrong selection
                            isSelected -> Color(0xFFFEFBF4) // Selected yellow container
                            else -> Color.White
                        }

                        val borderColor = when {
                            isSubmitted && choice == correctAns -> Color(0xFF10B981) // Green border for correct answer
                            isSubmitted && isSelected && choice != correctAns -> Color(0xFFEF4444) // Red border for wrong
                            isSelected -> Color(0xFFFBBF24) // Yellow border for selected
                            else -> Color(0xFF000000).copy(alpha = 0.08f)
                        }

                        val borderWidth = if (isSelected || (isSubmitted && choice == correctAns)) 2.dp else 1.dp

                        OutlinedButton(
                            onClick = { if (!isSubmitted) selectedChoice = choice },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(32.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor),
                            border = BorderStroke(borderWidth, borderColor),
                            enabled = !isSubmitted
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = choice,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1C1C1A),
                                    modifier = Modifier.weight(1f)
                                )

                                if (isSubmitted && choice == correctAns) {
                                    Text("✓", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                } else if (isSubmitted && isSelected && choice != correctAns) {
                                    Text("✗", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        if (isSubmitted) {
            // Show single Continue Button
            Button(
                onClick = {
                    viewModel.advanceToNextQuestion()
                    selectedChoice = ""
                    clozeAnswer = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("quiz_continue_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(32.dp)
            ) {
                val isCorrect = lastSubmitResult?.isCorrect == true
                Text(
                    text = if (isCorrect) "Tiếp tục (Tự chuyển sau 3s)" else "Tiếp tục",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        } else {
            // Show Skip ("Không biết?") & Submit buttons side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.submitPracticeAnswer("") // skips/Don't know
                    },
                    modifier = Modifier
                        .weight(0.35f)
                        .height(56.dp),
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Không biết?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        val ans = if (currentQuestion.questionType == "FILL_IN_BLANK") clozeAnswer else selectedChoice
                        if (ans.trim().isEmpty() && currentQuestion.questionType != "FILL_IN_BLANK") {
                            Toast.makeText(context, "Vui lòng chọn đáp án trước!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.submitPracticeAnswer(ans)
                        }
                    },
                    modifier = Modifier
                        .weight(0.65f)
                        .height(56.dp)
                        .testTag("quiz_submit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Text("NỘP BÀI", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
