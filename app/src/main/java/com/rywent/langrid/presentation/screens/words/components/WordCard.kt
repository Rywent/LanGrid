package com.rywent.langrid.presentation.screens.words.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rywent.langrid.presentation.screens.words.WordItem
import com.rywent.langrid.services.WordTtsHelper
import com.rywent.langrid.services.WordTtsManager

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WordCard(
    word: WordItem,
    targetLanguage: String = "en",
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onTogglePin: () -> Unit = {},
    onMove: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }

    var speechRate by remember { mutableFloatStateOf(1.0f) }
    val ttsHelper = remember { WordTtsManager.get(context) }

    fun toggleSpeak() {
        if (isSpeaking) {
            ttsHelper.stop()
            isSpeaking = false
            return
        }
        isSpeaking = true
        ttsHelper.setSpeechRate(speechRate)
        ttsHelper.speakSequence(
            languageCode = targetLanguage,
            phrases = listOfNotNull(
                word.term,
                word.exampleSentence
            ),
            onDone = { isSpeaking = false }
        )
    }

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
            // image
            Box(modifier = Modifier.size(72.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(scheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    if (!word.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = word.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp))
                        )
                    } else {
                        LetterPlaceholder(term = word.term)
                    }
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

            // text
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = word.term,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    PartOfSpeechChip(partOfSpeech = word.partOfSpeech)
                }

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

            // play / stop
            IconButton(
                onClick = { toggleSpeak() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.Rounded.VolumeUp,
                    contentDescription = if (isSpeaking) "Stop" else "Play",
                    tint = if (isSpeaking) scheme.primary
                    else scheme.onSurfaceVariant.copy(alpha = 0.55f),
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

                Text(
                    text = "Speech speed",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                SpeedMenuRow(
                    current = speechRate,
                    onSelect = { rate ->
                        speechRate = rate
                        ttsHelper.setSpeechRate(rate)
                        menuExpanded = false
                    }
                )

                ThinMenuItem(Icons.AutoMirrored.Rounded.DriveFileMove, "Move") {
                    menuExpanded = false
                    onMove()
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
private fun PartOfSpeechChip(partOfSpeech: String?) {
    if (partOfSpeech.isNullOrBlank()) return
    val scheme = MaterialTheme.colorScheme
    val label = when (partOfSpeech.lowercase()) {
        "noun" -> "n."
        "verb" -> "v."
        "adjective" -> "adj."
        "other" -> "other"
        else -> partOfSpeech.take(4)
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = scheme.secondaryContainer.copy(alpha = 0.7f)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = scheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
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
    if (term.isBlank()) {
        append(sentence)
        return@buildAnnotatedString
    }

    val pattern = Regex(
        pattern = """(?i)\b${Regex.escape(term.trim())}\w*\b""",
        options = setOf(RegexOption.IGNORE_CASE)
    )
    val match = pattern.find(sentence)

    if (match == null) {
        append(sentence)
        return@buildAnnotatedString
    }

    append(sentence.substring(0, match.range.first))
    withStyle(
        SpanStyle(
            fontWeight = FontWeight.SemiBold,
            color = highlightColor
        )
    ) {
        append(match.value)
    }
    append(sentence.substring(match.range.last + 1))
}

@Composable
private fun SpeedMenuRow(
    current: Float,
    onSelect: (Float) -> Unit
) {
    val options = listOf(0.5f to "0.5×", 1.0f to "1×", 2.0f to "2×")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (rate, label) ->
            val selected = current == rate
            FilterChip(
                selected = selected,
                onClick = { onSelect(rate) },
                label = {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}