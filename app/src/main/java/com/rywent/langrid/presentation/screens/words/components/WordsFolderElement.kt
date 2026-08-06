package com.rywent.langrid.presentation.screens.words.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WordsFolderElement(
    modifier: Modifier = Modifier,
    title: String,
    wordsCount: Int,
    progress: String = "0%",
    isPinned: Boolean = false,
    onClick: () -> Unit,
    onPractice: () -> Unit = {},
    onEdit: () -> Unit = {},
    onTogglePin: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(scheme.surfaceContainerHigh)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuExpanded = true }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.size(48.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(scheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = scheme.primary
                        )
                    }

                    if (isPinned) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(scheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = "Pinned",
                                modifier = Modifier.size(12.dp),
                                tint = scheme.primary
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "$wordsCount words",
                        fontSize = 11.sp,
                        color = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        color = scheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(scheme.surfaceContainerHighest)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = scheme.onSurface
                )
                Text(
                    text = progress,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
            }
        }

        if (menuExpanded) {
            ContextMenuOverlay(
                onDismiss = { menuExpanded = false }
            ) {
                ThinMenuItem(Icons.Rounded.School, "Practice") {
                    menuExpanded = false
                    onPractice()
                }
                ThinMenuItem(Icons.Rounded.PushPin, if (isPinned) "Unpin" else "Pin") {
                    menuExpanded = false
                    onTogglePin()
                }
                ThinMenuItem(Icons.Rounded.Edit, "Edit") {
                    menuExpanded = false
                    onEdit()
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = scheme.outlineVariant.copy(alpha = 0.4f)
                )
                ThinMenuItem(Icons.Rounded.Delete, "Delete", destructive = true) {
                    menuExpanded = false
                    showDeleteConfirm = true
                }
            }
        }

        if (showDeleteConfirm) {
            ConfirmDeleteDialog(
                title = "Delete folder?",
                message = "“$title” and all nested words will be removed.",
                onConfirm = {
                    showDeleteConfirm = false
                    onDelete()
                },
                onDismiss = { showDeleteConfirm = false }
            )
        }
    }
}