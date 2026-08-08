package com.rywent.langrid.presentation.screens.diary.components.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rywent.langrid.presentation.screens.diary.data.DiaryBlock

@Composable
fun DiaryTableEditor(
    block: DiaryBlock.TableBlock,
    onAddRow: () -> Unit,
    onAddColumn: () -> Unit,
    onCellChange: (row: Int, col: Int, value: String) -> Unit,
    onColumnWidth: (col: Int, widthDp: Float) -> Unit,
    onCaptionChange: (String) -> Unit,
    onRemove: () -> Unit,
    onFocus: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val cols = block.rows.firstOrNull()?.size ?: 0
    val baseWidths = if (block.columnWidthsDp.size == cols && cols > 0) {
        block.columnWidthsDp
    } else {
        List(cols.coerceAtLeast(1)) { 140f }
    }

    var localWidths by remember(block.id, cols) { mutableStateOf(baseWidths) }
    LaunchedEffect(block.columnWidthsDp, cols) {
        if (block.columnWidthsDp.size == cols && cols > 0) {
            localWidths = block.columnWidthsDp
        }
    }

    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, scheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = block.caption,
                onValueChange = onCaptionChange,
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = scheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(scheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .onFocusChanged { if (it.isFocused) onFocus() },
                decorationBox = { inner ->
                    Box {
                        if (block.caption.isEmpty()) {
                            Text(
                                "Table caption",
                                fontSize = 13.sp,
                                color = scheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                        inner()
                    }
                }
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Rounded.Close, contentDescription = "Remove table")
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Column {
                block.rows.forEachIndexed { r, row ->
                    val isHeader = r == 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        row.forEachIndexed { c, cell ->
                            val w = localWidths.getOrElse(c) { 140f }

                            OutlinedTextField(
                                value = cell,
                                onValueChange = { onCellChange(r, c, it) },
                                modifier = Modifier
                                    .width(w.dp)
                                    .onFocusChanged { if (it.isFocused) onFocus() },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = if (isHeader) {
                                        scheme.surfaceContainerHighest
                                    } else {
                                        scheme.surface
                                    },
                                    unfocusedContainerColor = if (isHeader) {
                                        scheme.surfaceContainerHighest
                                    } else {
                                        scheme.surface
                                    }
                                )
                            )

                            val dragState = rememberDraggableState { deltaPx ->
                                val deltaDp = deltaPx / density.density
                                val next = localWidths.toMutableList()
                                if (c in next.indices) {
                                    next[c] = (next[c] + deltaDp).coerceIn(72f, 320f)
                                    localWidths = next
                                    onColumnWidth(c, next[c])
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .height(48.dp)
                                    .draggable(
                                        state = dragState,
                                        orientation = Orientation.Horizontal
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(scheme.outline.copy(alpha = 0.7f))
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onAddRow) {
                Icon(Icons.Rounded.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Row")
            }
            TextButton(onClick = onAddColumn) {
                Icon(Icons.Rounded.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Column")
            }
        }
    }
}
