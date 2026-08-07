package com.rywent.langrid.presentation.screens.words.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ThemeCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    wordsCount: Int = 25,
    nativeLanguage: String = "ru",
    targetLanguage: String = "es",
    isPinned: Boolean = false,
    menuOnLeft: Boolean = false,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            scheme.surfaceContainerHighest,
                            scheme.surfaceContainerHigh.copy(alpha = 0.85f),
                            scheme.surfaceContainer
                        )
                    )
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuExpanded = true }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 10.dp, end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPinned) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(scheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(14.dp),
                            tint = scheme.primary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(28.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(scheme.primary.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${wordsCount}w",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp, bottom = 12.dp)
                    .fillMaxWidth(0.30f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(scheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(0.55f),
                    tint = scheme.onPrimary
                )
            }

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.W900,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 12.dp),
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = description,
                fontSize = 10.sp,
                lineHeight = 18.sp,
                modifier = Modifier
                    .padding(horizontal = 18.dp, vertical = 8.dp)
                    .weight(1f)
                    .fillMaxWidth(),
                color = scheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(scheme.surfaceContainerLow)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Translate,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = scheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${nativeLanguage.uppercase()} → ${targetLanguage.uppercase()}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurfaceVariant
                    )
                }
            }
        }

        if (menuExpanded) {
            Dialog(
                onDismissRequest = { menuExpanded = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.22f))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                menuExpanded = false
                            }
                    )

                    Surface(
                        modifier = Modifier
                            .align(
                                if (menuOnLeft) Alignment.CenterStart
                                else Alignment.CenterEnd
                            )
                            .padding(horizontal = 20.dp)
                            .widthIn(min = 200.dp, max = 240.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = scheme.surfaceContainerHigh,
                        tonalElevation = 6.dp,
                        shadowElevation = 8.dp
                    ) {
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            ThinMenuItem(
                                icon = Icons.Rounded.School,
                                label = "Practice",
                                onClick = {
                                    menuExpanded = false
                                    onPractice()
                                }
                            )
                            ThinMenuItem(
                                icon = Icons.Rounded.PushPin,
                                label = if (isPinned) "Unpin" else "Pin",
                                onClick = {
                                    menuExpanded = false
                                    onTogglePin()
                                }
                            )
                            ThinMenuItem(
                                icon = Icons.Rounded.Edit,
                                label = "Edit",
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = scheme.outlineVariant.copy(alpha = 0.4f)
                            )
                            ThinMenuItem(
                                icon = Icons.Rounded.Delete,
                                label = "Delete",
                                destructive = true,
                                onClick = {
                                    menuExpanded = false
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showDeleteConfirm) {
            ConfirmDeleteDialog(
                title = "Delete theme?",
                message = "“$title” and all nested folders and words will be permanently removed.",
                onConfirm = {
                    showDeleteConfirm = false
                    onDelete()
                },
                onDismiss = { showDeleteConfirm = false }
            )
        }
    }
}