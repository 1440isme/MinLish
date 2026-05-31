package com.minlish.feature.deck.presentation

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.minlish.core.data.model.AddVocabularyResult
import com.minlish.core.data.model.VocabularyEntity
import com.minlish.core.presentation.MinLishViewModel
import com.minlish.core.presentation.SameWordWarningState
import kotlinx.coroutines.launch

private val partOfSpeechOptions = listOf(
    "noun",
    "verb",
    "adjective",
    "adverb",
    "pronoun",
    "preposition",
    "conjunction",
    "interjection",
    "phrase",
    "other",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    deckId: String,
    viewModel: MinLishViewModel,
    onBack: () -> Unit,
    onStartStudy: () -> Unit,
    onStartQuiz: (String) -> Unit
) {
    val deck by viewModel.selectedDeck.collectAsState()
    val vocabs by viewModel.vocabulariesInSelectedDeck.collectAsState()
    val deckLearningProgress by viewModel.selectedDeckLearningProgress.collectAsState()
    val isLoadingDetail by viewModel.isLoadingDeckDetail.collectAsState()
    val sameWordWarning by viewModel.sameWordWarning.collectAsState()
    val favoritedIds by viewModel.favoritedSourceIds.collectAsState()
    val lastErrorMessage by viewModel.lastErrorMessage.collectAsState()

    var showWordDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showImportReport by remember { mutableStateOf(false) }
    var importReportMessage by remember { mutableStateOf("") }
    var showEditDeckDialog by remember { mutableStateOf(false) }
    var showDeleteDeckConfirm by remember { mutableStateOf(false) }
    var pendingDeleteVocabulary by remember { mutableStateOf<VocabularyEntity?>(null) }
    var selectedVocabulary by remember { mutableStateOf<VocabularyEntity?>(null) }
    var editingVocabulary by remember { mutableStateOf<VocabularyEntity?>(null) }
    val context = LocalContext.current
    var selectedImportUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImportFileName by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        selectedImportUri = uri
        selectedImportFileName = uri?.let { resolveDisplayName(context, it) }
    }

    LaunchedEffect(deckId) {
        viewModel.selectDeck(deckId)
        viewModel.refreshFavoritedIds()
    }

    LaunchedEffect(lastErrorMessage) {
        lastErrorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearLastError()
        }
    }

    val accentTeal = Color(0xFF0D9488)

    if (deck == null || isLoadingDetail) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isFavoritesDeck = deck!!.isFavoritesDeck
    val canManageWords = deck!!.deckType == "USER" && !isFavoritesDeck
    val totalWords = vocabs.size
    val newWordsAvailable = deckLearningProgress?.newWordsAvailable ?: totalWords
    val learnedWords = (totalWords - newWordsAvailable).coerceIn(0, totalWords)
    val learnedProgress = if (totalWords == 0) 0f else learnedWords.toFloat() / totalWords.toFloat()
    val emptyDeckMessage = when {
        canManageWords -> "This deck is empty. Add a word manually or import a CSV file to get started."
        isFavoritesDeck -> "You have not favorited any vocabulary yet. Tap the heart icon on a word card to save it here."
        else -> "This deck does not contain any vocabulary yet."
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = if (isSystemInDarkTheme()) Color(0xFF0F1E1B) else Color(0xFFF4F9F8),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isSystemInDarkTheme()) Color(0xFF0F1E1B) else Color(0xFFF4F9F8))
                .padding(16.dp)
        ) {
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

                if (deck!!.deckType == "USER" && !isFavoritesDeck) {
                    IconButton(onClick = { showEditDeckDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Deck", tint = accentTeal)
                    }
                    IconButton(onClick = { showDeleteDeckConfirm = true }) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStartStudy,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Learn", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Practice Button — single entry point, setup dialog handles type/count config
                Button(
                    onClick = { onStartQuiz(deckId) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Practice", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (canManageWords) {
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

            DeckLearningProgressHeader(
                learnedWords = learnedWords,
                totalWords = totalWords,
                progress = learnedProgress,
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(vocabs) { vocab ->
                    val sourceId = vocab.sourceVocabularyId ?: vocab.id
                    val isFavorited = favoritedIds.contains(sourceId)
                    VocabItemCard(
                        vocab = vocab,
                        isFavorited = isFavorited,
                        showFavorite = true,
                        onClick = { selectedVocabulary = vocab },
                        onSpeak = { viewModel.speak(vocab.word) },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(vocab) { _, error ->
                                error?.let { msg ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                }
                            }
                        },
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
                                    text = emptyDeckMessage,
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
    }

    if (showEditDeckDialog) {
        var dName by remember(deck!!.id) { mutableStateOf(deck!!.name) }
        var dDesc by remember(deck!!.id) { mutableStateOf(deck!!.description) }
        var dTags by remember(deck!!.id) {
            mutableStateOf(deck!!.tags.split(";").filter { it.isNotBlank() }.joinToString(", "))
        }

        Dialog(onDismissRequest = { showEditDeckDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Edit Deck", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = dName,
                        onValueChange = { dName = it },
                        label = { Text("Deck Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dDesc,
                        onValueChange = { dDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dTags,
                        onValueChange = { dTags = it },
                        label = { Text("Tags (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showEditDeckDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (dName.isNotBlank()) {
                                    val tagsList = dTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    viewModel.updateCustomDeck(deckId, dName, dDesc, tagsList)
                                    showEditDeckDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentTeal)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDeckConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteDeckConfirm = false },
            title = { Text("Delete deck?") },
            text = {
                Text("Delete \"${deck!!.name}\" and remove it from your personal decks? This action cannot be undone.")
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDeckConfirm = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomDeck(deckId)
                        showDeleteDeckConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete")
                }
            },
        )
    }

    // MANUAL WORD DIALOG MODAL
    if (showWordDialog) {
        var nWord by remember { mutableStateOf("") }
        var nPron by remember { mutableStateOf("") }
        var nPartOfSpeech by remember { mutableStateOf(partOfSpeechOptions.first()) }
        var nMeaning by remember { mutableStateOf("") }
        var nDesc by remember { mutableStateOf("") }
        var nExample by remember { mutableStateOf("") }
        var nColloc by remember { mutableStateOf("") }
        var nRel by remember { mutableStateOf("") }
        var nNote by remember { mutableStateOf("") }
        var partOfSpeechExpanded by remember { mutableStateOf(false) }

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

                    ExposedDropdownMenuBox(
                        expanded = partOfSpeechExpanded,
                        onExpandedChange = { partOfSpeechExpanded = !partOfSpeechExpanded }
                    ) {
                        OutlinedTextField(
                            value = nPartOfSpeech,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Part of Speech") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = partOfSpeechExpanded)
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = partOfSpeechExpanded,
                            onDismissRequest = { partOfSpeechExpanded = false }
                        ) {
                            partOfSpeechOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        nPartOfSpeech = option
                                        partOfSpeechExpanded = false
                                    }
                                )
                            }
                        }
                    }
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
                                        deckId = deckId,
                                        word = nWord,
                                        pronunciation = nPron,
                                        partOfSpeech = nPartOfSpeech,
                                        meaning = nMeaning,
                                        descEn = nDesc,
                                        example = nExample,
                                        collocation = nColloc,
                                        related = nRel,
                                        note = nNote,
                                    ) { result ->
                                        when (result) {
                                            is AddVocabularyResult.Success -> {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Word added")
                                                }
                                                showWordDialog = false
                                            }
                                            is AddVocabularyResult.DuplicateExact -> {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(result.message)
                                                }
                                            }
                                            is AddVocabularyResult.SameWordDifferentMeaning -> {
                                                showWordDialog = false
                                            }
                                            is AddVocabularyResult.Failure -> {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(result.message)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Word and Meaning are mandatory!")
                                    }
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
                        text = "Choose a .csv file from your device. Required columns: word, meaning.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { csvPickerLauncher.launch(arrayOf("text/*", "application/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Choose CSV File", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Selected file",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            Text(
                                text = selectedImportFileName ?: "No CSV file selected yet",
                                fontSize = 13.sp,
                                fontWeight = if (selectedImportFileName == null) FontWeight.Normal else FontWeight.SemiBold,
                                color = if (selectedImportFileName == null) Color.Gray else MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = if (selectedImportFileName == null) {
                                    "Choose a .csv file to continue."
                                } else {
                                    "Ready to import into this deck."
                                },
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = {
                            showImportDialog = false
                            selectedImportUri = null
                            selectedImportFileName = null
                        }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                selectedImportUri?.let { importUri ->
                                    viewModel.importCsv(deckId, importUri) { res ->
                                        if (res.success || res.importedCount > 0) {
                                            importReportMessage = formatImportReport(
                                                fileName = selectedImportFileName ?: "Selected CSV file",
                                                response = res
                                            )
                                            showImportReport = true
                                            showImportDialog = false
                                            selectedImportUri = null
                                            selectedImportFileName = null
                                        } else {
                                            val errMsg = formatImportFailureMessage(res)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(errMsg)
                                            }
                                            selectedImportUri = null
                                            selectedImportFileName = null
                                            showImportDialog = false
                                        }
                                    }
                                } ?: coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please choose a CSV file before importing.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                            modifier = Modifier.testTag("csv_import_submit"),
                            enabled = selectedImportUri != null
                        ) {
                            Text("Import")
                        }
                    }
                }
            }
        }
    }

    editingVocabulary?.let { vocab ->
        var nWord by remember(vocab.id) { mutableStateOf(vocab.word) }
        var nPron by remember(vocab.id) { mutableStateOf(vocab.pronunciation) }
        var nPartOfSpeech by remember(vocab.id) {
            mutableStateOf(vocab.partOfSpeech.ifBlank { partOfSpeechOptions.first() })
        }
        var nMeaning by remember(vocab.id) { mutableStateOf(vocab.meaning) }
        var nDesc by remember(vocab.id) { mutableStateOf(vocab.descriptionEn) }
        var nExample by remember(vocab.id) { mutableStateOf(vocab.example) }
        var nColloc by remember(vocab.id) { mutableStateOf(vocab.collocation) }
        var nRel by remember(vocab.id) { mutableStateOf(vocab.relatedWords) }
        var nNote by remember(vocab.id) { mutableStateOf(vocab.note) }
        var partOfSpeechExpanded by remember(vocab.id) { mutableStateOf(false) }

        Dialog(onDismissRequest = { editingVocabulary = null }) {
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
                    Text("Edit Vocabulary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nWord,
                        onValueChange = { nWord = it },
                        label = { Text("Vocabulary (Word)") },
                        modifier = Modifier.fillMaxWidth(),
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

                    ExposedDropdownMenuBox(
                        expanded = partOfSpeechExpanded,
                        onExpandedChange = { partOfSpeechExpanded = !partOfSpeechExpanded }
                    ) {
                        OutlinedTextField(
                            value = nPartOfSpeech,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Part of Speech") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = partOfSpeechExpanded)
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = partOfSpeechExpanded,
                            onDismissRequest = { partOfSpeechExpanded = false }
                        ) {
                            partOfSpeechOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        nPartOfSpeech = option
                                        partOfSpeechExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nMeaning,
                        onValueChange = { nMeaning = it },
                        label = { Text("Meaning (Vietnamese)") },
                        modifier = Modifier.fillMaxWidth(),
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
                        TextButton(onClick = { editingVocabulary = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (nWord.isNotBlank() && nMeaning.isNotBlank()) {
                                    viewModel.updateCustomVocabulary(
                                        id = vocab.id,
                                        deckId = deckId,
                                        word = nWord,
                                        pronunciation = nPron,
                                        partOfSpeech = nPartOfSpeech,
                                        meaning = nMeaning,
                                        descEn = nDesc,
                                        example = nExample,
                                        collocation = nColloc,
                                        related = nRel,
                                        note = nNote
                                    )
                                    editingVocabulary = null
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Word and Meaning are mandatory!")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentTeal)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    sameWordWarning?.let { warning ->
        SameWordDifferentMeaningDialog(
            warning = warning,
            onDismiss = { viewModel.dismissSameWordWarning() },
            onConfirm = {
                viewModel.confirmAddDifferentMeaning { result ->
                    when (result) {
                        is AddVocabularyResult.Success -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("New meaning added")
                            }
                        }
                        is AddVocabularyResult.DuplicateExact,
                        is AddVocabularyResult.Failure -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    (result as? AddVocabularyResult.Failure)?.message
                                        ?: (result as? AddVocabularyResult.DuplicateExact)?.message
                                        ?: "Could not add word"
                                )
                            }
                        }
                        else -> Unit
                    }
                }
            },
        )
    }

    if (showImportReport) {
        AlertDialog(
            onDismissRequest = { showImportReport = false },
            title = { Text("Import report") },
            text = { Text(importReportMessage) },
            confirmButton = {
                TextButton(onClick = { showImportReport = false }) {
                    Text("OK")
                }
            },
        )
    }

    pendingDeleteVocabulary?.let { vocab ->
        AlertDialog(
            onDismissRequest = { pendingDeleteVocabulary = null },
            title = { Text("Delete vocabulary?") },
            text = { Text("Delete \"${vocab.word}\" from this deck? This action cannot be undone.") },
            dismissButton = {
                TextButton(onClick = { pendingDeleteVocabulary = null }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomVocabulary(vocab.id, deckId)
                        pendingDeleteVocabulary = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete")
                }
            },
        )
    }

    selectedVocabulary?.let { vocab ->
        ModalBottomSheet(
            onDismissRequest = { selectedVocabulary = null }
        ) {
            VocabularyDetailSheet(
                vocab = vocab,
                canManageWords = canManageWords,
                onEdit = {
                    selectedVocabulary = null
                    editingVocabulary = vocab
                },
                onDelete = {
                    selectedVocabulary = null
                    pendingDeleteVocabulary = vocab
                }
            )
        }
    }
}

@Composable
private fun DeckLearningProgressHeader(
    learnedWords: Int,
    totalWords: Int,
    progress: Float,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Đã học: $learnedWords/$totalWords",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = Color(0xFF0D9488),
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        )

        Text(
            text = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SameWordDifferentMeaningDialog(
    warning: SameWordWarningState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val existingText = warning.existingItems.joinToString("\n") { "• ${it.word}: ${it.meaning}" }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Same word, different meaning?") },
        text = {
            Column {
                Text(warning.message)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Existing meanings:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(existingText, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Add \"${warning.pending.meaning}\" as a new flashcard?",
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Add new meaning") }
        },
    )
}

@Composable
fun VocabItemCard(
    vocab: VocabularyEntity,
    isFavorited: Boolean = false,
    showFavorite: Boolean = true,
    onClick: () -> Unit,
    onSpeak: () -> Unit,
    onToggleFavorite: () -> Unit = {},
) {
    Card(
        onClick = onClick,
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
                    if (vocab.partOfSpeech.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        PartOfSpeechChip(vocab.partOfSpeech)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onSpeak, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Listen", modifier = Modifier.size(16.dp), tint = Color(0xFF0D9488))
                    }
                }

                Row {
                    if (showFavorite) {
                        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                modifier = Modifier.size(18.dp),
                                tint = if (isFavorited) Color(0xFFE11D48) else Color.Gray,
                            )
                        }
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

        }
    }
}

@Composable
private fun PartOfSpeechChip(partOfSpeech: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF0D9488).copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = partOfSpeech,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0D9488)
        )
    }
}

@Composable
private fun VocabularyDetailSheet(
    vocab: VocabularyEntity,
    canManageWords: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = vocab.word,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (vocab.pronunciation.isNotBlank() || vocab.partOfSpeech.isNotBlank()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (vocab.pronunciation.isNotBlank()) {
                    Text(
                        text = vocab.pronunciation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                if (vocab.partOfSpeech.isNotBlank()) {
                    PartOfSpeechChip(vocab.partOfSpeech)
                }
            }
        }

        VocabularyDetailBlock("Meaning", vocab.meaning)
        VocabularyDetailBlock("English Definition", vocab.descriptionEn)
        VocabularyDetailBlock("Example", vocab.example)
        VocabularyDetailBlock("Collocation", vocab.collocation)
        VocabularyDetailBlock("Related Words", vocab.relatedWords)
        VocabularyDetailBlock("Note", vocab.note)

        if (canManageWords) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit")
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun VocabularyDetailBlock(label: String, value: String) {
    if (value.isBlank()) return

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun resolveDisplayName(context: Context, uri: Uri): String {
    context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null,
    )?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            val fileName = cursor.getString(nameIndex)
            if (!fileName.isNullOrBlank()) {
                return fileName
            }
        }
    }
    return "import.csv"
}

private fun formatImportReport(fileName: String, response: com.minlish.core.data.model.ImportCsvResponse): String {
    val summaryLine = when {
        response.importedCount > 0 && response.duplicateCount == 0 && response.failedCount == 0 ->
            "Import completed successfully."
        response.importedCount > 0 ->
            "Import completed with some skipped rows."
        else -> "No vocabularies were imported."
    }

    return buildString {
        append(summaryLine)
        append("\n\nFile: $fileName")
        if (response.totalRows > 0) {
            append("\nTotal rows processed: ${response.totalRows}")
        }
        append("\nImported successfully: ${response.importedCount}")
        if (response.duplicateCount > 0) {
            append("\nSkipped as duplicates: ${response.duplicateCount}")
        }
        if (response.failedCount > 0) {
            append("\nInvalid rows: ${response.failedCount}")
        }

        response.duplicateSamples
            ?.filter { it.isNotBlank() }
            ?.take(3)
            ?.let { samples ->
                if (samples.isNotEmpty()) {
                    append("\n\nDuplicate samples:")
                    samples.forEach { append("\n• $it") }
                }
            }

        response.errors
            ?.filter { it.isNotBlank() }
            ?.take(3)
            ?.let { errors ->
                if (errors.isNotEmpty()) {
                    append("\n\nRows that need fixing:")
                    errors.forEach { append("\n• $it") }
                }
            }
    }
}

private fun formatImportFailureMessage(response: com.minlish.core.data.model.ImportCsvResponse): String {
    val firstError = response.errors?.firstOrNull()?.takeIf { it.isNotBlank() }
    return firstError ?: "The selected CSV file could not be imported. Please check the file format and required columns."
}
