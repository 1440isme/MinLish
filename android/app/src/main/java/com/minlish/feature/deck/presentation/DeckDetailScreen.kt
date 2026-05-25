package com.minlish.feature.deck.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.minlish.core.data.model.VocabularyEntity
import com.minlish.core.presentation.MinLishViewModel

@Composable
fun DeckDetailScreen(
    deckId: String,
    viewModel: MinLishViewModel,
    onBack: () -> Unit,
    onStartQuiz: (String) -> Unit
) {
    val deck by viewModel.selectedDeck.collectAsState()
    val vocabs by viewModel.vocabulariesInSelectedDeck.collectAsState()

    var showWordDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val accentTeal = Color(0xFF0D9488)

    if (deck == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) Color(0xFF0F1E1B) else Color(0xFFF4F9F8))
            .padding(16.dp)
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = deck!!.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (deck!!.deckType == "USER") {
                IconButton(onClick = {
                    viewModel.deleteCustomDeck(deckId)
                    onBack()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Deck", tint = Color.Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = deck!!.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Practice Triggers Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onStartQuiz("MULTIPLE_CHOICE") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Multi-Choice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = { onStartQuiz("FILL_IN_BLANK") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cloze / Fill", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid Action Word / CSV Row
        if (deck!!.deckType == "USER") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showWordDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Manual Word", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                OutlinedButton(
                    onClick = { showImportDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // List of Vocabs in selected deck
        Text(
            text = "Word List (${vocabs.size} words)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(vocabs) { vocab ->
                VocabItemCard(
                    vocab = vocab,
                    canDelete = deck!!.deckType == "USER",
                    onDelete = { viewModel.deleteCustomVocabulary(vocab.id, deckId) },
                    onSpeak = { viewModel.speak(vocab.word) }
                )
            }

            if (vocabs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This deck is empty! Please click 'Add Word' or 'Import CSV' to populate English terms.",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // MANUAL WORD DIALOG MODAL
    if (showWordDialog) {
        var nWord by remember { mutableStateOf("") }
        var nPron by remember { mutableStateOf("") }
        var nMeaning by remember { mutableStateOf("") }
        var nDesc by remember { mutableStateOf("") }
        var nExample by remember { mutableStateOf("") }
        var nColloc by remember { mutableStateOf("") }
        var nRel by remember { mutableStateOf("") }
        var nNote by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showWordDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Add English Vocabulary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nWord,
                        onValueChange = { nWord = it },
                        label = { Text("Vocabulary (Word)") },
                        modifier = Modifier.fillMaxWidth().testTag("vocab_word_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nPron,
                        onValueChange = { nPron = it },
                        label = { Text("Pronunciation") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nMeaning,
                        onValueChange = { nMeaning = it },
                        label = { Text("Meaning (Vietnamese)") },
                        modifier = Modifier.fillMaxWidth().testTag("vocab_meaning_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nDesc,
                        onValueChange = { nDesc = it },
                        label = { Text("English Definition") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nExample,
                        onValueChange = { nExample = it },
                        label = { Text("Example Sentence") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nColloc,
                        onValueChange = { nColloc = it },
                        label = { Text("Common Collocations (semicolon separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nRel,
                        onValueChange = { nRel = it },
                        label = { Text("Related Words") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nNote,
                        onValueChange = { nNote = it },
                        label = { Text("Personal Memory Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showWordDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (nWord.isNotEmpty() && nMeaning.isNotEmpty()) {
                                    viewModel.addCustomVocabulary(
                                        deckId, nWord, nPron.ifEmpty { "/$nWord/" }, nMeaning, nDesc, nExample, nColloc, nRel, nNote
                                    )
                                    showWordDialog = false
                                } else {
                                    Toast.makeText(context, "Word and Meaning are mandatory!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                            modifier = Modifier.testTag("vocab_save_button")
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }

    // CSV BULK TEXT IMPORTER MODAL
    if (showImportDialog) {
        var csvPasteContent by remember {
            mutableStateOf(
                "word,pronunciation,meaning,description_en,example,collocation,related_words,note\n" +
                        "Aesthetic,/esˈθet.ɪk/,Thầm mỹ học,Concerned with beauty or the appreciation of beauty,The design achieves an aesthetic perfection.,aesthetic value,beautiful;artistic,Often used in arts and architecture."
            )
        }

        Dialog(onDismissRequest = { showImportDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Import Vocabularies from CSV", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Paste standard comma-separated text into the box below. The first row must be the header.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = csvPasteContent,
                        onValueChange = { csvPasteContent = it },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showImportDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (csvPasteContent.trim().isNotEmpty()) {
                                    viewModel.importCsv(deckId, csvPasteContent) { res ->
                                        if (res.success) {
                                            Toast.makeText(
                                                context,
                                                "Successfully imported ${res.importedCount} vocabularies!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            showImportDialog = false
                                        } else {
                                            val errMsg = res.errors?.firstOrNull() ?: "Unknown CSV structure"
                                            Toast.makeText(context, "Error: $errMsg", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                            modifier = Modifier.testTag("csv_import_submit")
                        ) {
                            Text("Import")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VocabItemCard(
    vocab: VocabularyEntity,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onSpeak: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = vocab.word, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = vocab.pronunciation, fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onSpeak, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Listen", modifier = Modifier.size(16.dp), tint = Color(0xFF0D9488))
                    }
                }

                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Word", tint = Color.LightGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Vietnamese Translation
            Text(text = vocab.meaning, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF0D9488))

            if (vocab.descriptionEn.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vocab.descriptionEn,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (vocab.example.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Column {
                        Text("Example Sentence:", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                        Text(text = vocab.example, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            if (vocab.collocation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text("Collocations: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    val colls = vocab.collocation.split(";").joinToString(", ")
                    Text(text = colls, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
