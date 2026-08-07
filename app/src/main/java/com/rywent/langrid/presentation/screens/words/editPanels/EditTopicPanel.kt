package com.rywent.langrid.presentation.screens.words.editPanels

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rywent.langrid.presentation.screens.words.FolderNode
import com.rywent.langrid.presentation.screens.words.components.FullIconPicker
import com.rywent.langrid.presentation.screens.words.components.themeIcons


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditThemePanel(
    folder: FolderNode,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String?,
        icon: ImageVector
    ) -> Unit
) {
    var title by remember { mutableStateOf(folder.title) }
    var description by remember { mutableStateOf(folder.description.orEmpty()) }
    var selectedIcon by remember {
        mutableStateOf(folder.icon ?: themeIcons.first())
    }
    var titleError by remember { mutableStateOf(false) }


    var nativeExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }
    var showFullIconPicker by remember { mutableStateOf(false) }

    BackHandler(onBack = onDismiss)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        val scheme = MaterialTheme.colorScheme

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Theme",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(scheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = selectedIcon,
                        contentDescription = null,
                        tint = scheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Icon",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { showFullIconPicker = true }) {
                        Text("More icons")
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            null,
                            Modifier.size(14.dp)
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(134.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false
                ) {
                    items(themeIcons) { icon ->
                        val selected = icon == selectedIcon
                        Surface(
                            modifier = Modifier.aspectRatio(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) scheme.primaryContainer else scheme.surfaceContainerHigh,
                            onClick = { selectedIcon = icon }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text("Title *") },
                    isError = titleError,
                    supportingText = if (titleError) {{ Text("Required") }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )


                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    onSave(
                        title.trim(),
                        description.trim().ifBlank { null },
                        selectedIcon
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save changes")
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showFullIconPicker) {
        ModalBottomSheet(
            onDismissRequest = { showFullIconPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            FullIconPicker(
                selectedIcon = selectedIcon,
                onIconSelected = {
                    selectedIcon = it
                    showFullIconPicker = false
                },
                onDismiss = { showFullIconPicker = false }
            )
        }
    }
}