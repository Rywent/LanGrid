package com.rywent.langrid.presentation.screens.diary.components.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.HorizontalRule
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DiaryEditorToolbar(
    onParagraph: () -> Unit,
    onHeading: (level: Int) -> Unit,
    onDivider: () -> Unit,
    onTable: () -> Unit,
    onCarousel: () -> Unit,
    onQuote: () -> Unit,
    onVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHeadingMenu by remember { mutableStateOf(false) }

    Surface(tonalElevation = 3.dp, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(onClick = onParagraph) {
                Icon(Icons.Rounded.TextFields, contentDescription = "Text")
            }
            Box {
                IconButton(onClick = { showHeadingMenu = true }) {
                    Icon(Icons.Rounded.Title, contentDescription = "Heading")
                }
                DropdownMenu(
                    expanded = showHeadingMenu,
                    onDismissRequest = { showHeadingMenu = false }
                ) {
                    (1..6).forEach { level ->
                        DropdownMenuItem(
                            text = { Text("Heading $level") },
                            onClick = {
                                showHeadingMenu = false
                                onHeading(level)
                            }
                        )
                    }
                }
            }
            IconButton(onClick = onDivider) {
                Icon(Icons.Rounded.HorizontalRule, contentDescription = "Divider")
            }
            IconButton(onClick = onTable) {
                Icon(Icons.Rounded.GridOn, contentDescription = "Table")
            }
            IconButton(onClick = onCarousel) {
                Icon(Icons.Rounded.ViewCarousel, contentDescription = "Photos")
            }
            IconButton(onClick = onQuote) {
                Icon(Icons.Rounded.FormatQuote, contentDescription = "Quote")
            }
            IconButton(onClick = onVoice) {
                Icon(Icons.Rounded.Mic, contentDescription = "Voice")
            }
        }
    }
}