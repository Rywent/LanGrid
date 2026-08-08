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
    val nativeLanguage: String = "ru",
    val targetLanguage: String = "en",
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

sealed class MoveTarget {
    data class Word(val wordId: String, val currentFolderId: String) : MoveTarget()
    data class Folder(val folderId: String, val currentParentId: String?) : MoveTarget()
}

data class MoveDestination(
    val folderId: String,
    val title: String,
    val path: String
)

data class WordsUiState(
    val isLoading: Boolean = true,
    // search
    val searchQuery: String = "",
    val searchResults: List<SearchResultItem> = emptyList(),
    val isSearchActive: Boolean = false,
    val rootFolders: List<FolderNode> = emptyList(),
    val navigationStack: List<FolderNode> = emptyList(),

    // create panels
    val showCreateTopicPanel: Boolean = false,
    val showCreateSubfolderPanel: Boolean = false,
    val showCreateWordPanel: Boolean = false,

    // sort
    val topicSortOption: GeneralSortOption? = null,
    val folderSortOption: GeneralSortOption? = null,
    val wordSortOption: GeneralSortOption? = null,
    val showSortPanel: Boolean = false,
    val sortVersion: Int = 0,
    val sortCategory: SortCategory = SortCategory.THEMES,

    // edit panels
    val showEditTopicPanel: Boolean = false,
    val showEditSubfolderPanel: Boolean = false,
    val selectedFolderForEdit: FolderNode? = null,

    // word info and edit panels
    val showWordDetailsPanel: Boolean = false,
    val selectedWord: WordItem? = null,
    val showEditWordPanel: Boolean = false,

    // move
    val showMovePanel: Boolean = false,
    val moveTarget: MoveTarget? = null,
    val moveDestinations: List<MoveDestination> = emptyList()
) {
    val currentFolder: FolderNode? get() = navigationStack.lastOrNull()
    val breadcrumbPath: List<String>
        get() = listOf("My Words") + navigationStack.map { it.title }
}