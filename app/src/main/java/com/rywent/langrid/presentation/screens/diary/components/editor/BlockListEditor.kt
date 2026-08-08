package com.rywent.langrid.presentation.screens.diary.components.editor

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.rywent.langrid.presentation.screens.diary.data.DiaryBlock
import com.rywent.langrid.presentation.screens.diary.data.SpanStyleType
import com.rywent.langrid.presentation.screens.diary.data.TextSpan
import com.rywent.langrid.presentation.screens.diary.components.editor.components.*;

@Composable
fun BlockListEditor(
    blocks: List<DiaryBlock>,
    focusedBlockId: String?,
    onFocus: (String?) -> Unit,
    onUpdate: (DiaryBlock) -> Unit,
    onRemove: (String) -> Unit,
    onApplySpan: (blockId: String, start: Int, end: Int, style: SpanStyleType) -> Unit,
    onUpdateParagraphText: (blockId: String, newText: String, oldText: String) -> Unit,
    onAddTableRow: (String) -> Unit,
    onAddTableColumn: (String) -> Unit,
    onUpdateTableCell: (String, Int, Int, String) -> Unit,
    onTableColumnWidth: (tableId: String, col: Int, widthDp: Float) -> Unit,
    onAddCarouselImage: (carouselId: String, path: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = blocks,
            key = { it.id }
        ) { block ->
            EditableBlock(
                block = block,
                onFocus = { onFocus(block.id) },
                onUpdate = onUpdate,
                onRemove = { onRemove(block.id) },
                onApplySpan = onApplySpan,
                onUpdateParagraphText = onUpdateParagraphText,
                onAddTableRow = onAddTableRow,
                onAddTableColumn = onAddTableColumn,
                onUpdateTableCell = onUpdateTableCell,
                onTableColumnWidth = onTableColumnWidth,
                onAddCarouselImage = onAddCarouselImage,
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(280),
                    fadeOutSpec = tween(200),
                    placementSpec = tween(280)
                )
            )
        }
    }
}




fun buildAnnotatedForEditor(text: String, spans: List<TextSpan>): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        spans.forEach { span ->
            val s = span.start.coerceIn(0, text.length)
            val e = span.end.coerceIn(0, text.length)
            if (s >= e) return@forEach
            val hasU = SpanStyleType.UNDERLINE in span.styles
            val hasS = SpanStyleType.STRIKETHROUGH in span.styles
            val decoration = when {
                hasU && hasS -> TextDecoration.Underline + TextDecoration.LineThrough
                hasU -> TextDecoration.Underline
                hasS -> TextDecoration.LineThrough
                else -> null
            }
            addStyle(
                SpanStyle(
                    fontWeight = if (SpanStyleType.BOLD in span.styles) FontWeight.Bold else null,
                    fontStyle = if (SpanStyleType.ITALIC in span.styles) FontStyle.Italic else null,
                    textDecoration = decoration,
                    background = if (SpanStyleType.SPOILER in span.styles) {
                        Color(0xFF7C4DFF).copy(alpha = 0.22f)
                    } else {
                        Color.Unspecified
                    }
                ),
                s,
                e
            )
        }
    }
}

fun buildAnnotatedForRead(
    text: String,
    spans: List<TextSpan>,
    spoilerRevealed: Boolean
): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        spans.forEach { span ->
            val s = span.start.coerceIn(0, text.length)
            val e = span.end.coerceIn(0, text.length)
            if (s >= e) return@forEach

            val hasSpoiler = SpanStyleType.SPOILER in span.styles
            val hide = hasSpoiler && !spoilerRevealed

            val hasU = SpanStyleType.UNDERLINE in span.styles
            val hasS = SpanStyleType.STRIKETHROUGH in span.styles
            val decoration = when {
                hasU && hasS -> TextDecoration.Underline + TextDecoration.LineThrough
                hasU -> TextDecoration.Underline
                hasS -> TextDecoration.LineThrough
                else -> null
            }

            addStyle(
                SpanStyle(
                    fontWeight = if (SpanStyleType.BOLD in span.styles) FontWeight.Bold else null,
                    fontStyle = if (SpanStyleType.ITALIC in span.styles) FontStyle.Italic else null,
                    textDecoration = decoration,
                    color = if (hide) Color.Transparent else Color.Unspecified,
                    background = when {
                        hide -> Color(0xFF2C2C2E)
                        hasSpoiler && spoilerRevealed -> Color(0xFF7C4DFF).copy(alpha = 0.12f)
                        else -> Color.Unspecified
                    }
                ),
                s,
                e
            )
        }
    }
}






