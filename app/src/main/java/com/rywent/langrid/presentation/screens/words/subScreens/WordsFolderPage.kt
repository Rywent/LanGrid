package com.rywent.langrid.presentation.screens.words.subScreens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rywent.langrid.presentation.screens.words.FolderNode
import com.rywent.langrid.presentation.screens.words.WordItem
import com.rywent.langrid.presentation.screens.words.components.EmptyStateHint
import com.rywent.langrid.presentation.screens.words.components.EmptyStateView
import com.rywent.langrid.presentation.screens.words.components.FolderBreadcrumbRow
import com.rywent.langrid.presentation.screens.words.components.WordCard
import com.rywent.langrid.presentation.screens.words.components.WordsFolderElement

@Composable
fun WordsFolderPage(
    currentFolder: FolderNode,
    breadcrumbPath: List<String>,
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    onSubFolderClick: (FolderNode) -> Unit,
    onBreadcrumbClick: (Int) -> Unit,
    onAddWordClick: () -> Unit = {},
    onAddSubfolderClick: () -> Unit = {},
    onWordClick: (WordItem) -> Unit = {},
    onEditClick: () -> Unit = {},
    onSortFoldersClick: () -> Unit = {},
    onSortWordsClick: () -> Unit = {},
    onDeleteSubfolder: (String) -> Unit = {},
    onTogglePinSubfolder: (String) -> Unit = {},
    onEditSubfolder: (String) -> Unit = {},
    onDeleteWord: (String) -> Unit = {},
    onTogglePinWord: (String) -> Unit = {},
    onEditWord: (String) -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme

    val sortedSubFolders = currentFolder.subFolders.sortedByDescending { it.isPinned }
    val sortedWords = currentFolder.words.sortedByDescending { it.isPinned }

    val hasFolders = currentFolder.subFolders.isNotEmpty()
    val hasWords = currentFolder.words.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = scheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentFolder.title,
                        fontSize = 26.sp,
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface,
                        maxLines = 1
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onSortFoldersClick) {
                        Icon(
                            imageVector = Icons.Rounded.FilterList,
                            contentDescription = "Sort folders",
                            tint = scheme.onSurface
                        )
                    }
                    IconButton(onClick = onAddSubfolderClick) {
                        Icon(
                            imageVector = Icons.Outlined.CreateNewFolder,
                            contentDescription = "Create subfolder",
                            tint = scheme.onSurface
                        )
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Rounded.Create,
                            contentDescription = "Edit folder",
                            tint = scheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FolderBreadcrumbRow(
                path = breadcrumbPath,
                modifier = Modifier.padding(bottom = 12.dp),
                onItemClick = onBreadcrumbClick
            )

            val description = currentFolder.description
            if (!description.isNullOrBlank() || currentFolder.words.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = scheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (!description.isNullOrBlank()) {
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${currentFolder.wordsCount} words",
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.primary
                            )
                        }
                        IconButton(onClick = onSortWordsClick) {
                            Icon(
                                imageVector = Icons.Rounded.FilterList,
                                contentDescription = "Sort words",
                                tint = scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            AnimatedContent(
                targetState = currentFolder.id,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith
                            fadeOut(animationSpec = tween(250))
                },
                label = "SubFolderContentAnimation",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { folderId ->
                when {
                    !hasFolders && !hasWords -> {
                        EmptyStateView(
                            icon = Icons.Rounded.Inbox,
                            title = "Nothing here yet",
                            description = "Add words or create subfolders to start building this theme."
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(
                                top = 4.dp,
                                bottom = paddingValues.calculateBottomPadding() + 88.dp
                            )
                        ) {
                            items(
                                items = sortedSubFolders,
                                key = { "${folderId}_folder_${it.id}" }
                            ) { subFolder ->
                                WordsFolderElement(
                                    title = subFolder.title,
                                    wordsCount = subFolder.wordsCount,
                                    progress = "0%",
                                    isPinned = subFolder.isPinned,
                                    modifier = Modifier.animateItem(),
                                    onClick = { onSubFolderClick(subFolder) },
                                    onDelete = { onDeleteSubfolder(subFolder.id) },
                                    onTogglePin = { onTogglePinSubfolder(subFolder.id) },
                                    onEdit = { onEditSubfolder(subFolder.id) }
                                )
                            }

                            items(
                                items = sortedWords,
                                key = { "${folderId}_word_${it.id}" }
                            ) { word ->
                                WordCard(
                                    word = word,
                                    targetLanguage = currentFolder.targetLanguage,
                                    modifier = Modifier.animateItem(),
                                    onClick = { onWordClick(word) },
                                    onDelete = { onDeleteWord(word.id) },
                                    onTogglePin = { onTogglePinWord(word.id) },
                                    onEdit = { onEditWord(word.id) }
                                )
                            }

                            if (hasWords && !hasFolders) {
                                item {
                                    EmptyStateHint(
                                        icon = Icons.Outlined.CreateNewFolder,
                                        title = "Organize with folders",
                                        description = "You can also create subfolders to group related words."
                                    )
                                }
                            }

                            if (hasFolders && !hasWords) {
                                item {
                                    EmptyStateHint(
                                        icon = Icons.Default.Add,
                                        title = "No words yet",
                                        description = "Tap + to add your first word to this folder."
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddWordClick,
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 35.dp, end = 25.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add word"
            )
        }
    }
}

