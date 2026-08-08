package com.rywent.langrid.presentation.screens.diary.components.editor.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.rywent.langrid.presentation.screens.diary.components.editor.buildAnnotatedForEditor
import com.rywent.langrid.presentation.screens.diary.data.DiaryBlock
import com.rywent.langrid.presentation.screens.diary.data.SpanStyleType

@Composable
fun StyledParagraphEditor(
    block: DiaryBlock.Paragraph,
    onFocus: () -> Unit,
    onUpdate: (DiaryBlock) -> Unit,
    onUpdateParagraphText: (blockId: String, newText: String, oldText: String) -> Unit,
    onRemove: () -> Unit,
    onApplySpan: (String, Int, Int, SpanStyleType) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val clipboard = LocalClipboardManager.current

    var value by remember(block.id) {
        mutableStateOf(TextFieldValue(buildAnnotatedForEditor(block.text, block.spans)))
    }

    LaunchedEffect(block.spans) {
        value = TextFieldValue(
            annotatedString = buildAnnotatedForEditor(block.text, block.spans),
            selection = value.selection
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            BasicTextField(
                value = value,
                onValueChange = { tfv ->
                    val old = value.text
                    value = TextFieldValue(
                        annotatedString = buildAnnotatedForEditor(
                            tfv.text,
                            block.spans.mapNotNull {
                                val s = it.start.coerceIn(0, tfv.text.length)
                                val e = it.end.coerceIn(0, tfv.text.length)
                                if (s < e) it.copy(start = s, end = e) else null
                            }
                        ),
                        selection = tfv.selection
                    )
                    onUpdateParagraphText(block.id, tfv.text, old)
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = scheme.onSurface,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(scheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { if (it.isFocused) onFocus() },
                decorationBox = { inner ->
                    Box {
                        if (value.text.isEmpty()) {
                            Text("Start writing…", color = scheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                        inner()
                    }
                }
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Rounded.Close, null, tint = scheme.onSurfaceVariant)
            }
        }

        val sel = value.selection
        if (!sel.collapsed && sel.min >= 0 && sel.max <= value.text.length) {
            TextStyleToolbar(
                onBold = { onApplySpan(block.id, sel.min, sel.max, SpanStyleType.BOLD) },
                onItalic = { onApplySpan(block.id, sel.min, sel.max, SpanStyleType.ITALIC) },
                onUnderline = { onApplySpan(block.id, sel.min, sel.max, SpanStyleType.UNDERLINE) },
                onStrike = { onApplySpan(block.id, sel.min, sel.max, SpanStyleType.STRIKETHROUGH) },
                onSpoiler = { onApplySpan(block.id, sel.min, sel.max, SpanStyleType.SPOILER) },
                onCopy = {
                    clipboard.setText(AnnotatedString(value.text.substring(sel.min, sel.max)))
                }
            )
        }
    }
}