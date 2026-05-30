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

private fun playIncorrectSound() {
    try {
        val sampleRate = 44100
        val noteLen = (250 * sampleRate) / 1000  // 250ms for each "womp"
        val gapLen = (50 * sampleRate) / 1000   // 50ms silence gap between notes
        val lastNoteLen = (550 * sampleRate) / 1000 // 550ms for the final "wooooomp"
        
        val totalSamples = (3 * (noteLen + gapLen)) + lastNoteLen
        val soundData = ShortArray(totalSamples)
        
        var phase = 0.0
        
        for (i in 0 until totalSamples) {
            // Determine note segment
            val noteIndex: Int
            val sampleInNote: Int
            val currentNoteLen: Int
            val startFreq: Double
            val endFreq: Double
            
            if (i < noteLen) {
                noteIndex = 0
                sampleInNote = i
                currentNoteLen = noteLen
                startFreq = 290.0
                endFreq = 260.0
            } else if (i < noteLen + gapLen) {
                noteIndex = -1 // gap
                sampleInNote = 0
                currentNoteLen = gapLen
                startFreq = 0.0
                endFreq = 0.0
            } else if (i < 2 * noteLen + gapLen) {
                noteIndex = 1
                sampleInNote = i - (noteLen + gapLen)
                currentNoteLen = noteLen
                startFreq = 270.0
                endFreq = 240.0
            } else if (i < 2 * (noteLen + gapLen)) {
                noteIndex = -1 // gap
                sampleInNote = 0
                currentNoteLen = gapLen
                startFreq = 0.0
                endFreq = 0.0
            } else if (i < 3 * noteLen + 2 * gapLen) {
                noteIndex = 2
                sampleInNote = i - 2 * (noteLen + gapLen)
                currentNoteLen = noteLen
                startFreq = 250.0
                endFreq = 220.0
            } else if (i < 3 * (noteLen + gapLen)) {
                noteIndex = -1 // gap
                sampleInNote = 0
                currentNoteLen = gapLen
                startFreq = 0.0
                endFreq = 0.0
            } else {
                noteIndex = 3
                sampleInNote = i - 3 * (noteLen + gapLen)
                currentNoteLen = lastNoteLen
                startFreq = 220.0
                endFreq = 160.0
            }
            
            if (noteIndex == -1) {
                soundData[i] = 0
                phase = 0.0 // reset phase for clean attack on next note
            } else {
                val progress = sampleInNote.toDouble() / currentNoteLen
                // Slide frequency down slightly inside each note for the "wah" trombone filter simulation
                val freq = startFreq + (endFreq - startFreq) * progress
                
                // Synthesize trombone brassy texture by combining fundamental with 2nd and 3rd harmonics
                val wave = Math.sin(phase) + 0.35 * Math.sin(2.0 * phase) + 0.15 * Math.sin(3.0 * phase)
                
                // Volume envelope (attack + decay) for wind instrument tonguing
                val attackSamples = (30 * sampleRate) / 1000 // 30ms attack
                val envelope = if (sampleInNote < attackSamples) {
                    sampleInNote.toDouble() / attackSamples
                } else {
                    1.0 - 0.45 * (sampleInNote - attackSamples).toDouble() / (currentNoteLen - attackSamples)
                }
                
                soundData[i] = (wave * Short.MAX_VALUE * 0.38 * envelope).toInt().toShort()
                
                phase += 2.0 * Math.PI * freq / sampleRate
                if (phase > 2.0 * Math.PI) {
                    phase -= 2.0 * Math.PI
                }
            }
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
        }, totalDurationMs + 150L)
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

    var selectedChoice by remember { mutableStateOf("") }
    var clozeAnswer by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Handle auto-advance for correct answer
    LaunchedEffect(lastSubmitResult) {
        val result = lastSubmitResult
        if (result != null) {
            if (result.isCorrect) {
                playCorrectSound()
                kotlinx.coroutines.delay(2000L) // 2s delay as requested by user
                // Ensure we are still looking at the same result before auto-advancing
                if (viewModel.lastSubmitResult.value == result) {
                    viewModel.advanceToNextQuestion()
                    selectedChoice = ""
                    clozeAnswer = ""
                }
            } else {
                playIncorrectSound()
            }
        }
    }

    if (questions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F6EE)),
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
                        text = "Unable to load practice session",
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
                        Text("Back", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            } else {
                // Loading state — waiting for API
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFFBBF24))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading questions...", color = Color(0xFF7C776E))
                }
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
                    text = "Practice completed!",
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
                                "Accuracy: ${it.accuracy.toInt()}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (it.accuracy >= 80) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Correct answers:", color = Color(0xFF7C776E))
                                Text("${it.correctAnswers}", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Wrong answers:", color = Color(0xFF7C776E))
                                Text("${it.wrongAnswers}", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Skipped answers:", color = Color(0xFF7C776E))
                                Text("${it.unanswered}", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Time taken:", color = Color(0xFF7C776E))
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
                    Text("Back", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        text = "Correct: $correctCount",
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
                        text = "Submit",
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
            text = "Question ${index + 1} / ${questions.size}",
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

        Text(
            text = "Practice",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1C1A),
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
                        placeholder = { Text("Enter English word...") },
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
                                    text = if (lastSubmitResult?.isCorrect == true) "Correct!" else "Incorrect!",
                                    fontWeight = FontWeight.Bold,
                                    color = if (lastSubmitResult?.isCorrect == true) Color(0xFF065F46) else Color(0xFF991B1B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Correct answer: $correctAns",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1C1C1A)
                                )
                                if (vocab != null && vocab.meaning.isNotEmpty()) {
                                    Text(
                                        text = "Meaning: ${vocab.meaning}",
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
                                    text = "Hint: ${it.meaning}",
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
                                Text("Show Hint", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
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
                    text = "Continue",
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
                        Text("Don't know?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                        Text("Answer", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                    Text("Don't know?", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
