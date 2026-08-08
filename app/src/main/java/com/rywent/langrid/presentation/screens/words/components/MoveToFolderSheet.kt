package com.rywent.langrid.presentation.screens.words.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rywent.langrid.presentation.screens.words.MoveDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveToFolderSheet(
    destinations: List<MoveDestination>,
    onDismiss: () -> Unit,
    onSelect: (folderId: String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Text(
            text = "Move to…",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        if (destinations.isEmpty()) {
            Text(
                text = "No other folders in this theme",
                modifier = Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(destinations, key = { it.folderId }) { dest ->
                    ListItem(
                        headlineContent = { Text(dest.title) },
                        supportingContent = {
                            if (dest.path.isNotBlank()) Text(dest.path)
                        },
                        leadingContent = {
                            Icon(Icons.Rounded.Folder, null)
                        },
                        modifier = Modifier.clickable { onSelect(dest.folderId) }
                    )
                }
            }
        }
    }
}