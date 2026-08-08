package com.rywent.langrid.presentation.screens.diary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rywent.langrid.presentation.screens.diary.DiaryUiState
import com.rywent.langrid.presentation.screens.diary.data.DiaryEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
public fun DiaryListContent(
    uiState: DiaryUiState,
    paddingValues: PaddingValues,
    scheme: ColorScheme,
    onSearch: (String) -> Unit,
    onOpen: (DiaryEntry) -> Unit,
    onCreate: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Journal",
                fontSize = 32.sp,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp)
            )

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearch,
                placeholder = { Text("Search by date or text…") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(100.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = scheme.surfaceContainerHigh,
                    focusedContainerColor = scheme.surfaceContainerHighest
                )
            )

            val entries = uiState.displayEntries
            if (entries.isEmpty()) {
                DiaryEmptyState(
                    title = if (uiState.isSearchActive) "Nothing found" else "No entries yet",
                    description = if (uiState.isSearchActive) {
                        "Try another date or keyword"
                    } else {
                        "Write your first daily journal — words, photos, tables, your way."
                    }
                )
            } else {
                val drafts = entries.filter { it.isDraft }
                val published = entries.filter { !it.isDraft }
                val grouped = groupEntriesByMonth(published)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (drafts.isNotEmpty()) {
                        item {
                            Text(
                                text = "Drafts",
                                style = MaterialTheme.typography.titleSmall,
                                color = scheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(drafts, key = { it.id }) { entry ->
                            DiaryEntryCard(
                                entry = entry,
                                onClick = { onOpen(entry) }
                            )
                        }
                    }

                    grouped.forEach { (month, list) ->
                        item {
                            Text(
                                text = month.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleSmall,
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                            )
                        }
                        items(list, key = { it.id }) { entry ->
                            DiaryEntryCard(
                                entry = entry,
                                onClick = { onOpen(entry) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreate,
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 35.dp, end = 25.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (uiState.hasEntryToday) "Edit today" else "New entry"
            )
        }
    }
}

private fun groupEntriesByMonth(entries: List<DiaryEntry>): List<Pair<String, List<DiaryEntry>>> {
    val fmt = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    return entries
        .groupBy { e ->
            runCatching { LocalDate.parse(e.date).format(fmt) }.getOrElse { e.date }
        }
        .toList()
        .sortedByDescending { (_, list) -> list.maxOfOrNull { it.date } ?: "" }
}