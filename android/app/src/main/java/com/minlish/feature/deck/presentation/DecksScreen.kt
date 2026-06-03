package com.minlish.feature.deck.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.minlish.R
import com.minlish.core.data.model.DeckEntity
import kotlinx.coroutines.delay

@Composable
fun DecksScreen(
    viewModel: DecksViewModel,
    onDeckClick: (String) -> Unit,
    lazyListState: LazyListState = rememberLazyListState(),
    selectedGoalFilter: String = "MY_DECKS",
    onGoalFilterChange: (String) -> Unit = {},
    searchKey: String = "",
    onSearchKeyChange: (String) -> Unit = {},
) {
    val decks by viewModel.decksList.collectAsState()
    val isLoading by viewModel.isLoadingDecks.collectAsState()
    val lastErrorMessage by viewModel.lastErrorMessage.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(searchKey) {
        delay(300)
        viewModel.refreshDecks(searchKey)
    }

    LaunchedEffect(lastErrorMessage) {
        lastErrorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearLastError()
        }
    }

    val accentTeal = Color(0xFF0D9488)
    val backgroundColor = if (isSystemInDarkTheme()) Color(0xFF0F1E1B) else Color(0xFFFFF9F2)
    val isMyDecksTab = selectedGoalFilter == "MY_DECKS"
    val systemDecks = decks.filter { it.deckType == "SYSTEM" }
    val filteredSystemDecks = when (selectedGoalFilter) {
        "TOEIC" -> systemDecks.filter { it.learningGoal.equals("TOEIC", ignoreCase = true) }
        "IELTS" -> systemDecks.filter { it.learningGoal.equals("IELTS", ignoreCase = true) }
        else -> emptyList()
    }
    val favoritesDecks = decks.filter { it.isFavoritesDeck }
    val userDecks = decks.filter { it.deckType == "USER" && !it.isFavoritesDeck }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 88.dp, start = 16.dp, end = 16.dp),
            )
        },
        containerColor = backgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
                .padding(start = 24.dp, end = 24.dp, top = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.decks_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1C1C1A),
                )

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.decks_add_deck), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchKey,
                onValueChange = onSearchKeyChange,
                placeholder = { Text(stringResource(R.string.decks_search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchKey.isNotBlank()) {
                        IconButton(onClick = { onSearchKeyChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.decks_clear_search),
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GoalFilterChip(
                    label = stringResource(R.string.decks_tab_my_decks),
                    selected = selectedGoalFilter == "MY_DECKS",
                    onClick = { onGoalFilterChange("MY_DECKS") },
                    accentColor = accentTeal,
                )
                GoalFilterChip(
                    label = stringResource(R.string.decks_tab_toeic),
                    selected = selectedGoalFilter == "TOEIC",
                    onClick = { onGoalFilterChange("TOEIC") },
                    accentColor = accentTeal,
                )
                GoalFilterChip(
                    label = stringResource(R.string.decks_tab_ielts),
                    selected = selectedGoalFilter == "IELTS",
                    onClick = { onGoalFilterChange("IELTS") },
                    accentColor = accentTeal,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading && decks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 140.dp),
                ) {
                    if (filteredSystemDecks.isNotEmpty()) {
                        item {
                            Text(
                                text = when (selectedGoalFilter) {
                                    "TOEIC" -> stringResource(R.string.decks_section_toeic)
                                    "IELTS" -> stringResource(R.string.decks_section_ielts)
                                    else -> stringResource(R.string.decks_section_system)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentTeal,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        items(filteredSystemDecks) { deck ->
                            DeckItemCard(deck = deck, onClick = { onDeckClick(deck.id) }, accentColor = accentTeal)
                        }
                    }

                    if (isMyDecksTab && favoritesDecks.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.decks_section_favorites),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE11D48),
                                modifier = Modifier.padding(top = 16.dp),
                            )
                        }
                        items(favoritesDecks) { deck ->
                            DeckItemCard(
                                deck = deck,
                                onClick = { onDeckClick(deck.id) },
                                accentColor = Color(0xFFE11D48),
                            )
                        }
                    }

                    if (isMyDecksTab && userDecks.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.decks_section_personal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA855F7),
                                modifier = Modifier.padding(top = 16.dp),
                            )
                        }
                        items(userDecks) { deck ->
                            DeckItemCard(
                                deck = deck,
                                onClick = { onDeckClick(deck.id) },
                                accentColor = Color(0xFFA855F7),
                            )
                        }
                    }

                    if (
                        filteredSystemDecks.isEmpty() &&
                        (!isMyDecksTab || favoritesDecks.isEmpty()) &&
                        (!isMyDecksTab || userDecks.isEmpty())
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Inbox,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.Gray,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (searchKey.isNotBlank()) {
                                            stringResource(R.string.decks_empty_search, searchKey)
                                        } else if (isMyDecksTab) {
                                            stringResource(R.string.decks_empty_my_decks)
                                        } else if (selectedGoalFilter == "TOEIC") {
                                            stringResource(R.string.decks_empty_toeic)
                                        } else if (selectedGoalFilter == "IELTS") {
                                            stringResource(R.string.decks_empty_ielts)
                                        } else {
                                            stringResource(R.string.decks_empty_default)
                                        },
                                        color = Color.Gray,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var dName by remember { mutableStateOf("") }
        var dDesc by remember { mutableStateOf("") }
        var dTags by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(R.string.decks_create_dialog_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = dName,
                        onValueChange = { dName = it },
                        label = { Text(stringResource(R.string.decks_field_name)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deck_name_input"),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dDesc,
                        onValueChange = { dDesc = it },
                        label = { Text(stringResource(R.string.decks_field_description)) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dTags,
                        onValueChange = { dTags = it },
                        label = { Text(stringResource(R.string.decks_field_tags)) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        TextButton(onClick = { showCreateDialog = false }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (dName.isNotBlank()) {
                                    val tagsList = dTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    viewModel.createCustomDeck(dName, dDesc, tagsList)
                                    showCreateDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentTeal),
                            modifier = Modifier.testTag("deck_save_button"),
                        ) {
                            Text(stringResource(R.string.common_create))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
            )
        },
        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
            selectedContainerColor = accentColor,
            selectedLabelColor = Color.White,
            containerColor = Color.White,
            labelColor = Color(0xFF1C1C1A),
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) accentColor else Color(0xFFE5E7EB),
        ),
    )
}

@Composable
fun DeckItemCard(deck: DeckEntity, onClick: () -> Unit, accentColor: Color) {
    val tagList = deck.tags.split(";").filter { it.isNotEmpty() }
    val hasDeckMeta = deck.isFavoritesDeck || tagList.isNotEmpty() || deck.targetLevel.isNotBlank()
    val favoriteBadges = listOf(
        stringResource(R.string.decks_badge_favorites),
        stringResource(R.string.decks_badge_saved_words),
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFF000000).copy(alpha = 0.06f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = deck.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF7C776E),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (hasDeckMeta) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        if (deck.isFavoritesDeck) {
                            favoriteBadges.forEach { badge ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE11D48).copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = badge,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE11D48),
                                    )
                                }
                            }
                        } else {
                            tagList.take(3).forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accentColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentColor,
                                    )
                                }
                            }
                        }
                    }

                    if (!deck.isFavoritesDeck && deck.targetLevel.isNotBlank()) {
                        Text(
                            text = deck.targetLevel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
