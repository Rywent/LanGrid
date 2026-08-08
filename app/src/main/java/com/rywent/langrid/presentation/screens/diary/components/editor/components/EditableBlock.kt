package com.rywent.langrid.presentation.screens.diary.components.editor.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rywent.langrid.presentation.screens.diary.data.DiaryBlock
import com.rywent.langrid.presentation.screens.diary.data.SpanStyleType

@Composable
public fun EditableBlock(
    block: DiaryBlock,
    onFocus: () -> Unit,
    onUpdate: (DiaryBlock) -> Unit,
    onRemove: () -> Unit,
    onApplySpan: (String, Int, Int, SpanStyleType) -> Unit,
    onUpdateParagraphText: (blockId: String, newText: String, oldText: String) -> Unit,
    onAddTableRow: (String) -> Unit,
    onAddTableColumn: (String) -> Unit,
    onUpdateTableCell: (String, Int, Int, String) -> Unit,
    onTableColumnWidth: (String, Int, Float) -> Unit,
    onAddCarouselImage: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme

    when (block) {
        is DiaryBlock.Heading -> {
            val size = when (block.level) {
                1 -> 28.sp
                2 -> 24.sp
                3 -> 20.sp
                4 -> 18.sp
                5 -> 16.sp
                else -> 14.sp
            }
            Row(verticalAlignment = Alignment.Top) {
                BasicTextField(
                    value = block.text,
                    onValueChange = { onUpdate(block.copy(text = it)) },
                    textStyle = TextStyle(
                        fontSize = size,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    ),
                    cursorBrush = SolidColor(scheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { if (it.isFocused) onFocus() },
                    decorationBox = { inner ->
                        Box {
                            if (block.text.isEmpty()) {
                                Text(
                                    "Heading ${block.level}",
                                    fontSize = size,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                            inner()
                        }
                    }
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Rounded.Close, null, tint = scheme.onSurfaceVariant)
                }
            }
        }

        is DiaryBlock.Paragraph -> {
            Box(modifier = modifier) {
                StyledParagraphEditor(
                    block = block,
                    onFocus = onFocus,
                    onUpdate = onUpdate,
                    onUpdateParagraphText = onUpdateParagraphText,
                    onRemove = onRemove,
                    onApplySpan = onApplySpan
                )
            }
        }

        is DiaryBlock.Quote -> {
            Row(modifier = modifier, verticalAlignment = Alignment.Top) {
                BasicTextField(
                    value = block.text,
                    onValueChange = { onUpdate(block.copy(text = it)) },
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        color = scheme.onSurfaceVariant
                    ),
                    cursorBrush = SolidColor(scheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { if (it.isFocused) onFocus() },
                    decorationBox = { inner ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, scheme.outlineVariant, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            if (block.text.isEmpty()) {
                                Text(
                                    "Quote…",
                                    fontStyle = FontStyle.Italic,
                                    color = scheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                            inner()
                        }
                    }
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Rounded.Close, null, tint = scheme.onSurfaceVariant)
                }
            }
        }

        is DiaryBlock.Divider -> {
            Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove) {
                    Icon(Icons.Rounded.Close, null, tint = scheme.onSurfaceVariant)
                }
            }
        }

        is DiaryBlock.VoiceBlock -> {
            Box(modifier = modifier) {
                VoiceBlockPlayer(
                    block = block,
                    onRemove = onRemove
                )
            }
        }

        is DiaryBlock.ImageBlock -> {
            Box(modifier = modifier){
                ImageBlockEditor(
                    block = block,
                    onUpdate = onUpdate,
                    onRemove = onRemove,
                    onFocus = onFocus
                )
            }


        }

        is DiaryBlock.CarouselBlock -> {
            Box(modifier = modifier){
                CarouselEditor(
                    block = block,
                    onRemove = onRemove,
                    onAddImage = { onAddCarouselImage(block.id, it) },
                    onFocus = onFocus
                )
            }


        }

        is DiaryBlock.TableBlock -> {
            Box(modifier = modifier){
                DiaryTableEditor(
                    block = block,
                    onAddRow = { onAddTableRow(block.id) },
                    onAddColumn = { onAddTableColumn(block.id) },
                    onCellChange = { r, c, v -> onUpdateTableCell(block.id, r, c, v) },
                    onColumnWidth = { col, w -> onTableColumnWidth(block.id, col, w) },
                    onCaptionChange = { caption ->
                        onUpdate(block.copy(caption = caption))
                    },
                    onRemove = onRemove,
                    onFocus = onFocus
                )
            }

        }
    }
}