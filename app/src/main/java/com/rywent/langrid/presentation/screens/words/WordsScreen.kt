package com.rywent.langrid.presentation.screens.words

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rywent.langrid.presentation.screens.words.components.AddWordsFolder
import com.rywent.langrid.presentation.screens.words.components.EmptyStateView
import com.rywent.langrid.presentation.screens.words.components.MoveToFolderSheet
import com.rywent.langrid.presentation.screens.words.components.SearchResultFolderCard
import com.rywent.langrid.presentation.screens.words.components.SearchResultWordCard
import com.rywent.langrid.presentation.screens.words.components.SortBottomSheet
import com.rywent.langrid.presentation.screens.words.components.SortCategory
import com.rywent.langrid.presentation.screens.words.components.ThemeCard
import com.rywent.langrid.presentation.screens.words.components.WordDetailsSheet
import com.rywent.langrid.presentation.screens.words.creationPanels.CreateSubfolderPanel
import com.rywent.langrid.presentation.screens.words.creationPanels.CreateThemePanel
import com.rywent.langrid.presentation.screens.words.creationPanels.CreateWordPanel
import com.rywent.langrid.presentation.screens.words.editPanels.EditWordPanel
import com.rywent.langrid.presentation.screens.words.editPanels.EditSubfolderPanel
import com.rywent.langrid.presentation.screens.words.editPanels.EditThemePanel
import com.rywent.langrid.presentation.screens.words.subScreens.WordsFolderPage
import kotlinx.coroutines.launch

@Composable
fun WordsScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    viewModel: WordsViewModel = hiltViewModel()
) {
    val scheme = MaterialTheme.colorScheme
    val gridState = rememberLazyGridState()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val transitionDuration = 350
    val transitionEasing = FastOutSlowInEasing

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState.currentFolder,
            contentKey = { it == null },
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally(
                        animationSpec = tween(transitionDuration, easing = transitionEasing),
                        initialOffsetX = { it }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(transitionDuration, easing = transitionEasing),
                        targetOffsetX = { -it / 3 }
                    ) + fadeOut(animationSpec = tween(transitionDuration))
                } else {
                    slideInHorizontally(
                        animationSpec = tween(transitionDuration, easing = transitionEasing),
                        initialOffsetX = { -it / 3 }
                    ) + fadeIn(animationSpec = tween(transitionDuration)) togetherWith
                            slideOutHorizontally(
                                animationSpec = tween(transitionDuration, easing = transitionEasing),
                                targetOffsetX = { it }
                            )
                }
            },
            label = "RootFolderTransition"
        ) { folder ->
            if (folder != null) {
                WordsFolderPage(
                    currentFolder = folder,
                    breadcrumbPath = uiState.breadcrumbPath,
                    paddingValues = paddingValues,
                    onBackClick = { viewModel.goBack() },
                    onSubFolderClick = { subFolder -> viewModel.navigateIntoFolder(subFolder) },
                    onBreadcrumbClick = { index -> viewModel.popToFolderIndex(index) },
                    onAddWordClick = { viewModel.onCreateWordClick() },
                    onAddSubfolderClick = { viewModel.onCreateSubfolderClick() },
                    onWordClick = { word -> viewModel.onWordClick(word) },
                    onEditWord = { wordId -> viewModel.onEditWord(wordId) },
                    onSortFoldersClick = { viewModel.onSortClick(SortCategory.FOLDERS) },
                    onSortWordsClick = { viewModel.onSortClick(SortCategory.WORDS) },
                    onDeleteSubfolder = { subFolderId -> viewModel.deleteSubfolder(subFolderId) },
                    onTogglePinSubfolder = { subFolderId -> viewModel.togglePinSubfolder(subFolderId) },
                    onEditSubfolder = { id -> viewModel.onEditSubfolder(id) },
                    onDeleteWord = { wordId -> viewModel.deleteWord(wordId) },
                    onTogglePinWord = { wordId -> viewModel.togglePinWord(wordId) },
                    onEditClick = {
                        uiState.currentFolder?.id?.let { viewModel.onEditSubfolder(it) }
                    },
                    onMoveWord = { viewModel.onMoveWord(it) },
                    onMoveSubfolder = { viewModel.onMoveSubfolder(it) }

                )
            } else {
                // root
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, start = 20.dp, end = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "My words",
                                fontSize = 32.sp,
                                style = MaterialTheme.typography.titleMedium,
                                color = scheme.onSurface
                            )
                            IconButton(onClick = { viewModel.onSortClick(SortCategory.THEMES) }) {
                                Icon(
                                    imageVector = Icons.Rounded.FilterList,
                                    contentDescription = "Sort themes",
                                    tint = scheme.onSurface
                                )
                            }
                        }
                        // search
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Search folders or words...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(100.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = scheme.surfaceContainerHigh,
                                focusedContainerColor = scheme.surfaceContainerHighest
                            ),
                            singleLine = true
                        )

                        // content
                        if (uiState.isSearchActive) {
                            if (uiState.searchResults.isEmpty()) {
                                EmptyStateView(
                                    icon = Icons.Default.Search,
                                    title = "Nothing found",
                                    description = "Try searching with a different keyword"
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 20.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 88.dp)
                                ) {
                                    items(uiState.searchResults) { result ->
                                        when (result) {
                                            is SearchResultItem.FolderResult -> {
                                                SearchResultFolderCard(
                                                    folder = result.folder,
                                                    path = result.path,
                                                    onClick = { viewModel.navigateToFolderByPath(result.folder.id) }
                                                )
                                            }
                                            is SearchResultItem.WordResult -> {
                                                SearchResultWordCard(
                                                    word = result.word,
                                                    path = result.path,
                                                    onClick = { viewModel.navigateToFolderByPath(result.folder.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (uiState.isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = scheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            if (uiState.rootFolders.isEmpty()) {
                                EmptyStateView(
                                    icon = Icons.Rounded.Inbox,
                                    title = "No themes yet",
                                    description = "Create your first theme folder below to start adding words."
                                )
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    state = gridState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    contentPadding = PaddingValues(
                                        top = 8.dp,
                                        bottom = paddingValues.calculateBottomPadding() + 88.dp,
                                        start = 8.dp,
                                        end = 8.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(
                                        items = uiState.rootFolders,
                                        key = { it.id }
                                    ) { folderItem ->
                                        val index = uiState.rootFolders.indexOfFirst { it.id == folderItem.id }
                                        val isLeftColumn = index % 2 == 0

                                        ThemeCard(
                                            title = folderItem.title,
                                            description = folderItem.description ?: "",
                                            icon = folderItem.icon ?: Icons.Rounded.Home,
                                            wordsCount = folderItem.wordsCount,
                                            nativeLanguage = folderItem.nativeLanguage,
                                            targetLanguage = folderItem.targetLanguage,
                                            isPinned = folderItem.isPinned,
                                            menuOnLeft = !isLeftColumn,
                                            modifier = Modifier.animateItem(),
                                            onClick = { viewModel.navigateIntoFolder(folderItem) },
                                            onPractice = { /* TODO */ },
                                            onEdit = { viewModel.onEditTheme(folderItem.id) },
                                            onTogglePin = {
                                                viewModel.togglePinTheme(folderItem.id)
                                                coroutineScope.launch {
                                                    gridState.scrollToItem(0)
                                                }
                                            },
                                            onDelete = { viewModel.deleteTheme(folderItem.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // create FAB
                    AddWordsFolder(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 35.dp, end = 25.dp)
                    ) {
                        viewModel.onCreateThemeClick()
                    }
                }
            }
        }

        // panels
        if (uiState.showCreateTopicPanel) {
            CreateThemePanel(
                onDismiss = { viewModel.onDismissCreateThemePanel() },
                onCreate = { title, desc, icon, nativeLang, targetLang ->
                    viewModel.createTheme(title, desc, icon, nativeLang, targetLang)
                }
            )
        }
        if (uiState.showCreateSubfolderPanel) {
            CreateSubfolderPanel(
                onDismiss = { viewModel.onDismissCreateSubfolderPanel() },
                onCreate = { title, desc -> viewModel.createSubfolder(title, desc) }
            )
        }
        if (uiState.showCreateWordPanel) {
            CreateWordPanel(
                targetLanguage = uiState.currentFolder?.targetLanguage ?: "en",
                onDismiss = { viewModel.onDismissCreateWordPanel() },
                onCreate = { word -> viewModel.createWord(word) }
            )
        }
        uiState.selectedWord?.let { word ->
            if (uiState.showWordDetailsPanel) {
                WordDetailsSheet(
                    word = word,
                    targetLanguage = uiState.currentFolder?.targetLanguage ?: "en",
                    onDismiss = { viewModel.onDismissWordDetails() },
                    onEdit = { viewModel.onEditWordFromDetails() }
                )
            }
        }
        if (uiState.showEditWordPanel && uiState.selectedWord != null) {
            EditWordPanel(
                word = uiState.selectedWord!!,
                targetLanguage = uiState.currentFolder?.targetLanguage ?: "en",
                onDismiss = { viewModel.onDismissEditWordPanel() },
                onSave = { viewModel.updateWord(it) }
            )
        }

        if (uiState.showEditTopicPanel && uiState.selectedFolderForEdit != null) {
            EditThemePanel(
                folder = uiState.selectedFolderForEdit!!,
                onDismiss = { viewModel.onDismissEditThemePanel() },
                onSave = { title, desc, icon ->
                    viewModel.updateTheme(title, desc, icon)
                }
            )
        }

        if (uiState.showMovePanel) {
            MoveToFolderSheet(
                destinations = uiState.moveDestinations,
                onDismiss = { viewModel.onDismissMovePanel() },
                onSelect = { viewModel.confirmMove(it) }
            )
        }

        if (uiState.showEditSubfolderPanel && uiState.selectedFolderForEdit != null) {
            EditSubfolderPanel(
                folder = uiState.selectedFolderForEdit!!,
                onDismiss = { viewModel.onDismissEditSubfolderPanel() },
                onSave = { title, desc -> viewModel.updateSubfolder(title, desc) }
            )
        }

        if (uiState.showSortPanel) {
            val currentOpt = when (uiState.sortCategory) {
                SortCategory.THEMES -> uiState.topicSortOption
                SortCategory.FOLDERS -> uiState.folderSortOption
                SortCategory.WORDS -> uiState.wordSortOption
            }
            SortBottomSheet(
                category = uiState.sortCategory,
                currentOption = currentOpt,
                onDismiss = { viewModel.onDismissSortPanel() },
                onOptionSelected = { option ->
                    viewModel.setSortOption(option)
                    coroutineScope.launch {
                        if (uiState.sortCategory == SortCategory.THEMES) {
                            gridState.scrollToItem(0)
                        }
                    }
                }
            )
        }
    }
}





