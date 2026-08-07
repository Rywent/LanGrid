package com.rywent.langrid.presentation.screens.words.editPanels

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rywent.langrid.presentation.screens.words.WordItem
import com.rywent.langrid.presentation.screens.words.creationPanels.WordImageSection
import com.rywent.langrid.services.ImageSearchService
import com.rywent.langrid.services.TranscriptionResult
import com.rywent.langrid.services.TranscriptionService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWordPanel(
    word: WordItem,
    targetLanguage: String = "en",
    onDismiss: () -> Unit,
    onSave: (WordItem) -> Unit
) {
    var term by remember { mutableStateOf(word.term) }
    var translation by remember { mutableStateOf(word.translation.orEmpty()) }
    var transcription by remember { mutableStateOf(word.transcription.orEmpty()) }
    var exampleSentence by remember { mutableStateOf(word.exampleSentence.orEmpty()) }
    var exampleTranslation by remember { mutableStateOf(word.exampleTranslation.orEmpty()) }
    var notes by remember { mutableStateOf(word.notes.orEmpty()) }
    var partOfSpeech by remember { mutableStateOf(word.partOfSpeech ?: "noun") }
    var termError by remember { mutableStateOf(false) }
    var transcriptionError by remember { mutableStateOf<String?>(null) }

    var imageUrls by remember {
        mutableStateOf(word.imageUrl?.let { listOf(it) } ?: emptyList())
    }
    var selectedImageUrl by remember { mutableStateOf(word.imageUrl) }
    var isLoadingImages by remember { mutableStateOf(false) }
    var hasAttemptedSelect by remember { mutableStateOf(false) }
    var isLoadingTranscription by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val uriString = it.toString()
            imageUrls = listOf(uriString) + imageUrls.filter { img -> img != uriString }
            selectedImageUrl = uriString
        }
    }

    val performImageSearch = {
        val query = term.trim()
        if (query.length >= 2) {
            coroutineScope.launch {
                isLoadingImages = true
                val results = ImageSearchService.searchImages(query)
                imageUrls = (listOfNotNull(selectedImageUrl) + results).distinct()
                isLoadingImages = false
            }
        }
    }

    val fetchAutoTranscription = {
        val currentTerm = term.trim()
        if (currentTerm.length >= 2) {
            coroutineScope.launch {
                isLoadingTranscription = true
                transcriptionError = null
                when (val result = TranscriptionService.fetchTranscription(targetLanguage, currentTerm)) {
                    is TranscriptionResult.Success -> {
                        transcription = result.transcription
                        transcriptionError = null
                    }
                    is TranscriptionResult.NotFound -> {
                        transcriptionError = "Transcription not found"
                    }
                    is TranscriptionResult.Error -> {
                        transcriptionError = result.message
                    }
                }
                isLoadingTranscription = false
            }
        }
    }

    BackHandler(onBack = onDismiss)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
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
                    text = "Edit Word",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = term,
                    onValueChange = {
                        term = it
                        termError = false
                    },
                    label = { Text("Word *") },
                    isError = termError,
                    supportingText = if (termError) {{ Text("Required") }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = performImageSearch,
                            enabled = term.trim().length >= 2 && !isLoadingImages
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search images",
                                tint = if (term.trim().length >= 2)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Part of Speech",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "noun" to "Noun",
                            "verb" to "Verb",
                            "adjective" to "Adjective",
                            "other" to "Other"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = partOfSpeech == key,
                                onClick = { partOfSpeech = key },
                                label = { Text(label, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = translation,
                    onValueChange = { translation = it },
                    label = { Text("Translation (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = transcription,
                    onValueChange = {
                        transcription = it
                        transcriptionError = null
                    },
                    label = { Text("Transcription") },
                    isError = transcriptionError != null,
                    supportingText = transcriptionError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = fetchAutoTranscription,
                            enabled = term.trim().length >= 2 && !isLoadingTranscription
                        ) {
                            if (isLoadingTranscription) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.AutoFixHigh,
                                    contentDescription = "Auto transcription",
                                    tint = if (term.trim().length >= 2)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                )

                WordImageSection(
                    imageUrls = imageUrls,
                    selectedImageUrl = selectedImageUrl,
                    isLoading = isLoadingImages,
                    showWarning = hasAttemptedSelect && selectedImageUrl == null,
                    onImageSelected = { url ->
                        selectedImageUrl = if (selectedImageUrl == url) null else url
                    },
                    onLoadMore = {
                        if (term.trim().length >= 2) {
                            coroutineScope.launch {
                                isLoadingImages = true
                                val more = ImageSearchService.loadMoreImages(term)
                                imageUrls = (imageUrls + more).distinct()
                                isLoadingImages = false
                            }
                        }
                    },
                    onChooseFromGallery = { galleryLauncher.launch("image/*") }
                )

                OutlinedTextField(
                    value = exampleSentence,
                    onValueChange = { exampleSentence = it },
                    label = { Text("Example sentence") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = exampleTranslation,
                    onValueChange = { exampleTranslation = it },
                    label = { Text("Sentence translation (optional)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (term.isBlank()) {
                        termError = true
                        return@Button
                    }
                    if (selectedImageUrl == null) {
                        hasAttemptedSelect = true
                        return@Button
                    }
                    onSave(
                        word.copy(
                            term = term.trim(),
                            translation = translation.trim().ifBlank { null },
                            transcription = transcription.trim().ifBlank { null },
                            imageUrl = selectedImageUrl,
                            exampleSentence = exampleSentence.trim().ifBlank { null },
                            exampleTranslation = exampleTranslation.trim().ifBlank { null },
                            notes = notes.trim().ifBlank { null },
                            partOfSpeech = partOfSpeech
                        )
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
}