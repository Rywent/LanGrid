package com.rywent.langrid.presentation.screens.words.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rywent.langrid.presentation.screens.words.WordItem

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WordCard(
    word: WordItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onPlayClick: (() -> Unit)? = null,
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(72.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(scheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    LetterPlaceholder(term = word.term)
                }

                if (word.isPinned) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .size(24.dp)
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = word.term,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!word.transcription.isNullOrBlank()) {
                    Text(
                        text = word.transcription,
                        fontSize = 13.sp,
                        color = scheme.primary.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!word.exampleSentence.isNullOrBlank()) {
                    Text(
                        text = highlightTerm(
                            sentence = word.exampleSentence,
                            term = word.term,
                            highlightColor = scheme.primary
                        ),
                        fontSize = 13.sp,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
            }

            IconButton(
                onClick = { onPlayClick?.invoke() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = "Play",
                    tint = scheme.onSurfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        if (menuExpanded) {
            ContextMenuOverlay(onDismiss = { menuExpanded = false }) {
                ThinMenuItem(Icons.Rounded.PushPin, if (word.isPinned) "Unpin" else "Pin") {
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
                title = "Delete word?",
                message = "“${word.term}” will be permanently removed.",
                onConfirm = {
                    showDeleteConfirm = false
                    onDelete()
                },
                onDismiss = { showDeleteConfirm = false }
            )
        }
    }
}

@Composable
private fun LetterPlaceholder(term: String) {
    val scheme = MaterialTheme.colorScheme
    val letter = term.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(scheme.primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = scheme.primary
        )
    }
}

private fun highlightTerm(
    sentence: String,
    term: String,
    highlightColor: Color
) = buildAnnotatedString {
    val index = sentence.indexOf(term, ignoreCase = true)
    if (index < 0) {
        append(sentence)
        return@buildAnnotatedString
    }
    append(sentence.substring(0, index))
    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = highlightColor)) {
        append(sentence.substring(index, index + term.length))
    }
    append(sentence.substring(index + term.length))
}