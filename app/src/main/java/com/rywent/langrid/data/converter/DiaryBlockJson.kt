package com.rywent.langrid.data.converter

import com.rywent.langrid.presentation.screens.diary.data.DiaryBlock
import com.rywent.langrid.presentation.screens.diary.data.ImageAlign
import com.rywent.langrid.presentation.screens.diary.data.SpanStyleType
import com.rywent.langrid.presentation.screens.diary.data.TextSpan
import org.json.JSONArray
import org.json.JSONObject

object DiaryBlockJson {

    fun encode(blocks: List<DiaryBlock>): String {
        val arr = JSONArray()
        blocks.forEach { arr.put(encodeBlock(it)) }
        return arr.toString()
    }

    fun decode(json: String): List<DiaryBlock> {
        if (json.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    decodeBlock(arr.getJSONObject(i))?.let { add(it) }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun encodeBlock(block: DiaryBlock): JSONObject = when (block) {
        is DiaryBlock.Heading -> JSONObject()
            .put("type", "heading")
            .put("id", block.id)
            .put("level", block.level)
            .put("text", block.text)

        is DiaryBlock.Paragraph -> JSONObject()
            .put("type", "paragraph")
            .put("id", block.id)
            .put("text", block.text)
            .put("spans", encodeSpans(block.spans))

        is DiaryBlock.Quote -> JSONObject()
            .put("type", "quote")
            .put("id", block.id)
            .put("text", block.text)

        is DiaryBlock.Divider -> JSONObject()
            .put("type", "divider")
            .put("id", block.id)

        is DiaryBlock.ImageBlock -> JSONObject()
            .put("type", "image")
            .put("id", block.id)
            .put("localPath", block.localPath)
            .put("remoteUrl", block.remoteUrl)
            .put("align", block.align.name)
            .put("cropScale", block.cropScale.toDouble())
            .put("cropOffsetX", block.cropOffsetX.toDouble())
            .put("cropOffsetY", block.cropOffsetY.toDouble())

        is DiaryBlock.CarouselBlock -> JSONObject()
            .put("type", "carousel")
            .put("id", block.id)
            .put("urls", JSONArray(block.urls))

        is DiaryBlock.TableBlock -> {
            val rows = JSONArray()
            block.rows.forEach { row ->
                rows.put(JSONArray(row))
            }
            val widths = JSONArray()
            block.columnWidthsDp.forEach { widths.put(it.toDouble()) }
            JSONObject()
                .put("type", "table")
                .put("id", block.id)
                .put("caption", block.caption)
                .put("rows", rows)
                .put("columnWidthsDp", widths)
        }

        is DiaryBlock.VoiceBlock -> JSONObject()
            .put("type", "voice")
            .put("id", block.id)
            .put("localPath", block.localPath)
            .put("durationMs", block.durationMs)
            .put("createdAt", block.createdAt)
    }

    private fun decodeBlock(o: JSONObject): DiaryBlock? {
        val id = o.optString("id").ifBlank { return null }
        return when (o.optString("type")) {
            "heading" -> DiaryBlock.Heading(
                id = id,
                level = o.optInt("level", 2),
                text = o.optString("text")
            )
            "paragraph" -> DiaryBlock.Paragraph(
                id = id,
                text = o.optString("text"),
                spans = decodeSpans(o.optJSONArray("spans"))
            )
            "quote" -> DiaryBlock.Quote(id = id, text = o.optString("text"))
            "divider" -> DiaryBlock.Divider(id = id)
            "image" -> DiaryBlock.ImageBlock(
                id = id,
                localPath = o.optString("localPath").ifBlank { null },
                remoteUrl = o.optString("remoteUrl").ifBlank { null },
                align = runCatching {
                    ImageAlign.valueOf(o.optString("align", "CENTER"))
                }.getOrDefault(ImageAlign.CENTER),
                cropScale = o.optDouble("cropScale", 1.0).toFloat(),
                cropOffsetX = o.optDouble("cropOffsetX", 0.0).toFloat(),
                cropOffsetY = o.optDouble("cropOffsetY", 0.0).toFloat()
            )
            "carousel" -> {
                val urls = mutableListOf<String>()
                val arr = o.optJSONArray("urls")
                if (arr != null) {
                    for (i in 0 until arr.length()) urls.add(arr.getString(i))
                }
                DiaryBlock.CarouselBlock(id = id, urls = urls)
            }
            "table" -> {
                val rows = mutableListOf<List<String>>()
                val rowsArr = o.optJSONArray("rows")
                if (rowsArr != null) {
                    for (r in 0 until rowsArr.length()) {
                        val rowArr = rowsArr.getJSONArray(r)
                        val row = mutableListOf<String>()
                        for (c in 0 until rowArr.length()) row.add(rowArr.getString(c))
                        rows.add(row)
                    }
                }
                val widths = mutableListOf<Float>()
                val wArr = o.optJSONArray("columnWidthsDp")
                if (wArr != null) {
                    for (i in 0 until wArr.length()) widths.add(wArr.getDouble(i).toFloat())
                }
                DiaryBlock.TableBlock(
                    id = id,
                    caption = o.optString("caption"),
                    rows = rows.ifEmpty { listOf(listOf("", ""), listOf("", "")) },
                    columnWidthsDp = widths
                )
            }
            "voice" -> DiaryBlock.VoiceBlock(
                id = id,
                localPath = o.optString("localPath"),
                durationMs = o.optLong("durationMs"),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
            else -> null
        }
    }

    private fun encodeSpans(spans: List<TextSpan>): JSONArray {
        val arr = JSONArray()
        spans.forEach { span ->
            val styles = JSONArray()
            span.styles.forEach { styles.put(it.name) }
            arr.put(
                JSONObject()
                    .put("start", span.start)
                    .put("end", span.end)
                    .put("styles", styles)
            )
        }
        return arr
    }

    private fun decodeSpans(arr: JSONArray?): List<TextSpan> {
        if (arr == null) return emptyList()
        val list = mutableListOf<TextSpan>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val stylesArr = o.optJSONArray("styles")
            val styles = mutableSetOf<SpanStyleType>()
            if (stylesArr != null) {
                for (j in 0 until stylesArr.length()) {
                    runCatching {
                        styles.add(SpanStyleType.valueOf(stylesArr.getString(j)))
                    }
                }
            }
            list.add(
                TextSpan(
                    start = o.optInt("start"),
                    end = o.optInt("end"),
                    styles = styles
                )
            )
        }
        return list
    }
}