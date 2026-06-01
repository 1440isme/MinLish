package com.minlish.feature.practice.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
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
import androidx.compose.ui.res.stringResource
import com.minlish.R
import com.minlish.core.network.dto.PracticeQuestionDto
import com.minlish.core.network.dto.PracticeAnswerDto
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

private data class ChimeNote(
    val freq: Double,
    val startTime: Double,
    val decay: Double,
    val amp: Double
)

private fun playCorrectSound() {
    try {
        val sampleRate = 44100
        val totalDurationSeconds = 1.0
        val totalSamples = (totalDurationSeconds * sampleRate).toInt()
        val soundData = ShortArray(totalSamples)
        
        val notes = listOf(
            ChimeNote(523.25, 0.00, 0.15, 0.18),  // C5
            ChimeNote(659.25, 0.08, 0.15, 0.18),  // E5
            ChimeNote(783.99, 0.16, 0.15, 0.18),  // G5
            ChimeNote(1046.50, 0.24, 0.20, 0.22), // C6
            ChimeNote(1318.51, 0.32, 0.25, 0.25), // E6
            ChimeNote(1567.98, 0.40, 0.40, 0.30)  // G6
        )
        
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            var sampleValue = 0.0
            
            for (note in notes) {
                if (t >= note.startTime) {
                    val noteT = t - note.startTime
                    // Envelope: fast attack (5ms) + exponential decay
                    val attackTime = 0.005
                    val envelope = if (noteT < attackTime) {
                        (noteT / attackTime) * Math.exp(-noteT / note.decay)
                    } else {
                        Math.exp(-noteT / note.decay)
                    }
                    
                    // Rich chime/bell timbre: fundamental + 2nd harmonic (35%) + 3rd harmonic (15%) + 4th harmonic (5%)
                    val wave = Math.sin(2.0 * Math.PI * note.freq * noteT) +
                               0.35 * Math.sin(2.0 * Math.PI * (2.0 * note.freq) * noteT) +
                               0.15 * Math.sin(2.0 * Math.PI * (3.0 * note.freq) * noteT) +
                               0.05 * Math.sin(2.0 * Math.PI * (4.0 * note.freq) * noteT)
                    
                    sampleValue += wave * envelope * note.amp
                }
            }
            
            // Apply master volume gain and clamp to Short range
            val scaled = (sampleValue * Short.MAX_VALUE * 0.32).toInt()
            soundData[i] = scaled.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        
        val audioTrack = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            android.media.AudioTrack(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                android.media.AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                    .build(),
                totalSamples * 2,
                android.media.AudioTrack.MODE_STATIC,
                android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
            )
        } else {
            @Suppress("DEPRECATION")
            android.media.AudioTrack(
                android.media.AudioManager.STREAM_MUSIC,
                sampleRate,
                android.media.AudioFormat.CHANNEL_OUT_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT,
                totalSamples * 2,
                android.media.AudioTrack.MODE_STATIC
            )
        }
        
        audioTrack.write(soundData, 0, totalSamples)
        audioTrack.play()
        
        val totalDurationMs = (totalSamples * 1000) / sampleRate
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }, totalDurationMs + 100L)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private data class TromboneNote(
    val startTime: Double,
    val endTime: Double,
    val startFreq: Double,
    val endFreq: Double,
    val hasVibrato: Boolean = false
)

private fun playIncorrectSound() {
    try {
        val sampleRate = 44100
        val totalDurationSeconds = 1.5
        val totalSamples = (totalDurationSeconds * sampleRate).toInt()
        val soundData = ShortArray(totalSamples)
        
        // Classic "Sad Trombone" Chromatic Descent: Bb4 -> A4 -> Ab4 -> G4 (with slide to Eb4 / C4)
        val notes = listOf(
            TromboneNote(0.00, 0.22, 466.16, 440.00), // Bb4 to A4
            TromboneNote(0.28, 0.50, 440.00, 415.30), // A4 to Ab4
            TromboneNote(0.56, 0.78, 415.30, 392.00), // Ab4 to G4
            TromboneNote(0.84, 1.45, 392.00, 261.63, hasVibrato = true) // G4 sliding way down to C4 with wobbly vibrato
        )
        
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            var sampleValue = 0.0
            
            for (note in notes) {
                if (t >= note.startTime && t <= note.endTime) {
                    val x = t - note.startTime
                    val d = note.endTime - note.startTime
                    
                    // Closed-form exact phase for linear frequency sweep
                    var phase = 2.0 * Math.PI * x * (note.startFreq + 0.5 * (note.endFreq - note.startFreq) * (x / d))
                    
                    // Add shaky sad vibrato to the final note
                    if (note.hasVibrato) {
                        phase += 0.55 * Math.sin(2.0 * Math.PI * 6.5 * x)
                    }
                    
                    // Brassy trombone wave: fundamental + 2nd harmonic (50%) + 3rd harmonic (25%) + 4th harmonic (10%)
                    val wave = Math.sin(phase) + 
                               0.50 * Math.sin(2.0 * phase) + 
                               0.25 * Math.sin(3.0 * phase) + 
                               0.10 * Math.sin(4.0 * phase)
                    
                    // Envelope with fast attack (25ms) and fade out exactly to 0
                    val attackTime = 0.025
                    val envelope = if (x < attackTime) {
                        x / attackTime
                    } else {
                        1.0 - (x - attackTime) / (d - attackTime)
                    }
                    
                    sampleValue = wave * envelope * 0.35
                    break
                }
            }
            
            val scaled = (sampleValue * Short.MAX_VALUE).toInt()
            soundData[i] = scaled.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        
        val audioTrack = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            android.media.AudioTrack(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                android.media.AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                    .build(),
                totalSamples * 2,
                android.media.AudioTrack.MODE_STATIC,
                android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
            )
        } else {
            @Suppress("DEPRECATION")
            android.media.AudioTrack(
                android.media.AudioManager.STREAM_MUSIC,
                sampleRate,
                android.media.AudioFormat.CHANNEL_OUT_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT,
                totalSamples * 2,
                android.media.AudioTrack.MODE_STATIC
            )
        }
        
        audioTrack.write(soundData, 0, totalSamples)
        audioTrack.play()
        
        val totalDurationMs = (totalSamples * 1000) / sampleRate
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }, totalDurationMs + 100L)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

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
    val practiceError by viewModel.practiceError.collectAsState()
    val isSoundEnabled by viewModel.isPracticeSoundEnabled.collectAsState()

    var selectedChoice by remember { mutableStateOf("") }
    var clozeAnswer by remember { mutableStateOf("") }
    var showReviewScreen by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Handle auto-advance for correct answer
    LaunchedEffect(lastSubmitResult) {
        val result = lastSubmitResult
        if (result != null) {
            if (result.isCorrect) {
                if (isSoundEnabled) {
                    playCorrectSound()
                }
                kotlinx.coroutines.delay(2000L) // 2s delay as requested by user
                // Ensure we are still looking at the same result before auto-advancing
                if (viewModel.lastSubmitResult.value == result) {
                    viewModel.advanceToNextQuestion()
                    selectedChoice = ""
                    clozeAnswer = ""
                }
            } else {
                if (isSoundEnabled) {
                    playIncorrectSound()
                }
            }
        }
    }

    if (questions.isEmpty() && !isFinished) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF9F2)),
            contentAlignment = Alignment.Center
        ) {
            if (practiceError != null) {
                // Error state — API failed, show message + back button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "😕",
                        fontSize = 56.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.quiz_unable_to_load),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1C1C1A),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = practiceError ?: "",
                        fontSize = 13.sp,
                        color = Color(0xFF7C776E),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.clearPracticeError()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1A)),
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(stringResource(R.string.common_back), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            } else {
                // Loading state — waiting for API
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFFBBF24))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.quiz_loading_questions), color = Color(0xFF7C776E))
                }
            }
        }
        return
    }

    if (isFinished) {
        if (showReviewScreen) {
            val finishAnswers by viewModel.finishAnswers.collectAsState()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFF9F2))
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.quiz_practice_review),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1A)
                        )
                        
                        IconButton(
                            onClick = { showReviewScreen = false },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFF000000).copy(alpha = 0.05f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_cancel), tint = Color(0xFF1C1C1A))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scrollable list of answers
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(finishAnswers) { answer ->
                            val isCorrect = answer.isCorrect
                            val isSkipped = answer.userAnswer.isNullOrBlank()
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, Color(0xFF000000).copy(alpha = 0.05f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    // Badge row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val badgeColor = when {
                                            isCorrect -> Color(0xFF10B981) // Green
                                            isSkipped -> Color(0xFF9CA3AF) // Gray
                                            else -> Color(0xFFEF4444) // Red
                                        }
                                        val badgeText = when {
                                            isCorrect -> stringResource(R.string.quiz_correct)
                                            isSkipped -> stringResource(R.string.quiz_skipped)
                                            else -> stringResource(R.string.quiz_incorrect)
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(badgeColor.copy(alpha = 0.12f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = badgeText,
                                                color = badgeColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Display question type friendly text
                                        val qTypeText = when (answer.questionType) {
                                            "WORD_TO_MEANING" -> stringResource(R.string.quiz_type_word_to_meaning)
                                            "MEANING_TO_WORD" -> stringResource(R.string.quiz_type_meaning_to_word)
                                            "FILL_IN_BLANK" -> stringResource(R.string.quiz_type_fill_in_blank)
                                            "LISTENING_WORD" -> stringResource(R.string.quiz_type_listening_word)
                                            else -> answer.questionType
                                        }
                                        Text(
                                            text = qTypeText,
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Question text
                                    Text(
                                        text = answer.questionText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1C1C1A)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Answer choices or fill-in-blank styling
                                    if (answer.questionType == "FILL_IN_BLANK") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // User Answer
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        if (isCorrect) Color(0xFFECFDF5)
                                                        else if (isSkipped) Color(0xFFF3F4F6)
                                                        else Color(0xFFFEF2F2)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isCorrect) Color(0xFF10B981)
                                                        else if (isSkipped) Color(0xFFE5E7EB)
                                                        else Color(0xFFEF4444),
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .padding(14.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = stringResource(
                                                            R.string.quiz_your_answer,
                                                            answer.userAnswer ?: stringResource(R.string.quiz_skipped_parentheses)
                                                        ),
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color(0xFF1C1C1A)
                                                    )
                                                    if (isCorrect) {
                                                        Text("✓", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                    } else if (!isSkipped) {
                                                        Text("✗", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                    }
                                                }
                                            }

                                            // Correct Answer if user was wrong/skipped
                                            if (!isCorrect) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(Color(0xFFECFDF5))
                                                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(16.dp))
                                                        .padding(14.dp)
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.quiz_correct_answer, answer.correctAnswer),
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF065F46)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // Multiple Choice options list
                                        val options = answer.optionsJson ?: emptyList()
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            options.forEach { option ->
                                                val isUserSelected = answer.userAnswer == option
                                                val isCorrectOption = answer.correctAnswer == option
                                                
                                                val optBgColor = when {
                                                    isCorrectOption -> Color(0xFFECFDF5) // Green background for correct
                                                    isUserSelected -> Color(0xFFFEF2F2) // Red background for user's wrong selection
                                                    else -> Color.White
                                                }
                                                val optBorderColor = when {
                                                    isCorrectOption -> Color(0xFF10B981) // Green border for correct
                                                    isUserSelected -> Color(0xFFEF4444) // Red border for wrong
                                                    else -> Color(0xFF000000).copy(alpha = 0.08f)
                                                }
                                                val optBorderWidth = if (isCorrectOption || isUserSelected) 2.dp else 1.dp

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(48.dp)
                                                        .clip(RoundedCornerShape(24.dp))
                                                        .background(optBgColor)
                                                        .border(optBorderWidth, optBorderColor, RoundedCornerShape(24.dp))
                                                        .padding(horizontal = 16.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = option,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = Color(0xFF1C1C1A)
                                                        )
                                                        if (isCorrectOption) {
                                                            Text("✓", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                        } else if (isUserSelected) {
                                                            Text("✗", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Done Button
                    Button(
                        onClick = { showReviewScreen = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(stringResource(R.string.common_done), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        } else {
            // SUMMARY / RESULTS SCREEN
            val summary = finishSummary
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFF9F2))
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
                        text = stringResource(R.string.quiz_practice_completed),
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
                                    stringResource(R.string.quiz_accuracy, it.accuracy.toInt()),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (it.accuracy >= 80) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stringResource(R.string.quiz_correct_answers_label), color = Color(0xFF7C776E))
                                    Text("${it.correctAnswers}", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stringResource(R.string.quiz_wrong_answers_label), color = Color(0xFF7C776E))
                                    Text("${it.wrongAnswers}", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stringResource(R.string.quiz_skipped_answers_label), color = Color(0xFF7C776E))
                                    Text("${it.unanswered}", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stringResource(R.string.quiz_time_taken_label), color = Color(0xFF7C776E))
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
                        onClick = { showReviewScreen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(stringResource(R.string.quiz_btn_review_answers), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(stringResource(R.string.common_back), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
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
            .background(Color(0xFFFFF9F2))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(24.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFF000000).copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_back), tint = Color(0xFF1C1C1A))
                }

                IconButton(
                    onClick = {
                        viewModel.isPracticeSoundEnabled.value = !isSoundEnabled
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFF000000).copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Toggle Sound",
                        tint = if (isSoundEnabled) Color(0xFF0D9488) else Color.Gray
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${stringResource(R.string.quiz_correct)}: $correctCount",
                        color = Color(0xFF1C1C1A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        viewModel.finishCurrentSession()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = stringResource(R.string.common_submit),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress text & indicator
        Text(
            text = stringResource(R.string.quiz_question_number, index + 1, questions.size),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF7C776E),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = { (index + 1).toFloat() / questions.size },
            modifier = Modifier
                .width(80.dp)
                .height(4.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape),
            color = Color(0xFFFBBF24),
            trackColor = Color(0xFFE2E8F0)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mascot
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp),
            contentAlignment = Alignment.Center
        ) {
            BeeMascot(modifier = Modifier.fillMaxHeight())
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.deck_detail_practice),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1A),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Play Audio",
                                modifier = Modifier.size(36.dp),
                                tint = Color(0xFFD97706)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.quiz_tap_to_listen),
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

        Spacer(modifier = Modifier.height(16.dp))

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
                        placeholder = { Text(stringResource(R.string.quiz_enter_word_placeholder)) },
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
                                    text = if (lastSubmitResult?.isCorrect == true) {
                                        stringResource(R.string.quiz_cloze_correct)
                                    } else {
                                        stringResource(R.string.quiz_cloze_incorrect)
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = if (lastSubmitResult?.isCorrect == true) Color(0xFF065F46) else Color(0xFF991B1B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.quiz_cloze_correct_answer_label, correctAns ?: ""),
                                    fontSize = 14.sp,
                                    color = Color(0xFF1C1C1A)
                                )
                                if (vocab != null && vocab.meaning.isNotEmpty()) {
                                    Text(
                                        text = stringResource(R.string.quiz_meaning_label, vocab.meaning),
                                        fontSize = 13.sp,
                                        color = Color(0xFF7C776E)
                                    )
                                }
                            }
                        }
                    } else {
                        var showHint by remember(currentQuestion.index) { mutableStateOf(false) }
                        if (showHint) {
                            vocab?.let {
                                Text(
                                    text = stringResource(R.string.quiz_hint_label, it.meaning),
                                    fontSize = 13.sp,
                                    color = Color(0xFF7C776E),
                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                                )
                            }
                        } else {
                            TextButton(
                                onClick = { showHint = true },
                                modifier = Modifier.padding(start = 16.dp)
                            ) {
                                Text(stringResource(R.string.quiz_show_hint), color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
                            }
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
                            onClick = {
                                if (!isSubmitted) {
                                    selectedChoice = choice
                                    viewModel.submitPracticeAnswer(choice)
                                }
                            },
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
            // Show single Continue Button (always "Continue" now, since final submission is handled on correct auto-advance or via top Submit button)
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
                Text(
                    text = stringResource(R.string.dashboard_continue),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        } else {
            if (currentQuestion.questionType == "FILL_IN_BLANK") {
                // Show Skip ("Don't know?") & Answer buttons side by side for fill-in-blank
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
                        Text(stringResource(R.string.quiz_btn_dont_know), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.submitPracticeAnswer(clozeAnswer)
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
                        Text(stringResource(R.string.quiz_btn_answer), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            } else {
                // Show only full-width Skip ("Don't know?") button for MCQ / Listening (since tapping options submits immediately)
                OutlinedButton(
                    onClick = {
                        viewModel.submitPracticeAnswer("") // skips/Don't know
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text(stringResource(R.string.quiz_btn_dont_know), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
