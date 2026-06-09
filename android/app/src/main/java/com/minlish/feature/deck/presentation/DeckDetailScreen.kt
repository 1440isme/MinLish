package com.minlish.feature.deck.presentation

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.minlish.R
import com.minlish.core.data.model.AddVocabularyResult
import com.minlish.core.data.model.VocabularyEntity
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
    viewModel: DeckDetailViewModel,
    onBack: () -> Unit,
    onStartStudy: () -> Unit,
    onStartQuiz: (String) -> Unit,
    onSpeak: (String) -> Unit,
) {
    val deck by viewModel.selectedDeck.collectAsState()
    val vocabs by viewModel.vocabulariesInSelectedDeck.collectAsState()
    val deckLearningProgress by viewModel.selectedDeckLearningProgress.collectAsState()
    val isLoadingDetail by viewModel.isLoadingDeckDetail.collectAsState()
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
    var localSameWordWarning by remember { mutableStateOf<SameWordWarningState?>(null) }
    var addWordErrorMessage by remember { mutableStateOf<String?>(null) }
    var pendingAddSnackbarMessage by remember { mutableStateOf<String?>(null) }
    var addWord by remember { mutableStateOf("") }
    var addPronunciation by remember { mutableStateOf("") }
    var addPartOfSpeech by remember { mutableStateOf(partOfSpeechOptions.first()) }
    var addMeaning by remember { mutableStateOf("") }
    var addDescriptionEn by remember { mutableStateOf("") }
    var addExample by remember { mutableStateOf("") }
    var addCollocation by remember { mutableStateOf("") }
    var addRelatedWords by remember { mutableStateOf("") }
    var addNote by remember { mutableStateOf("") }
    var addPartOfSpeechExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var selectedImportUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImportFileName by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val csvTemplateSavedText = stringResource(R.string.deck_detail_csv_template_saved)
    val csvTemplateSaveFailedText = stringResource(R.string.deck_detail_csv_template_save_failed)
    val wordAddedText = stringResource(R.string.deck_detail_word_added)
    val duplicateVocabText = stringResource(R.string.deck_detail_duplicate_vocab)
    val wordMeaningRequiredText = stringResource(R.string.deck_detail_word_meaning_required)
    val selectedCsvFallbackText = stringResource(R.string.deck_detail_selected_csv_fallback)
    val importChooseFileFirstText = stringResource(R.string.deck_detail_import_choose_file_first)
    val exportSuccessText = stringResource(R.string.deck_detail_export_success)
    val exportFailedDefaultText = stringResource(R.string.deck_detail_export_failed_default)
    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        selectedImportUri = uri
        selectedImportFileName = uri?.let { resolveDisplayName(context, it) }
    }

    val csvTemplateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val saved = writeSampleCsvTemplate(context, uri)
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                if (saved) {
                    csvTemplateSavedText
                } else {
                    csvTemplateSaveFailedText
                }
            )
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.exportCsv(deckId, uri) { result ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    result.message.takeUnless { result.success || it.isNullOrBlank() }
                        ?: if (result.success) exportSuccessText else exportFailedDefaultText
                )
            }
        }
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

    LaunchedEffect(showWordDialog, pendingAddSnackbarMessage) {
        val message = pendingAddSnackbarMessage
        if (!showWordDialog && !message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            pendingAddSnackbarMessage = null
        }
    }

    val accentTeal = Color(0xFF0D9488)
    val resetAddWordDialogForm = {
        addWord = ""
        addPronunciation = ""
        addPartOfSpeech = partOfSpeechOptions.first()
        addMeaning = ""
        addDescriptionEn = ""
        addExample = ""
        addCollocation = ""
        addRelatedWords = ""
        addNote = ""
        addPartOfSpeechExpanded = false
        addWordErrorMessage = null
        localSameWordWarning = null
    }
    val closeAddWordDialog = {
        showWordDialog = false
        addPartOfSpeechExpanded = false
        addWordErrorMessage = null
        localSameWordWarning = null
    }

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
        canManageWords -> stringResource(R.string.deck_detail_empty_manageable)
        isFavoritesDeck -> stringResource(R.string.deck_detail_empty_favorites)
        else -> stringResource(R.string.deck_detail_empty_default)
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 88.dp, start = 16.dp, end = 16.dp),
            )
        },
        containerColor = if (isSystemInDarkTheme()) Color(0xFF0F1E1B) else Color(0xFFFFF9F2),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isSystemInDarkTheme()) Color(0xFF0F1E1B) else Color(0xFFFFF9F2))
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
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
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.deck_detail_edit_deck), tint = accentTeal)
                    }
                    IconButton(onClick = { showDeleteDeckConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.deck_detail_delete_deck), tint = Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = deck!!.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
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
                        Text(stringResource(R.string.deck_detail_learn), fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                        Text(stringResource(R.string.deck_detail_practice), fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                        onClick = {
                            resetAddWordDialogForm()
                            showWordDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.deck_detail_add_manual_word), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ImportExport, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.deck_detail_import_export_csv), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
                contentPadding = PaddingValues(bottom = 140.dp),
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
                        onSpeak = { onSpeak(vocab.word) },
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
        var deckNameError by remember(deck!!.id) { mutableStateOf<String?>(null) }
        val deckNameRequiredText = stringResource(R.string.decks_name_required)
        val deckNameDuplicateText = stringResource(R.string.decks_name_duplicate)

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
                    Text(stringResource(R.string.deck_detail_edit_deck), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = dName,
                        onValueChange = {
                            dName = it
                            if (deckNameError != null) {
                                deckNameError = null
                            }
                        },
                        label = { Text(stringResource(R.string.decks_field_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = deckNameError != null,
                        supportingText = {
                            deckNameError?.let { message ->
                                Text(message)
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dDesc,
                        onValueChange = { dDesc = it },
                        label = { Text(stringResource(R.string.decks_field_description)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dTags,
                        onValueChange = { dTags = it },
                        label = { Text(stringResource(R.string.decks_field_tags)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showEditDeckDialog = false }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val normalizedName = dName.trim()
                                if (normalizedName.isBlank()) {
                                    deckNameError = deckNameRequiredText
                                } else {
                                    val tagsList = dTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    viewModel.updateCustomDeck(
                                        deckId = deckId,
                                        name = normalizedName,
                                        description = dDesc.trim(),
                                        tags = tagsList,
                                        onSuccess = { showEditDeckDialog = false },
                                        onError = { message ->
                                            if (message == deckNameDuplicateText) {
                                                deckNameError = message
                                            } else {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(message)
                                                }
                                            }
                                        },
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentTeal)
                        ) {
                            Text(stringResource(R.string.common_save))
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDeckConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteDeckConfirm = false },
            title = { Text(stringResource(R.string.deck_detail_delete_deck_title)) },
            text = {
                Text(stringResource(R.string.deck_detail_delete_deck_message, deck!!.name))
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDeckConfirm = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomDeck(deckId) {
                            onBack()
                        }
                        showDeleteDeckConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
        )
    }

    // MANUAL WORD DIALOG MODAL
    if (showWordDialog) {
        Dialog(onDismissRequest = closeAddWordDialog) {
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
                    Text(stringResource(R.string.deck_detail_add_vocab_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = addWord,
                        onValueChange = {
                            addWord = it
                            addWordErrorMessage = null
                            localSameWordWarning = null
                        },
                        label = { Text(stringResource(R.string.deck_detail_field_word)) },
                        modifier = Modifier.fillMaxWidth().testTag("vocab_word_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = addPronunciation,
                        onValueChange = {
                            addPronunciation = it
                            addWordErrorMessage = null
                            localSameWordWarning = null
                        },
                        label = { Text(stringResource(R.string.deck_detail_field_pronunciation)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = addPartOfSpeechExpanded,
                        onExpandedChange = { addPartOfSpeechExpanded = !addPartOfSpeechExpanded }
                    ) {
                        OutlinedTextField(
                            value = addPartOfSpeech,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.deck_detail_field_part_of_speech)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = addPartOfSpeechExpanded)
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = addPartOfSpeechExpanded,
                            onDismissRequest = { addPartOfSpeechExpanded = false }
                        ) {
                            partOfSpeechOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        addPartOfSpeech = option
                                        addWordErrorMessage = null
                                        localSameWordWarning = null
                                        addPartOfSpeechExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = addMeaning,
                        onValueChange = {
                            addMeaning = it
                            addWordErrorMessage = null
                            localSameWordWarning = null
                        },
                        label = { Text(stringResource(R.string.deck_detail_field_meaning)) },
                        modifier = Modifier.fillMaxWidth().testTag("vocab_meaning_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = addDescriptionEn,
                        onValueChange = {
                            addDescriptionEn = it
                            addWordErrorMessage = null
                            localSameWordWarning = null
                        },
                        label = { Text(stringResource(R.string.deck_detail_field_english_definition)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = addExample,
                        onValueChange = {
                            addExample = it
                            addWordErrorMessage = null
                            localSameWordWarning = null
                        },
                        label = { Text(stringResource(R.string.deck_detail_field_example_sentence)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = addCollocation,
                        onValueChange = {
                            addCollocation = it
                            addWordErrorMessage = null
                            localSameWordWarning = null
                        },
                        label = { Text(stringResource(R.string.deck_detail_field_collocations)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = addRelatedWords,
                        onValueChange = {
                            addRelatedWords = it
                            addWordErrorMessage = null
                            localSameWordWarning = null
                        },
                        label = { Text(stringResource(R.string.deck_detail_field_related_words)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = addNote,
                        onValueChange = {
                            addNote = it
                            addWordErrorMessage = null
                            localSameWordWarning = null
                        },
                        label = { Text(stringResource(R.string.deck_detail_field_notes)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    addWordErrorMessage?.let { message ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                        ) {
                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                            )
                        }
                    }

                    localSameWordWarning?.let { warning ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.deck_detail_same_word_warning_inline,
                                        warning.pending.word,
                                    ),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (warning.existingItems.isNotEmpty()) {
                                    Text(
                                        text = stringResource(
                                            R.string.deck_detail_existing_meanings_inline,
                                            warning.existingItems.joinToString("; ") { it.meaning },
                                        ),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.deck_detail_add_new_meaning_prompt),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Text(
                                    text = stringResource(R.string.deck_detail_add_new_meaning_hint),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Button(
                                    onClick = {
                                        viewModel.confirmAddDifferentMeaning(warning.pending) { result ->
                                            when (result) {
                                                is AddVocabularyResult.Success -> {
                                                    closeAddWordDialog()
                                                    resetAddWordDialogForm()
                                                    pendingAddSnackbarMessage = wordAddedText
                                                }
                                                is AddVocabularyResult.DuplicateExact -> {
                                                    localSameWordWarning = null
                                                    addWordErrorMessage = if (result.code == "DUPLICATE_VOCABULARY") {
                                                        duplicateVocabText
                                                    } else {
                                                        result.message
                                                    }
                                                }
                                                is AddVocabularyResult.SameWordDifferentMeaning -> {
                                                    addWordErrorMessage = null
                                                    localSameWordWarning = SameWordWarningState(
                                                        existingItems = result.existingItems,
                                                        pending = result.pendingRequest,
                                                    )
                                                }
                                                is AddVocabularyResult.Failure -> {
                                                    localSameWordWarning = null
                                                    addWordErrorMessage = result.message
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Text(stringResource(R.string.deck_detail_confirm_add_new_meaning))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = closeAddWordDialog) {
                            Text(stringResource(R.string.common_cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (addWord.isNotEmpty() && addMeaning.isNotEmpty()) {
                                    addWordErrorMessage = null
                                    val onResult: (AddVocabularyResult) -> Unit = { result ->
                                        when (result) {
                                            is AddVocabularyResult.Success -> {
                                                closeAddWordDialog()
                                                resetAddWordDialogForm()
                                                pendingAddSnackbarMessage = wordAddedText
                                            }
                                            is AddVocabularyResult.DuplicateExact -> {
                                                addWordErrorMessage = if (result.code == "DUPLICATE_VOCABULARY") {
                                                    duplicateVocabText
                                                } else {
                                                    result.message
                                                }
                                            }
                                            is AddVocabularyResult.SameWordDifferentMeaning -> {
                                                addWordErrorMessage = null
                                                localSameWordWarning = SameWordWarningState(
                                                    existingItems = result.existingItems,
                                                    pending = result.pendingRequest,
                                                )
                                            }
                                            is AddVocabularyResult.Failure -> {
                                                localSameWordWarning = null
                                                addWordErrorMessage = result.message
                                            }
                                        }
                                    }

                                    if (localSameWordWarning == null) {
                                        viewModel.addCustomVocabulary(
                                            deckId = deckId,
                                            word = addWord,
                                            pronunciation = addPronunciation,
                                            partOfSpeech = addPartOfSpeech,
                                            meaning = addMeaning,
                                            descEn = addDescriptionEn,
                                            example = addExample,
                                            collocation = addCollocation,
                                            related = addRelatedWords,
                                            note = addNote,
                                            onResult = onResult,
                                        )
                                    }
                                } else {
                                    addWordErrorMessage = wordMeaningRequiredText
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                            modifier = Modifier.testTag("vocab_save_button")
                        ) {
                            Text(
                                stringResource(R.string.common_add)
                            )
                        }
                    }
                }
            }
        }
    }

    // CSV BULK TEXT IMPORTER MODAL
    if (showImportDialog) {
        val closeCsvToolsSheet = {
            showImportDialog = false
            selectedImportUri = null
            selectedImportFileName = null
        }

        ModalBottomSheet(
            onDismissRequest = closeCsvToolsSheet,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                BottomSheetDefaults.DragHandle()
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.deck_detail_import_export_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.deck_detail_import_export_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CsvToolActionCard(
                        title = stringResource(R.string.deck_detail_export_csv),
                        description = stringResource(R.string.deck_detail_export_card_description),
                        icon = Icons.Default.Download,
                        accentColor = accentTeal,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            exportCsvLauncher.launch(buildExportCsvFileName(deck!!.name))
                        }
                    )

                    CsvToolActionCard(
                        title = stringResource(R.string.deck_detail_import_csv),
                        description = stringResource(R.string.deck_detail_import_card_description),
                        icon = Icons.Default.UploadFile,
                        accentColor = accentTeal,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            csvPickerLauncher.launch(arrayOf("text/*", "application/*"))
                        }
                    )
                }

                Text(
                    text = stringResource(R.string.deck_detail_import_help),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = { csvTemplateLauncher.launch("minlish-import-template.csv") },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = accentTeal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.deck_detail_download_sample_csv),
                        color = accentTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.deck_detail_selected_file),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Text(
                            text = selectedImportFileName ?: stringResource(R.string.deck_detail_no_csv_selected),
                            fontSize = 13.sp,
                            fontWeight = if (selectedImportFileName == null) FontWeight.Normal else FontWeight.SemiBold,
                            color = if (selectedImportFileName == null) Color.Gray else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (selectedImportFileName == null) {
                                stringResource(R.string.deck_detail_choose_csv_prompt)
                            } else {
                                stringResource(R.string.deck_detail_ready_to_import)
                            },
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = closeCsvToolsSheet) {
                        Text(stringResource(R.string.common_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedImportUri?.let { importUri ->
                                viewModel.importCsv(deckId, importUri) { res ->
                                    if (res.success || res.importedCount > 0) {
                                        importReportMessage = formatImportReport(
                                            context = context,
                                            fileName = selectedImportFileName ?: selectedCsvFallbackText,
                                            response = res
                                        )
                                        showImportReport = true
                                        closeCsvToolsSheet()
                                    } else {
                                        val errMsg = formatImportFailureMessage(context, res)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(errMsg)
                                        }
                                        closeCsvToolsSheet()
                                    }
                                }
                            } ?: coroutineScope.launch {
                                snackbarHostState.showSnackbar(importChooseFileFirstText)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                        modifier = Modifier.testTag("csv_import_submit"),
                        enabled = selectedImportUri != null
                    ) {
                        Text(stringResource(R.string.deck_detail_import_button))
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
                    Text(stringResource(R.string.deck_detail_edit_vocab_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nWord,
                        onValueChange = { nWord = it },
                        label = { Text(stringResource(R.string.deck_detail_field_word)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nPron,
                        onValueChange = { nPron = it },
                        label = { Text(stringResource(R.string.deck_detail_field_pronunciation)) },
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
                            label = { Text(stringResource(R.string.deck_detail_field_part_of_speech)) },
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
                        label = { Text(stringResource(R.string.deck_detail_field_meaning)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nDesc,
                        onValueChange = { nDesc = it },
                        label = { Text(stringResource(R.string.deck_detail_field_english_definition)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nExample,
                        onValueChange = { nExample = it },
                        label = { Text(stringResource(R.string.deck_detail_field_example_sentence)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nColloc,
                        onValueChange = { nColloc = it },
                        label = { Text(stringResource(R.string.deck_detail_field_collocations)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nRel,
                        onValueChange = { nRel = it },
                        label = { Text(stringResource(R.string.deck_detail_field_related_words)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nNote,
                        onValueChange = { nNote = it },
                        label = { Text(stringResource(R.string.deck_detail_field_notes)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { editingVocabulary = null }) {
                            Text(stringResource(R.string.common_cancel))
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
                                        snackbarHostState.showSnackbar(wordMeaningRequiredText)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentTeal)
                        ) {
                            Text(stringResource(R.string.common_save))
                        }
                    }
                }
            }
        }
    }

    if (showImportReport) {
        AlertDialog(
            onDismissRequest = { showImportReport = false },
            title = { Text(stringResource(R.string.deck_detail_import_report_title)) },
            text = { Text(importReportMessage) },
            confirmButton = {
                TextButton(onClick = { showImportReport = false }) {
                    Text(stringResource(R.string.common_ok))
                }
            },
        )
    }

    pendingDeleteVocabulary?.let { vocab ->
        AlertDialog(
            onDismissRequest = { pendingDeleteVocabulary = null },
            title = { Text(stringResource(R.string.deck_detail_delete_vocab_title)) },
            text = { Text(stringResource(R.string.deck_detail_delete_vocab_message, vocab.word)) },
            dismissButton = {
                TextButton(onClick = { pendingDeleteVocabulary = null }) {
                    Text(stringResource(R.string.common_cancel))
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
                    Text(stringResource(R.string.common_delete))
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
            text = stringResource(R.string.deck_detail_learned_progress, learnedWords, totalWords),
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
private fun CsvToolActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
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
                        Icon(Icons.Default.VolumeUp, contentDescription = stringResource(R.string.deck_detail_listen), modifier = Modifier.size(16.dp), tint = Color(0xFF0D9488))
                    }
                }

                Row {
                    if (showFavorite) {
                        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = stringResource(R.string.deck_detail_favorite),
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
                        Text(stringResource(R.string.deck_detail_example_sentence_label), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
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

        VocabularyDetailBlock(stringResource(R.string.deck_detail_block_meaning), vocab.meaning)
        VocabularyDetailBlock(stringResource(R.string.deck_detail_block_english_definition), vocab.descriptionEn)
        VocabularyDetailBlock(stringResource(R.string.deck_detail_block_example), vocab.example)
        VocabularyDetailBlock(stringResource(R.string.deck_detail_block_collocation), vocab.collocation)
        VocabularyDetailBlock(stringResource(R.string.deck_detail_block_related_words), vocab.relatedWords)
        VocabularyDetailBlock(stringResource(R.string.deck_detail_block_note), vocab.note)

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
                    Text(stringResource(R.string.common_edit))
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.common_delete))
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

private fun formatImportReport(
    context: Context,
    fileName: String,
    response: com.minlish.core.data.model.ImportCsvResponse,
): String {
    val summaryLine = when {
        response.importedCount > 0 && response.duplicateCount == 0 && response.failedCount == 0 ->
            context.getString(R.string.deck_detail_import_summary_success)
        response.importedCount > 0 ->
            context.getString(R.string.deck_detail_import_summary_partial)
        else -> context.getString(R.string.deck_detail_import_summary_none)
    }

    return buildString {
        append(summaryLine)
        append("\n\n")
        append(context.getString(R.string.deck_detail_import_report_file, fileName))
        if (response.totalRows > 0) {
            append("\n")
            append(context.getString(R.string.deck_detail_import_report_total_rows, response.totalRows))
        }
        append("\n")
        append(context.getString(R.string.deck_detail_import_report_imported, response.importedCount))
        if (response.duplicateCount > 0) {
            append("\n")
            append(context.getString(R.string.deck_detail_import_report_duplicates, response.duplicateCount))
        }
        if (response.failedCount > 0) {
            append("\n")
            append(context.getString(R.string.deck_detail_import_report_invalid_rows, response.failedCount))
        }

        response.duplicateSamples
            ?.filter { it.isNotBlank() }
            ?.take(3)
            ?.let { samples ->
                if (samples.isNotEmpty()) {
                    append("\n\n")
                    append(context.getString(R.string.deck_detail_import_report_duplicate_samples))
                    samples.forEach { append("\n• $it") }
                }
            }

        response.errors
            ?.filter { it.isNotBlank() }
            ?.take(3)
            ?.let { errors ->
                if (errors.isNotEmpty()) {
                    append("\n\n")
                    append(context.getString(R.string.deck_detail_import_report_rows_to_fix))
                    errors.forEach { append("\n• $it") }
                }
            }
    }
}

private fun formatImportFailureMessage(
    context: Context,
    response: com.minlish.core.data.model.ImportCsvResponse,
): String {
    val firstError = response.errors?.firstOrNull()?.takeIf { it.isNotBlank() }
    return firstError ?: context.getString(R.string.deck_detail_import_failure_default)
}

private fun buildExportCsvFileName(deckName: String): String {
    val safeName = deckName
        .trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')

    return "${safeName.ifBlank { "deck" }}_vocabularies.csv"
}

private fun writeSampleCsvTemplate(context: Context, uri: Uri): Boolean {
    val sampleCsv = buildString {
        appendLine("word,meaning,pronunciation,description_en,example,collocation,related_words,note,part_of_speech")
        appendLine("\"budget\",\"ngân sách\",\"/ˈbʌdʒɪt/\",\"an estimate of income and expenditure\",\"We need to reduce the budget.\",\"tight budget\",\"finance;cost\",\"common in business English\",\"noun\"")
        appendLine("\"delay\",\"trì hoãn\",\"/dɪˈleɪ/\",\"to make something happen later\",\"The flight was delayed.\",\"delay a plan\",\"postpone;defer\",\"can be verb or noun\",\"verb\"")
    }

    return runCatching {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8).use { writer ->
            checkNotNull(writer) { "Output stream is unavailable" }
            writer.write(sampleCsv)
            writer.flush()
        }
    }.isSuccess
}
