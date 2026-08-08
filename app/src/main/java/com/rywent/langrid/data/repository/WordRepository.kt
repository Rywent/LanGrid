package com.rywent.langrid.data.repository

import com.rywent.langrid.data.local.dao.FolderDao
import com.rywent.langrid.data.local.dao.WordDao
import com.rywent.langrid.data.local.entity.WordEntity
import com.rywent.langrid.data.mapper.toDomain
import com.rywent.langrid.data.mapper.toEntity
import com.rywent.langrid.data.utils.WordImageStorage
import com.rywent.langrid.presentation.screens.words.WordItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject



class WordRepository @Inject constructor(
    private val wordDao: WordDao,
    private val folderDao: FolderDao,
    private val imageStorage: WordImageStorage
) {
    fun observeByFolder(folderId: String): Flow<List<WordItem>> =
        wordDao.observeByFolder(folderId).map { list -> list.map { it.toDomain() } }

    fun observeAll(): Flow<List<WordEntity>> = wordDao.observeAll()

    suspend fun getById(id: String): WordItem? =
        wordDao.getById(id)?.toDomain()

    suspend fun create(folderId: String, draft: WordItem): WordItem {
        return try {
            val remote = draft.imageUrl?.takeIf {
                it.startsWith("http://") || it.startsWith("https://")
            }
            val local = draft.imageUrl?.let { src ->
                try {
                    imageStorage.persist(draft.id, src)
                } catch (e: Exception) {
                    android.util.Log.e("WordRepo", "image persist failed", e)
                    null
                }
            }

            val entity = draft.toEntity(
                folderId = folderId,
                imageLocalPath = local,
                imageRemoteUrl = remote
            )
            wordDao.upsert(entity)
            touchFolder(folderId)
            android.util.Log.d("WordRepo", "word ok id=${entity.id} folder=$folderId")
            entity.toDomain()
        } catch (e: Exception) {
            android.util.Log.e("WordRepo", "create failed folder=$folderId", e)
            throw e
        }
    }

    suspend fun update(folderId: String, updated: WordItem): WordItem {
        val existing = wordDao.getById(updated.id)
        val source = updated.imageUrl
        val local = when {
            source.isNullOrBlank() -> existing?.imageLocalPath
            source == existing?.imageLocalPath -> existing.imageLocalPath
            else -> imageStorage.persist(updated.id, source) ?: existing?.imageLocalPath
        }
        val remote = source?.takeIf {
            it.startsWith("http://") || it.startsWith("https://")
        } ?: existing?.imageRemoteUrl

        val entity = updated.toEntity(
            folderId = folderId,
            imageLocalPath = local,
            imageRemoteUrl = remote,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        wordDao.upsert(entity)
        touchFolder(folderId)
        return entity.toDomain()
    }

    suspend fun togglePin(wordId: String) {
        val existing = wordDao.getById(wordId) ?: return
        wordDao.upsert(
            existing.copy(
                isPinned = !existing.isPinned,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun move(wordId: String, newFolderId: String) {
        val existing = wordDao.getById(wordId) ?: return
        val oldFolderId = existing.folderId
        wordDao.update(existing.copy(folderId = newFolderId, updatedAt = System.currentTimeMillis()))
        folderDao.touch(oldFolderId)
        folderDao.touch(newFolderId)
    }

    suspend fun delete(wordId: String) {
        val existing = wordDao.getById(wordId)
        imageStorage.delete(wordId)
        wordDao.delete(wordId)
        existing?.folderId?.let { touchFolder(it) }
    }

    suspend fun search(query: String): List<WordItem> =
        wordDao.search(query.trim()).map { it.toDomain() }

    private suspend fun touchFolder(folderId: String) {
        val f = folderDao.getById(folderId) ?: return
        folderDao.upsert(f.copy(updatedAt = System.currentTimeMillis()))
    }
}