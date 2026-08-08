package com.rywent.langrid.presentation.screens.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rywent.langrid.data.repository.DiaryRepository
import com.rywent.langrid.presentation.screens.diary.data.DiaryBlock
import com.rywent.langrid.presentation.screens.diary.data.DiaryEntry
import com.rywent.langrid.presentation.screens.diary.data.SpanStyleType
import com.rywent.langrid.presentation.screens.diary.data.TextSpan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    // init

    init {
        viewModelScope.launch {
            repository.observeEntries().collect { entries ->
                _uiState.update { state ->
                    val filtered = if (state.isSearchActive && state.searchQuery.isNotBlank()) {
                        filterEntries(entries, state.searchQuery)
                    } else emptyList()
                    state.copy(
                        entries = entries,
                        filteredEntries = filtered
                    )
                }
            }
        }
    }

    // search

    private fun filterEntries(entries: List<DiaryEntry>, query: String): List<DiaryEntry> {
        val q = query.trim()
        return entries.filter { entry ->
            entry.date.contains(q, ignoreCase = true) ||
                    entry.title?.contains(q, ignoreCase = true) == true ||
                    entry.plainPreview(500).contains(q, ignoreCase = true)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            if (query.isBlank()) {
                state.copy(searchQuery = "", isSearchActive = false, filteredEntries = emptyList())
            } else {
                state.copy(
                    searchQuery = query,
                    isSearchActive = true,
                    filteredEntries = filterEntries(state.entries, query)
                )
            }
        }
    }

    // detail

    fun openEntry(entry: DiaryEntry) {
        _uiState.update {
            it.copy(showDetail = true, selectedEntry = entry, showTodayExistsDialog = false)
        }
    }

    fun dismissDetail() {
        _uiState.update { it.copy(showDetail = false, selectedEntry = null) }
    }

    // create panel

    fun onCreateClick() {
        val today = LocalDate.now().toString()
        val existing = _uiState.value.entries.find { it.date == today && !it.isDraft }
        if (existing != null) {
            _uiState.update { it.copy(showTodayExistsDialog = true, todayEntry = existing) }
            return
        }
        openCreateNew()
    }

    fun dismissTodayExistsDialog() {
        _uiState.update { it.copy(showTodayExistsDialog = false, todayEntry = null) }
    }

    fun openTodayFromDialog() {
        val entry = _uiState.value.todayEntry ?: return
        _uiState.update {
            it.copy(
                showTodayExistsDialog = false,
                todayEntry = null,
                showDetail = true,
                selectedEntry = entry
            )
        }
    }

    fun editTodayFromDialog() {
        val entry = _uiState.value.todayEntry ?: return
        _uiState.update { it.copy(showTodayExistsDialog = false, todayEntry = null) }
        openEdit(entry)
    }

    private fun openCreateNew() {
        val today = LocalDate.now().toString()
        _uiState.update {
            it.copy(
                showCreatePanel = true,
                draftDate = today,
                draftTitle = "",
                draftBlocks = listOf(DiaryBlock.Paragraph(text = "")),
                focusedBlockId = null,
                selectedEntry = null
            )
        }
    }

    fun dismissCreatePanel() {
        _uiState.update {
            it.copy(showCreatePanel = false, draftBlocks = emptyList(), draftTitle = "")
        }
    }

    // edit panel

    fun openEdit(entry: DiaryEntry) {
        _uiState.update {
            it.copy(
                showEditPanel = true,
                showDetail = false,
                selectedEntry = entry,
                draftDate = entry.date,
                draftTitle = entry.title.orEmpty(),
                draftBlocks = entry.blocks.ifEmpty { listOf(DiaryBlock.Paragraph()) },
                focusedBlockId = null
            )
        }
    }

    fun dismissEditPanel() {
        _uiState.update {
            it.copy(
                showEditPanel = false,
                selectedEntry = null,
                draftBlocks = emptyList(),
                draftTitle = ""
            )
        }
    }

    // draft

    fun onDraftTitleChange(title: String) {
        _uiState.update { it.copy(draftTitle = title) }
    }

    fun onFocusedBlockChange(id: String?) {
        _uiState.update { it.copy(focusedBlockId = id) }
    }

    fun saveDraft() {
        val state = _uiState.value
        val blocks = state.draftBlocks.filterNot { block ->
            when (block) {
                is DiaryBlock.Paragraph -> block.text.isBlank() && block.spans.isEmpty()
                is DiaryBlock.Heading -> block.text.isBlank()
                is DiaryBlock.Quote -> block.text.isBlank()
                is DiaryBlock.CarouselBlock -> block.urls.isEmpty()
                is DiaryBlock.VoiceBlock -> block.localPath.isBlank()
                else -> false
            }
        }.ifEmpty { listOf(DiaryBlock.Paragraph(text = "")) }

        val title = state.draftTitle.trim().ifBlank {
            blocks.filterIsInstance<DiaryBlock.Heading>().firstOrNull()?.text
                ?: blocks.filterIsInstance<DiaryBlock.Paragraph>().firstOrNull()?.text?.take(40)
        }

        val entry = DiaryEntry(
            id = state.selectedEntry?.id ?: UUID.randomUUID().toString(),
            date = state.draftDate,
            title = title?.trim()?.ifBlank { null },
            blocks = blocks,
            isDraft = false,
            createdAt = state.selectedEntry?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.save(entry)
            _uiState.update {
                it.copy(
                    showCreatePanel = false,
                    showEditPanel = false,
                    showDiscardDraftDialog = false,
                    selectedEntry = null,
                    draftBlocks = emptyList(),
                    draftTitle = "",
                    searchQuery = "",
                    isSearchActive = false,
                    filteredEntries = emptyList()
                )
            }
        }
    }

    fun onDismissEditorAttempt() {
        val s = _uiState.value
        val hasContent = s.draftTitle.isNotBlank() ||
                s.draftBlocks.any {
                    when (it) {
                        is DiaryBlock.Paragraph -> it.text.isNotBlank()
                        is DiaryBlock.Heading -> it.text.isNotBlank()
                        is DiaryBlock.Quote -> it.text.isNotBlank()
                        is DiaryBlock.CarouselBlock -> it.urls.isNotEmpty()
                        is DiaryBlock.TableBlock -> it.rows.any { row -> row.any { c -> c.isNotBlank() } }
                        is DiaryBlock.VoiceBlock -> it.localPath.isNotBlank()
                        else -> true
                    }
                }
        if (hasContent) {
            _uiState.update { it.copy(showDiscardDraftDialog = true) }
        } else {
            dismissCreatePanel()
            dismissEditPanel()
        }
    }

    fun saveAsDraft() {
        val state = _uiState.value
        val entry = DiaryEntry(
            id = state.selectedEntry?.id ?: UUID.randomUUID().toString(),
            date = state.draftDate,
            title = state.draftTitle.trim().ifBlank { "Draft" },
            blocks = state.draftBlocks,
            isDraft = true,
            createdAt = state.selectedEntry?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.save(entry)
            _uiState.update {
                it.copy(
                    showCreatePanel = false,
                    showEditPanel = false,
                    showDiscardDraftDialog = false,
                    selectedEntry = null,
                    draftBlocks = emptyList(),
                    draftTitle = ""
                )
            }
        }
    }

    fun discardDraftAndClose() {
        _uiState.update {
            it.copy(
                showDiscardDraftDialog = false,
                showCreatePanel = false,
                showEditPanel = false,
                draftBlocks = emptyList(),
                draftTitle = "",
                selectedEntry = null
            )
        }
    }

    fun dismissDiscardDialog() {
        _uiState.update { it.copy(showDiscardDraftDialog = false) }
    }

    // delete

    fun requestDelete(entry: DiaryEntry) {
        _uiState.update { it.copy(showDeleteConfirm = true, entryPendingDelete = entry) }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = false, entryPendingDelete = null) }
    }

    fun confirmDelete() {
        val id = _uiState.value.entryPendingDelete?.id ?: return
        viewModelScope.launch {
            repository.delete(id)
            _uiState.update {
                it.copy(
                    showDetail = false,
                    showEditPanel = false,
                    selectedEntry = null,
                    showDeleteConfirm = false,
                    entryPendingDelete = null
                )
            }
        }
    }

    // blocks insert

    fun insertHeading(level: Int) {
        insertBlock(DiaryBlock.Heading(level = level.coerceIn(1, 6)), addParagraphAfter = true)
    }

    fun insertParagraph() {
        insertBlock(DiaryBlock.Paragraph(), addParagraphAfter = false)
    }

    fun insertDivider() {
        insertBlock(DiaryBlock.Divider(), addParagraphAfter = true)
    }

    fun insertQuote() {
        insertBlock(DiaryBlock.Quote(), addParagraphAfter = true)
    }

    fun insertTable() {
        insertBlock(
            DiaryBlock.TableBlock(
                caption = "",
                rows = listOf(listOf("", ""), listOf("", "")),
                columnWidthsDp = listOf(140f, 140f)
            ),
            addParagraphAfter = true
        )
    }

    fun insertCarousel(paths: List<String>) {
        if (paths.isEmpty()) return
        insertBlock(DiaryBlock.CarouselBlock(urls = paths), addParagraphAfter = true)
    }

    fun insertVoice(path: String, durationMs: Long) {
        insertBlock(
            DiaryBlock.VoiceBlock(localPath = path, durationMs = durationMs),
            addParagraphAfter = true
        )
    }

    fun addToCarousel(carouselId: String, path: String) {
        _uiState.update { state ->
            state.copy(
                draftBlocks = state.draftBlocks.map { block ->
                    if (block is DiaryBlock.CarouselBlock && block.id == carouselId) {
                        block.copy(urls = block.urls + path)
                    } else block
                }
            )
        }
    }

    private fun insertBlock(block: DiaryBlock, addParagraphAfter: Boolean) {
        _uiState.update { state ->
            val blocks = state.draftBlocks.toMutableList()
            val focusId = state.focusedBlockId
            val index = blocks.indexOfFirst { it.id == focusId }
            val insertAt = if (index >= 0) index + 1 else blocks.size
            blocks.add(insertAt, block)
            var focus = block.id
            if (addParagraphAfter) {
                val p = DiaryBlock.Paragraph()
                blocks.add(insertAt + 1, p)
                focus = p.id
            }
            state.copy(draftBlocks = blocks, focusedBlockId = focus)
        }
    }

    // locks update / remove

    fun updateBlock(updated: DiaryBlock) {
        _uiState.update { state ->
            state.copy(
                draftBlocks = state.draftBlocks.map {
                    if (it.id == updated.id) updated else it
                }
            )
        }
    }

    fun removeBlock(blockId: String) {
        _uiState.update { state ->
            val next = state.draftBlocks.filterNot { it.id == blockId }
            state.copy(
                draftBlocks = next.ifEmpty { listOf(DiaryBlock.Paragraph()) },
                focusedBlockId = state.focusedBlockId.takeIf { it != blockId }
            )
        }
    }

    // table

    fun addTableRow(tableId: String) {
        mutateTable(tableId) { rows ->
            val cols = rows.firstOrNull()?.size ?: 2
            rows + listOf(List(cols) { "" })
        }
    }

    fun addTableColumn(tableId: String) {
        _uiState.update { state ->
            state.copy(
                draftBlocks = state.draftBlocks.map { block ->
                    if (block is DiaryBlock.TableBlock && block.id == tableId) {
                        val newRows = block.rows.map { it + "" }
                        val widths = if (block.columnWidthsDp.isEmpty()) {
                            List(newRows.firstOrNull()?.size ?: 1) { 140f }
                        } else {
                            block.columnWidthsDp + 140f
                        }
                        block.copy(rows = newRows, columnWidthsDp = widths)
                    } else block
                }
            )
        }
    }

    private fun mutateTable(
        tableId: String,
        transform: (List<List<String>>) -> List<List<String>>
    ) {
        _uiState.update { state ->
            state.copy(
                draftBlocks = state.draftBlocks.map { block ->
                    if (block is DiaryBlock.TableBlock && block.id == tableId) {
                        block.copy(rows = transform(block.rows))
                    } else block
                }
            )
        }
    }

    fun updateTableCell(tableId: String, row: Int, col: Int, value: String) {
        _uiState.update { state ->
            state.copy(
                draftBlocks = state.draftBlocks.map { block ->
                    if (block is DiaryBlock.TableBlock && block.id == tableId) {
                        val rows = block.rows.mapIndexed { r, line ->
                            if (r != row) line
                            else line.mapIndexed { c, cell -> if (c == col) value else cell }
                        }
                        block.copy(rows = rows)
                    } else block
                }
            )
        }
    }

    fun updateTableColumnWidth(tableId: String, colIndex: Int, widthDp: Float) {
        _uiState.update { state ->
            state.copy(
                draftBlocks = state.draftBlocks.map { block ->
                    if (block is DiaryBlock.TableBlock && block.id == tableId) {
                        val cols = (block.rows.firstOrNull()?.size ?: 0).coerceAtLeast(1)
                        val widths = if (block.columnWidthsDp.size == cols) {
                            block.columnWidthsDp.toMutableList()
                        } else {
                            MutableList(cols) { 140f }
                        }
                        if (colIndex in widths.indices) {
                            widths[colIndex] = widthDp.coerceIn(72f, 320f)
                        }
                        block.copy(columnWidthsDp = widths)
                    } else block
                }
            )
        }
    }

    // paragraph spans

    fun applySpanToParagraph(blockId: String, start: Int, end: Int, style: SpanStyleType) {
        if (start >= end) return
        _uiState.update { state ->
            state.copy(
                draftBlocks = state.draftBlocks.map { block ->
                    if (block is DiaryBlock.Paragraph && block.id == blockId) {
                        block.copy(spans = mergeSpan(block.spans, start, end, style))
                    } else block
                }
            )
        }
    }

    fun updateParagraphText(blockId: String, newText: String, oldText: String) {
        _uiState.update { state ->
            state.copy(
                draftBlocks = state.draftBlocks.map { block ->
                    if (block is DiaryBlock.Paragraph && block.id == blockId) {
                        block.copy(
                            text = newText,
                            spans = remapSpans(block.spans, oldText, newText)
                        )
                    } else block
                }
            )
        }
    }

    private fun mergeSpan(
        existing: List<TextSpan>,
        start: Int,
        end: Int,
        style: SpanStyleType
    ): List<TextSpan> {
        val list = existing.toMutableList()
        val exact = list.indexOfFirst { it.start == start && it.end == end }
        if (exact >= 0) {
            val old = list[exact]
            val styles = old.styles.toMutableSet()
            if (style in styles) styles.remove(style) else styles.add(style)
            if (styles.isEmpty()) list.removeAt(exact)
            else list[exact] = old.copy(styles = styles)
            return list
        }
        val result = mutableListOf<TextSpan>()
        for (span in list) {
            if (span.end <= start || span.start >= end) {
                result.add(span)
                continue
            }
            if (span.start < start) result.add(span.copy(end = start))
            if (span.end > end) result.add(span.copy(start = end))
        }
        result.add(TextSpan(start, end, setOf(style)))
        return result
            .groupBy { it.start to it.end }
            .map { (range, spans) ->
                TextSpan(range.first, range.second, spans.flatMap { it.styles }.toSet())
            }
            .filter { it.styles.isNotEmpty() && it.start < it.end }
            .sortedBy { it.start }
    }

    private fun remapSpans(
        spans: List<TextSpan>,
        oldText: String,
        newText: String
    ): List<TextSpan> {
        if (oldText == newText) return spans
        if (newText.startsWith(oldText)) return spans
        if (oldText.startsWith(newText)) {
            return spans.mapNotNull { span ->
                val end = span.end.coerceAtMost(newText.length)
                val start = span.start.coerceAtMost(end)
                if (start < end) span.copy(start = start, end = end) else null
            }
        }
        return spans.mapNotNull { span ->
            val start = span.start.coerceIn(0, newText.length)
            val end = span.end.coerceIn(0, newText.length)
            if (start < end) span.copy(start = start, end = end) else null
        }
    }
}