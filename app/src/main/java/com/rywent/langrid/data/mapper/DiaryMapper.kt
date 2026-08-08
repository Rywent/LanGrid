package com.rywent.langrid.data.mapper

import com.rywent.langrid.data.converter.DiaryBlockJson
import com.rywent.langrid.data.local.entity.DiaryEntryEntity
import com.rywent.langrid.presentation.screens.diary.data.DiaryEntry

fun DiaryEntryEntity.toDomain(): DiaryEntry = DiaryEntry(
    id = id,
    date = date,
    title = title,
    blocks = DiaryBlockJson.decode(blocksJson),
    mood = mood,
    isDraft = isDraft,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun DiaryEntry.toEntity(): DiaryEntryEntity = DiaryEntryEntity(
    id = id,
    date = date,
    title = title,
    blocksJson = DiaryBlockJson.encode(blocks),
    mood = mood,
    isDraft = isDraft,
    createdAt = createdAt,
    updatedAt = updatedAt
)