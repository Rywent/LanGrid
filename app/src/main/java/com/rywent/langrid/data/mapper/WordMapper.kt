package com.rywent.langrid.data.mapper

import com.rywent.langrid.data.local.entity.WordEntity
import com.rywent.langrid.presentation.screens.words.WordItem

fun WordEntity.toDomain(): WordItem = WordItem(
    id = id,
    term = term,
    translation = translation,
    transcription = transcription,
    imageUrl = imageLocalPath ?: imageRemoteUrl,
    exampleSentence = exampleSentence,
    exampleTranslation = exampleTranslation,
    partOfSpeech = partOfSpeech,
    isPinned = isPinned,
    progress = progress,
    notes = notes
)

fun WordItem.toEntity(
    folderId: String,
    imageLocalPath: String? = null,
    imageRemoteUrl: String? = null,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis()
): WordEntity = WordEntity(
    id = id,
    folderId = folderId,
    term = term,
    translation = translation,
    transcription = transcription,
    imageLocalPath = imageLocalPath,
    imageRemoteUrl = imageRemoteUrl,
    exampleSentence = exampleSentence,
    exampleTranslation = exampleTranslation,
    partOfSpeech = partOfSpeech,
    notes = notes,
    isPinned = isPinned,
    progress = progress,
    createdAt = createdAt,
    updatedAt = updatedAt
)