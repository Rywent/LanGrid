package com.rywent.langrid.presentation.screens.words.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rywent.langrid.presentation.screens.words.WordItem
import com.rywent.langrid.services.WordTtsHelper
import com.rywent.langrid.services.WordTtsManager
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailsSheet(
    word: WordItem,
    targetLanguage: String = "en",
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var isSpeaking by remember { mutableStateOf(false) }
    val ttsHelper = remember { WordTtsManager.get(context) }

    fun toggleSpeak() {
        if (isSpeaking) {
            ttsHelper.stop()
            isSpeaking = false
            return
        }
        isSpeaking = true
        ttsHelper.speakSequence(
            languageCode = targetLanguage,
            phrases = listOfNotNull(word.term, word.exampleSentence),
            onDone = { isSpeaking = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = scheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Word details",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface
                )
                Row {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(scheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    if (!word.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = word.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = word.term.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = scheme.primary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = word.term,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    word.partOfSpeech?.takeIf { it.isNotBlank() }?.let { pos ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = scheme.secondaryContainer
                        ) {
                            Text(
                                text = pos.replaceFirstChar {
                                    if (it.isLowerCase())
                                        it.titlecase(Locale.ROOT)
                                    else it.toString()
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.onSecondaryContainer
                            )
                        }
                    }

                    FilledIconButton(onClick = { toggleSpeak() }) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.Rounded.VolumeUp,
                            contentDescription = if (isSpeaking) "Stop" else "Play"
                        )
                    }
                }

                if (!word.transcription.isNullOrBlank()) {
                    Text(
                        text = word.transcription,
                        fontSize = 16.sp,
                        color = scheme.primary
                    )
                }

                if (!word.translation.isNullOrBlank()) {
                    DetailBlock(title = "Translation", body = word.translation)
                }

                if (!word.exampleSentence.isNullOrBlank()) {
                    DetailBlock(title = "Example", body = word.exampleSentence)
                }

                if (!word.exampleTranslation.isNullOrBlank()) {
                    DetailBlock(title = "Example translation", body = word.exampleTranslation)
                }

                if (!word.notes.isNullOrBlank()) {
                    DetailBlock(title = "Notes", body = word.notes)
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Edit word")
                }
            }
        }
    }
}

@Composable
private fun DetailBlock(title: String, body: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = scheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurface
            )
        }
    }
}