package com.rywent.langrid.presentation.screens.diary.components.editor.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatStrikethrough
import androidx.compose.material.icons.rounded.FormatUnderlined
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
public fun TextStyleToolbar(
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onStrike: () -> Unit,
    onSpoiler: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBold) { Icon(Icons.Rounded.FormatBold, "Bold") }
            IconButton(onClick = onItalic) { Icon(Icons.Rounded.FormatItalic, "Italic") }
            IconButton(onClick = onUnderline) { Icon(Icons.Rounded.FormatUnderlined, "Underline") }
            IconButton(onClick = onStrike) { Icon(Icons.Rounded.FormatStrikethrough, "Strike") }
            IconButton(onClick = onSpoiler) { Icon(Icons.Rounded.VisibilityOff, "Spoiler") }
            IconButton(onClick = onCopy) { Icon(Icons.Rounded.ContentCopy, "Copy") }
        }
    }
}