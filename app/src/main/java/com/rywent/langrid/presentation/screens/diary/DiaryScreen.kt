package com.rywent.langrid.presentation.screens.diary

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rywent.langrid.presentation.screens.diary.components.DiaryListContent
import com.rywent.langrid.presentation.screens.diary.creationPanels.CreateDiaryPanel
import com.rywent.langrid.presentation.screens.diary.subScreens.DiaryEntryDetailScreen
import com.rywent.langrid.presentation.screens.words.components.ConfirmDeleteDialog


@Composable
fun DiaryScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val scheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState.showDetail to uiState.selectedEntry?.id,
            transitionSpec = {
                if (targetState.first) {
                    (slideInHorizontally(tween(350)) { it } togetherWith
                            slideOutHorizontally(tween(350)) { -it / 3 } + fadeOut(tween(350)))
                } else {
                    (slideInHorizontally(tween(280)) { full -> -full / 4 } + fadeIn(tween(200)) togetherWith
                            slideOutHorizontally(tween(280)) { full -> full })
                }.using(SizeTransform(clip = false))
            },
            label = "diaryDetail"
        ) { (showDetail, _) ->
            if (showDetail && uiState.selectedEntry != null) {
                DiaryEntryDetailScreen(
                    entry = uiState.selectedEntry!!,
                    onBack = { viewModel.dismissDetail() },
                    onEdit = { viewModel.openEdit(uiState.selectedEntry!!) },
                    onDelete = { viewModel.requestDelete(uiState.selectedEntry!!) }
                )
            } else {
                DiaryListContent(
                    uiState = uiState,
                    paddingValues = paddingValues,
                    scheme = scheme,
                    onSearch = viewModel::onSearchQueryChanged,
                    onOpen = viewModel::openEntry,
                    onCreate = viewModel::onCreateClick
                )
            }
        }

        if (uiState.showCreatePanel || uiState.showEditPanel) {
            CreateDiaryPanel(
                dateIso = uiState.draftDate,
                title = uiState.draftTitle,
                blocks = uiState.draftBlocks,
                focusedBlockId = uiState.focusedBlockId,
                isEdit = uiState.showEditPanel,
                onTitleChange = viewModel::onDraftTitleChange,
                onFocusBlock = viewModel::onFocusedBlockChange,
                onUpdateBlock = viewModel::updateBlock,
                onRemoveBlock = viewModel::removeBlock,
                onApplySpan = viewModel::applySpanToParagraph,
                onUpdateParagraphText = viewModel::updateParagraphText,
                onInsertParagraph = viewModel::insertParagraph,
                onInsertHeading = viewModel::insertHeading,
                onInsertDivider = viewModel::insertDivider,
                onInsertTable = viewModel::insertTable,
                onInsertCarousel = { path -> viewModel.insertCarousel(listOf(path)) },
                onInsertQuote = viewModel::insertQuote,
                onAddTableRow = viewModel::addTableRow,
                onAddTableColumn = viewModel::addTableColumn,
                onUpdateTableCell = viewModel::updateTableCell,
                onTableColumnWidth = viewModel::updateTableColumnWidth,
                onAddCarouselImage = viewModel::addToCarousel,
                onInsertVoice = viewModel::insertVoice,
                onSave = viewModel::saveDraft,
                onDismiss = viewModel::onDismissEditorAttempt
            )
        }

        if (uiState.showTodayExistsDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissTodayExistsDialog,
                title = { Text("Entry for today exists") },
                text = { Text("You already wrote today’s journal. Open it or continue editing?") },
                confirmButton = {
                    TextButton(onClick = viewModel::editTodayFromDialog) { Text("Edit") }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = viewModel::openTodayFromDialog) { Text("Open") }
                        TextButton(onClick = viewModel::dismissTodayExistsDialog) { Text("Cancel") }
                    }
                }
            )
        }

        if (uiState.showDeleteConfirm && uiState.entryPendingDelete != null) {
            ConfirmDeleteDialog(
                title = "Delete entry?",
                message = "“${uiState.entryPendingDelete!!.title ?: uiState.entryPendingDelete!!.date}” will be permanently removed.",
                onConfirm = viewModel::confirmDelete,
                onDismiss = viewModel::dismissDeleteConfirm
            )
        }

        if (uiState.showDiscardDraftDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissDiscardDialog,
                title = { Text("Save draft?") },
                text = { Text("Keep this entry as a draft or discard changes?") },
                confirmButton = {
                    TextButton(onClick = viewModel::saveAsDraft) { Text("Save draft") }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = viewModel::discardDraftAndClose) { Text("Discard") }
                        TextButton(onClick = viewModel::dismissDiscardDialog) { Text("Cancel") }
                    }
                }
            )
        }
    }
}

