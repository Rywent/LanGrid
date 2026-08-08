package com.rywent.langrid.presentation.screens.diary.creationPanels

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rywent.langrid.presentation.screens.diary.components.editor.BlockListEditor
import com.rywent.langrid.presentation.screens.diary.components.editor.DiaryEditorToolbar
import com.rywent.langrid.presentation.screens.diary.components.editor.VoiceRecordBar
import com.rywent.langrid.presentation.screens.diary.data.DiaryBlock
import com.rywent.langrid.presentation.screens.diary.data.SpanStyleType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDiaryPanel(
    dateIso: String,
    title: String,
    blocks: List<DiaryBlock>,
    focusedBlockId: String?,
    isEdit: Boolean = false,
    onTitleChange: (String) -> Unit,
    onFocusBlock: (String?) -> Unit,
    onUpdateBlock: (DiaryBlock) -> Unit,
    onRemoveBlock: (String) -> Unit,
    onApplySpan: (blockId: String, start: Int, end: Int, style: SpanStyleType) -> Unit,
    onUpdateParagraphText: (blockId: String, newText: String, oldText: String) -> Unit,
    onInsertParagraph: () -> Unit,
    onInsertHeading: (Int) -> Unit,
    onInsertDivider: () -> Unit,
    onInsertTable: () -> Unit,
    onInsertCarousel: (String) -> Unit,
    onInsertQuote: () -> Unit,
    onAddTableRow: (String) -> Unit,
    onAddTableColumn: (String) -> Unit,
    onUpdateTableCell: (String, Int, Int, String) -> Unit,
    onTableColumnWidth: (tableId: String, col: Int, widthDp: Float) -> Unit,
    onAddCarouselImage: (carouselId: String, path: String) -> Unit,
    onInsertVoice: (path: String, durationMs: Long) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val carouselLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.toString()?.let { onInsertCarousel(it) }
    }

    val configuration = LocalConfiguration.current
    val dateLabel = runCatching {
        LocalDate.parse(dateIso)
            .format(DateTimeFormatter.ofPattern("EEEE, d MMM", configuration.locales[0]))
    }.getOrElse { dateIso }

    val scheme = MaterialTheme.colorScheme

    var showVoiceBar by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    val lockSheetScroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return available
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = scheme.surfaceContainerLow,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .nestedScroll(lockSheetScroll)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
                Text(text = dateLabel, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                TextButton(onClick = onSave) {
                    Text(if (isEdit) "Save" else "Post")
                }
            }

            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    lineHeight = 30.sp
                ),
                cursorBrush = SolidColor(scheme.primary),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                decorationBox = { inner ->
                    Box {
                        if (title.isEmpty()) {
                            Text(
                                text = "Title",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onSurfaceVariant.copy(alpha = 0.35f)
                                )
                            )
                        }
                        inner()
                    }
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = scheme.outlineVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            BlockListEditor(
                blocks = blocks,
                focusedBlockId = focusedBlockId,
                onFocus = onFocusBlock,
                onUpdate = onUpdateBlock,
                onRemove = onRemoveBlock,
                onApplySpan = onApplySpan,
                onUpdateParagraphText = onUpdateParagraphText,
                onAddTableRow = onAddTableRow,
                onAddTableColumn = onAddTableColumn,
                onUpdateTableCell = onUpdateTableCell,
                onTableColumnWidth = onTableColumnWidth,
                onAddCarouselImage = onAddCarouselImage,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            if (showVoiceBar) {
                VoiceRecordBar(
                    onRecorded = { path, durationMs ->
                        showVoiceBar = false
                        onInsertVoice(path, durationMs)
                    },
                    onCancel = { showVoiceBar = false }
                )
            } else {
                DiaryEditorToolbar(
                    onParagraph = onInsertParagraph,
                    onHeading = onInsertHeading,
                    onDivider = onInsertDivider,
                    onTable = onInsertTable,
                    onCarousel = { carouselLauncher.launch("image/*") },
                    onQuote = onInsertQuote,
                    onVoice = { showVoiceBar = true }
                )
            }
        }
    }
}