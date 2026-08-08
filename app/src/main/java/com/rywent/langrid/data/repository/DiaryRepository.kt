package com.rywent.langrid.data.repository

import com.rywent.langrid.data.local.dao.DiaryDao
import com.rywent.langrid.data.mapper.toDomain
import com.rywent.langrid.data.mapper.toEntity
import com.rywent.langrid.data.utils.DiaryMediaStorage
import com.rywent.langrid.presentation.screens.diary.data.DiaryBlock
import com.rywent.langrid.presentation.screens.diary.data.DiaryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(
    private val dao: DiaryDao,
    private val media: DiaryMediaStorage
) {

    fun observeEntries(): Flow<List<DiaryEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: String): DiaryEntry? =
        dao.getById(id)?.toDomain()

    suspend fun getByDate(date: String): DiaryEntry? =
        dao.getByDate(date)?.toDomain()

    suspend fun save(entry: DiaryEntry) {
        val blocks = entry.blocks.map { persistBlockMedia(it) }
        val normalized = entry.copy(
            blocks = blocks,
            updatedAt = System.currentTimeMillis()
        )
        dao.upsert(normalized.toEntity())
    }

    private suspend fun persistBlockMedia(block: DiaryBlock): DiaryBlock = when (block) {
        is DiaryBlock.CarouselBlock -> {
            val urls = block.urls.mapNotNull { src ->
                media.persistImage(src)
            }
            block.copy(urls = urls)
        }

        is DiaryBlock.ImageBlock -> {
            val local = block.localPath?.let { media.persistImage(it) }
                ?: block.remoteUrl?.let { media.persistImage(it) }
            block.copy(
                localPath = local,
                remoteUrl = if (local != null) null else block.remoteUrl
            )
        }

        is DiaryBlock.VoiceBlock -> {
            val path = media.persistVoice(block.localPath, block.id) ?: block.localPath
            block.copy(localPath = path)
        }

        else -> block
    }

    suspend fun delete(id: String) {
        val existing = dao.getById(id)?.toDomain()
        existing?.blocks?.forEach { block ->
            when (block) {
                is DiaryBlock.CarouselBlock -> block.urls.forEach { media.deleteFile(it) }
                is DiaryBlock.ImageBlock -> media.deleteFile(block.localPath)
                is DiaryBlock.VoiceBlock -> media.deleteFile(block.localPath)
                else -> Unit
            }
        }
        dao.deleteById(id)
    }
}