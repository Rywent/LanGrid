package com.rywent.langrid.presentation.screens.diary.data

import java.util.UUID

enum class SpanStyleType {
    BOLD, ITALIC, UNDERLINE, STRIKETHROUGH, SPOILER
}

data class TextSpan(
    val start: Int,
    val end: Int,
    val styles: Set<SpanStyleType> = emptySet()
)

enum class ImageAlign { LEFT, CENTER, RIGHT }

sealed interface DiaryBlock {
    val id: String

    data class Heading(
        override val id: String = UUID.randomUUID().toString(),
        val level: Int,
        val text: String = ""
    ) : DiaryBlock

    data class Paragraph(
        override val id: String = UUID.randomUUID().toString(),
        val text: String = "",
        val spans: List<TextSpan> = emptyList()
    ) : DiaryBlock

    data class VoiceBlock(
        override val id: String = UUID.randomUUID().toString(),
        val localPath: String,
        val durationMs: Long = 0L,
        val createdAt: Long = System.currentTimeMillis()
    ) : DiaryBlock

    data class ImageBlock(
        override val id: String = UUID.randomUUID().toString(),
        val localPath: String? = null,
        val remoteUrl: String? = null,
        val align: ImageAlign = ImageAlign.CENTER,
        val cropScale: Float = 1f,
        val cropOffsetX: Float = 0f,
        val cropOffsetY: Float = 0f
    ) : DiaryBlock {
        val displayUrl: String? get() = localPath ?: remoteUrl
    }

    data class CarouselBlock(
        override val id: String = UUID.randomUUID().toString(),
        val urls: List<String> = emptyList()
    ) : DiaryBlock

    data class TableBlock(
        override val id: String = UUID.randomUUID().toString(),
        val caption: String = "",
        val rows: List<List<String>> = listOf(
            listOf("", ""),
            listOf("", "")
        ),
        val columnWidthsDp: List<Float> = emptyList()
    ) : DiaryBlock

    data class Divider(
        override val id: String = UUID.randomUUID().toString()
    ) : DiaryBlock

    data class Quote(
        override val id: String = UUID.randomUUID().toString(),
        val text: String = ""
    ) : DiaryBlock
}

data class DiaryEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val title: String? = null,
    val blocks: List<DiaryBlock> = emptyList(),
    val mood: Int? = null,
    val isDraft: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun plainPreview(maxLen: Int = 120): String {
        val raw = blocks.joinToString(" ") { block ->
            when (block) {
                is DiaryBlock.Heading -> block.text
                is DiaryBlock.Paragraph -> block.text
                is DiaryBlock.Quote -> block.text
                is DiaryBlock.TableBlock -> "Table"
                is DiaryBlock.ImageBlock -> "Photo"
                is DiaryBlock.CarouselBlock -> "${block.urls.size} photos"
                is DiaryBlock.VoiceBlock -> "Voice"
                is DiaryBlock.Divider -> ""
            }
        }.trim().replace(Regex("\\s+"), " ")
        return if (raw.length <= maxLen) raw else raw.take(maxLen).trimEnd() + "…"
    }

    fun imageCount(): Int =
        blocks.sumOf {
            when (it) {
                is DiaryBlock.ImageBlock -> 1
                is DiaryBlock.CarouselBlock -> it.urls.size
                else -> 0
            }
        }

    fun hasTable(): Boolean = blocks.any { it is DiaryBlock.TableBlock }

    fun firstImageUrl(): String? =
        blocks.firstNotNullOfOrNull {
            when (it) {
                is DiaryBlock.ImageBlock -> it.displayUrl
                is DiaryBlock.CarouselBlock -> it.urls.firstOrNull()
                else -> null
            }
        }
}