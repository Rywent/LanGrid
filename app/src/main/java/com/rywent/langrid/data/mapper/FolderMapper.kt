package com.rywent.langrid.data.mapper

import com.rywent.langrid.data.local.entity.FolderEntity
import com.rywent.langrid.data.local.entity.WordEntity
import com.rywent.langrid.presentation.screens.words.FolderNode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun FolderEntity.toDomain(
    subFolders: List<FolderNode> = emptyList(),
    words: List<com.rywent.langrid.presentation.screens.words.WordItem> = emptyList(),
    wordsCount: Int = words.size + subFolders.sumOf { it.wordsCount }
): FolderNode = FolderNode(
    id = id,
    title = title,
    description = description,
    icon = iconKey?.toIcon(),
    nativeLanguage = nativeLanguage,
    targetLanguage = targetLanguage,
    wordsCount = wordsCount,
    updated = formatUpdated(updatedAt),
    isPinned = isPinned,
    subFolders = subFolders,
    words = words
)

fun FolderNode.toEntity(parentId: String? = null): FolderEntity = FolderEntity(
    id = id,
    parentId = parentId,
    title = title,
    description = description,
    iconKey = icon?.toPath(),
    nativeLanguage = nativeLanguage,
    targetLanguage = targetLanguage,
    isPinned = isPinned,
    createdAt = System.currentTimeMillis(),
    updatedAt = System.currentTimeMillis()
)

fun buildFolderTree(
    folders: List<FolderEntity>,
    words: List<WordEntity>
): List<FolderNode> {
    val wordsByFolder = words.groupBy { it.folderId }

    fun childrenOf(parentId: String?): List<FolderNode> {
        return folders
            .filter { it.parentId == parentId }
            .map { entity ->
                val childFolders = childrenOf(entity.id)
                val folderWords = wordsByFolder[entity.id].orEmpty().map { it.toDomain() }
                val count = folderWords.size + childFolders.sumOf { it.wordsCount }
                entity.toDomain(
                    subFolders = childFolders,
                    words = folderWords,
                    wordsCount = count
                )
            }
    }

    return childrenOf(null)
}

private fun formatUpdated(ts: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return "updated ${sdf.format(Date(ts))}"
}