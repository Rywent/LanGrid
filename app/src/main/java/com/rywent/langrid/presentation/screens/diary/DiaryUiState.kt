package com.rywent.langrid.presentation.screens.diary

import com.rywent.langrid.presentation.screens.diary.data.DiaryBlock
import com.rywent.langrid.presentation.screens.diary.data.DiaryEntry
import java.time.LocalDate

data class DiaryUiState(
    val entries: List<DiaryEntry> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val filteredEntries: List<DiaryEntry> = emptyList(),

    val showCreatePanel: Boolean = false,
    val showEditPanel: Boolean = false,
    val showDetail: Boolean = false,
    val selectedEntry: DiaryEntry? = null,

    val showTodayExistsDialog: Boolean = false,
    val todayEntry: DiaryEntry? = null,

    val showDeleteConfirm: Boolean = false,
    val entryPendingDelete: DiaryEntry? = null,

    val draftDate: String = LocalDate.now().toString(),
    val draftTitle: String = "",
    val draftBlocks: List<DiaryBlock> = emptyList(),
    val focusedBlockId: String? = null,
    val showDiscardDraftDialog: Boolean = false,
) {
    val hasEntryToday: Boolean
        get() = entries.any { it.date == LocalDate.now().toString() }

    val displayEntries: List<DiaryEntry>
        get() = if (isSearchActive) filteredEntries else entries
}