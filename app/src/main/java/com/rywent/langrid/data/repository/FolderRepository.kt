package com.rywent.langrid.data.repository

import com.rywent.langrid.data.local.dao.FolderDao
import com.rywent.langrid.data.local.dao.WordDao
import com.rywent.langrid.data.local.entity.FolderEntity
import com.rywent.langrid.data.mapper.buildFolderTree
import com.rywent.langrid.data.mapper.toPath
import com.rywent.langrid.presentation.screens.words.FolderNode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID
import javax.inject.Inject
import androidx.compose.ui.graphics.vector.ImageVector


class FolderRepository @Inject constructor(
    private val folderDao: FolderDao,
    private val wordDao: WordDao
) {

    fun observeFolderTree(): Flow<List<FolderNode>> =
        combine(
            folderDao.observeAll(),
            wordDao.observeAll()
        ) { folders, words ->
            buildFolderTree(folders, words)
        }

    suspend fun getTreeOnce(): List<FolderNode> {
        return buildFolderTree(folderDao.getAll(), wordDao.getAll())
    }

    suspend fun getById(id: String): FolderEntity? = folderDao.getById(id)

    suspend fun createTheme(
        title: String,
        description: String?,
        icon: ImageVector,
        nativeLanguage: String,
        targetLanguage: String
    ): FolderEntity {
        val entity = FolderEntity(
            id = UUID.randomUUID().toString(),
            parentId = null,
            title = title.trim(),
            description = description?.trim()?.ifBlank { null },
            iconKey = icon.toPath(),
            nativeLanguage = nativeLanguage,
            targetLanguage = targetLanguage,
            isPinned = false
        )
        folderDao.upsert(entity)
        return entity
    }

    suspend fun createSubfolder(
        parentId: String,
        title: String,
        description: String?
    ): FolderEntity {
        return try {
            val entity = FolderEntity(
                id = java.util.UUID.randomUUID().toString(),
                parentId = parentId,
                title = title.trim(),
                description = description?.trim()?.ifBlank { null },
                iconKey = null,
                isPinned = false
            )
            folderDao.upsert(entity)
            touch(parentId)
            android.util.Log.d("FolderRepo", "subfolder ok id=${entity.id} parent=$parentId")
            entity
        } catch (e: Exception) {
            android.util.Log.e("FolderRepo", "createSubfolder failed parent=$parentId", e)
            throw e
        }
    }

    suspend fun updateTheme(
        id: String,
        title: String,
        description: String?,
        icon: ImageVector
    ) {
        val existing = folderDao.getById(id) ?: return
        folderDao.upsert(
            existing.copy(
                title = title.trim(),
                description = description?.trim()?.ifBlank { null },
                iconKey = icon.toPath(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateSubfolder(id: String, title: String, description: String?) {
        val existing = folderDao.getById(id) ?: return
        folderDao.upsert(
            existing.copy(
                title = title.trim(),
                description = description?.trim()?.ifBlank { null },
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun togglePin(id: String) {
        val existing = folderDao.getById(id) ?: return
        folderDao.upsert(
            existing.copy(
                isPinned = !existing.isPinned,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun move(folderId: String, newParentId: String) {
        val existing = folderDao.getById(folderId) ?: return
        if (folderId == newParentId) return
        folderDao.update(
            existing.copy(parentId = newParentId, updatedAt = System.currentTimeMillis())
        )
        existing.parentId?.let { folderDao.touch(it) }
        folderDao.touch(newParentId)
    }

    suspend fun delete(id: String) {
        folderDao.delete(id)
    }

    private suspend fun touch(folderId: String) {
        folderDao.touch(folderId)
    }
}