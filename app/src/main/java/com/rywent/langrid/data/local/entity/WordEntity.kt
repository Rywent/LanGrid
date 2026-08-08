package com.rywent.langrid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "words",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("folderId"), Index("term")]
)
data class WordEntity(
    @PrimaryKey val id: String,
    val folderId: String,
    val term: String,
    val translation: String? = null,
    val transcription: String? = null,
    val imageLocalPath: String? = null,
    val imageRemoteUrl: String? = null,
    val exampleSentence: String? = null,
    val exampleTranslation: String? = null,
    val partOfSpeech: String? = null,
    val notes: String? = null,
    val isPinned: Boolean = false,
    val progress: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)