package com.rywent.langrid.presentation.screens.words

import androidx.compose.ui.graphics.vector.ImageVector
import com.rywent.langrid.presentation.screens.words.components.GeneralSortOption
import com.rywent.langrid.presentation.screens.words.components.SortCategory

data class WordItem(
    val id: String,
    val term: String,
    val translation: String? = null,
    val transcription: String? = null,
    val imageUrl: String? = null,
    val exampleSentence: String? = null,
    val exampleTranslation: String? = null,
    val partOfSpeech: String? = null,
    val isPinned: Boolean = false,
    val progress: Int = 0,
    val notes: String? = null
)

data class FolderNode(
    val id: String,
    val title: String,
    val description: String? = null,
    val icon: ImageVector? = null,
    val wordsCount: Int = 0,
    val updated: String = "updated today",
    val isPinned: Boolean = false,
    val subFolders: List<FolderNode> = emptyList(),
    val words: List<WordItem> = emptyList()
)

sealed interface SearchResultItem {
    data class FolderResult(val folder: FolderNode, val path: String) : SearchResultItem
    data class WordResult(val word: WordItem, val folder: FolderNode, val path: String) : SearchResultItem
}

data class WordsUiState(
    val searchQuery: String = "",
    val searchResults: List<SearchResultItem> = emptyList(),
    val isSearchActive: Boolean = false,
    val rootFolders: List<FolderNode> = emptyList(),
    val navigationStack: List<FolderNode> = emptyList(),
    val showCreateThemePanel: Boolean = false,
    val showCreateSubfolderPanel: Boolean = false,
    val showCreateWordPanel: Boolean = false,
    val themeSortOption: GeneralSortOption? = null,
    val folderSortOption: GeneralSortOption? = null,
    val wordSortOption: GeneralSortOption? = null,
    val showSortPanel: Boolean = false,
    val sortVersion: Int = 0,
    val sortCategory: SortCategory = SortCategory.THEMES
) {
    val currentFolder: FolderNode? get() = navigationStack.lastOrNull()
    val breadcrumbPath: List<String>
        get() = listOf("My Words") + navigationStack.map { it.title }
}