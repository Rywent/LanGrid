package com.rywent.langrid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey val id: String,
    val date: String,
    val title: String?,
    val blocksJson: String,
    val mood: Int?,
    val isDraft: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)