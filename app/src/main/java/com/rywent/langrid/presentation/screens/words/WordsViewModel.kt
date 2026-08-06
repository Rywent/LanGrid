package com.rywent.langrid.presentation.screens.words

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import com.rywent.langrid.presentation.screens.words.components.GeneralSortOption
import com.rywent.langrid.presentation.screens.words.components.SortCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WordsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(WordsUiState())
    val uiState: StateFlow<WordsUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        val mockRootFolders = listOf(
            FolderNode(
                id = "1",
                title = "Home",
                description = "Everything about house, rooms, and furniture...",
                icon = Icons.Rounded.Home,
                subFolders = listOf(
                    FolderNode(
                        id = "1_1",
                        title = "Living Room",
                        description = "Hall interior and appliances",
                        subFolders = listOf(
                            FolderNode(
                                id = "1_1_1",
                                title = "Furniture",
                                description = "Sofas, chairs, tables",
                                words = listOf(
                                    WordItem(
                                        id = "w1",
                                        term = "Sofa",
                                        translation = "Диван",
                                        transcription = "/ˈsəʊ.fə/",
                                        exampleSentence = "She sat on the sofa and read a book.",
                                        exampleTranslation = "Она села на диван и читала книгу.",
                                        partOfSpeech = "noun",
                                        progress = 40
                                    ),
                                    WordItem(
                                        id = "w2",
                                        term = "Armchair",
                                        translation = "Кресло",
                                        transcription = "/ˈɑːm.tʃeər/",
                                        exampleSentence = "He fell asleep in the armchair.",
                                        exampleTranslation = "Он заснул в кресле.",
                                        partOfSpeech = "noun",
                                        progress = 15
                                    ),
                                    WordItem(
                                        id = "w3",
                                        term = "Shelf",
                                        translation = "Полка",
                                        transcription = "/ʃelf/",
                                        exampleSentence = "Put the books on the shelf.",
                                        progress = 70
                                    )
                                )
                            )
                        )
                    ),
                    FolderNode(id = "1_2", title = "Kitchen")
                )
            ),
            FolderNode(
                id = "2",
                title = "Traveling",
                description = "Airports, flights, hotels and bookings...",
                icon = Icons.Rounded.AirplanemodeActive,
                words = (1..10).map {
                    WordItem(id = "tr_$it", term = "Airport $it", translation = "Аэропорт $it")
                }
            ),

            FolderNode(
                id = "3",
                title = "Food & Cooking",
                description = "Restaurants, dishes and ingredients...",
                icon = Icons.Rounded.Restaurant
            ),

        )

        val calculatedFolders = recalculateTreeCounts(mockRootFolders)
        _uiState.update { it.copy(rootFolders = calculatedFolders) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            if (query.isBlank()) {
                state.copy(searchQuery = "", searchResults = emptyList(), isSearchActive = false)
            } else {
                val results = performDeepSearch(state.rootFolders, query.trim())
                state.copy(searchQuery = query, searchResults = results, isSearchActive = true)
            }
        }
    }

    private fun performDeepSearch(folders: List<FolderNode>, query: String): List<SearchResultItem> {
        val folderResults = mutableListOf<SearchResultItem.FolderResult>()
        val wordResults = mutableListOf<SearchResultItem.WordResult>()

        fun traverse(currentList: List<FolderNode>, currentPath: List<String>) {
            for (folder in currentList) {
                val newPath = currentPath + folder.title
                if (folder.title.contains(query, ignoreCase = true) ||
                    (folder.description?.contains(query, ignoreCase = true) == true)) {
                    folderResults.add(SearchResultItem.FolderResult(folder, newPath.joinToString(" / ")))
                }
                for (word in folder.words) {
                    if (word.term.contains(query, ignoreCase = true) ||
                        (word.translation?.contains(query, ignoreCase = true) == true)) {
                        wordResults.add(SearchResultItem.WordResult(word, folder, newPath.joinToString(" / ")))
                    }
                }
                if (folder.subFolders.isNotEmpty()) {
                    traverse(folder.subFolders, newPath)
                }
            }
        }

        traverse(folders, emptyList())
        return folderResults + wordResults
    }

    fun navigateIntoFolder(folder: FolderNode) {
        _uiState.update { state ->
            state.copy(
                navigationStack = state.navigationStack + folder,
                searchQuery = "",
                searchResults = emptyList(),
                isSearchActive = false
            )
        }
    }

    fun navigateToFolderByPath(targetFolderId: String) {
        val path = mutableListOf<FolderNode>()
        fun findPath(folders: List<FolderNode>, currentStack: List<FolderNode>): Boolean {
            for (f in folders) {
                if (f.id == targetFolderId) {
                    path.addAll(currentStack + f)
                    return true
                }
                if (f.subFolders.isNotEmpty()) {
                    if (findPath(f.subFolders, currentStack + f)) return true
                }
            }
            return false
        }

        if (findPath(_uiState.value.rootFolders, emptyList())) {
            _uiState.update {
                it.copy(
                    navigationStack = path,
                    searchQuery = "",
                    searchResults = emptyList(),
                    isSearchActive = false
                )
            }
        }
    }

    fun popToFolderIndex(index: Int) {
        _uiState.update { state ->
            val newStack = if (index <= 0) emptyList() else state.navigationStack.take(index)
            state.copy(navigationStack = newStack)
        }
    }

    fun goBack() {
        _uiState.update { state ->
            if (state.navigationStack.isNotEmpty()) {
                state.copy(navigationStack = state.navigationStack.dropLast(1))
            } else {
                state
            }
        }
    }

    // Panels and creation
    fun onCreateThemeClick() { _uiState.update { it.copy(showCreateThemePanel = true) } }
    fun onDismissCreateThemePanel() { _uiState.update { it.copy(showCreateThemePanel = false) } }

    fun createTheme(title: String, description: String?, icon: ImageVector) {
        val folder = FolderNode(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description?.trim()?.ifBlank { null },
            icon = icon,
            updated = "updated today"
        )
        _uiState.update {
            val newRoots = recalculateTreeCounts(it.rootFolders + folder)
            it.copy(rootFolders = newRoots, showCreateThemePanel = false)
        }
    }

    fun onCreateSubfolderClick() { _uiState.update { it.copy(showCreateSubfolderPanel = true) } }
    fun onDismissCreateSubfolderPanel() { _uiState.update { it.copy(showCreateSubfolderPanel = false) } }

    fun createSubfolder(title: String, description: String?) {
        val current = _uiState.value.currentFolder ?: return
        val newFolder = FolderNode(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description?.trim()?.ifBlank { null },
            updated = "updated today"
        )
        _uiState.update { state ->
            val newRoots = updateFolderInTree(state.rootFolders, current.id) { folder ->
                folder.copy(subFolders = folder.subFolders + newFolder)
            }
            val calculatedRoots = recalculateTreeCounts(newRoots)
            state.copy(
                rootFolders = calculatedRoots,
                navigationStack = rebuildStack(calculatedRoots, state.navigationStack),
                showCreateSubfolderPanel = false
            )
        }
    }

    fun onCreateWordClick() { _uiState.update { it.copy(showCreateWordPanel = true) } }
    fun onDismissCreateWordPanel() { _uiState.update { it.copy(showCreateWordPanel = false) } }

    fun createWord(word: WordItem) {
        val current = _uiState.value.currentFolder ?: return
        _uiState.update { state ->
            val newRoots = updateFolderInTree(state.rootFolders, current.id) { folder ->
                folder.copy(words = folder.words + word)
            }
            val calculatedRoots = recalculateTreeCounts(newRoots)
            state.copy(
                rootFolders = calculatedRoots,
                navigationStack = rebuildStack(calculatedRoots, state.navigationStack),
                showCreateWordPanel = false
            )
        }
    }

    // pin and edit

    fun togglePinTheme(folderId: String) {
        _uiState.update { state ->
            val newRoots = state.rootFolders
                .map { folder ->
                    if (folder.id == folderId) folder.copy(isPinned = !folder.isPinned)
                    else folder
                }
                .let { applySortToFolders(it, state.themeSortOption) }
            state.copy(rootFolders = newRoots)
        }
    }

    fun deleteTheme(folderId: String) {
        _uiState.update { state ->
            state.copy(
                rootFolders = state.rootFolders.filter { it.id != folderId },
                sortVersion = state.sortVersion + 1
            )
        }
    }

    fun onEditTheme(folderId: String) {
        // TODO
    }


    // folders and words

    fun togglePinSubfolder(folderId: String) {
        _uiState.update { state ->
            val current = state.currentFolder ?: return@update state
            val updatedSubFolders = applySortToFolders(
                current.subFolders.map {
                    if (it.id == folderId) it.copy(isPinned = !it.isPinned) else it
                },
                state.folderSortOption
            )
            val newRoots = updateFolderInTree(state.rootFolders, current.id) { folder ->
                folder.copy(subFolders = updatedSubFolders)
            }
            state.copy(
                rootFolders = newRoots,
                navigationStack = rebuildStack(newRoots, state.navigationStack)
            )
        }
    }

    fun deleteSubfolder(folderId: String) {
        _uiState.update { state ->
            val current = state.currentFolder ?: return@update state
            val updatedSubFolders = current.subFolders.filter { it.id != folderId }
            val newRoots = updateFolderInTree(state.rootFolders, current.id) { folder ->
                folder.copy(subFolders = updatedSubFolders)
            }
            val calculatedRoots = recalculateTreeCounts(newRoots)
            state.copy(
                rootFolders = calculatedRoots,
                navigationStack = rebuildStack(calculatedRoots, state.navigationStack)
            )
        }
    }

    fun togglePinWord(wordId: String) {
        _uiState.update { state ->
            val current = state.currentFolder ?: return@update state
            val updatedWords = applySortToWords(
                current.words.map {
                    if (it.id == wordId) it.copy(isPinned = !it.isPinned) else it
                },
                state.wordSortOption
            )
            val newRoots = updateFolderInTree(state.rootFolders, current.id) { folder ->
                folder.copy(words = updatedWords)
            }
            state.copy(
                rootFolders = newRoots,
                navigationStack = rebuildStack(newRoots, state.navigationStack)
            )
        }
    }

    fun deleteWord(wordId: String) {
        _uiState.update { state ->
            val current = state.currentFolder ?: return@update state
            val updatedWords = current.words.filter { it.id != wordId }
            val newRoots = updateFolderInTree(state.rootFolders, current.id) { folder ->
                folder.copy(words = updatedWords)
            }
            val calculatedRoots = recalculateTreeCounts(newRoots)
            state.copy(
                rootFolders = calculatedRoots,
                navigationStack = rebuildStack(calculatedRoots, state.navigationStack)
            )
        }
    }


    // search

    fun onSortClick(category: SortCategory) {
        _uiState.update { it.copy(showSortPanel = true, sortCategory = category) }
    }

    fun onDismissSortPanel() {
        _uiState.update { it.copy(showSortPanel = false) }
    }

    fun setSortOption(option: GeneralSortOption?) {
        _uiState.update { state ->
            when (state.sortCategory) {
                SortCategory.THEMES -> {
                    state.copy(
                        themeSortOption = option,
                        rootFolders = applySortToFolders(state.rootFolders, option),
                        showSortPanel = false,
                        sortVersion = state.sortVersion + 1
                    )
                }
                SortCategory.FOLDERS -> {
                    val current = state.currentFolder ?: return@update state
                    val updatedSubFolders = applySortToFolders(current.subFolders, option)
                    val newRoots = updateFolderInTree(state.rootFolders, current.id) { folder ->
                        folder.copy(subFolders = updatedSubFolders)
                    }
                    state.copy(
                        folderSortOption = option,
                        rootFolders = newRoots,
                        navigationStack = rebuildStack(newRoots, state.navigationStack),
                        showSortPanel = false,
                        sortVersion = state.sortVersion + 1
                    )
                }
                SortCategory.WORDS -> {
                    val current = state.currentFolder ?: return@update state
                    val updatedWords = applySortToWords(current.words, option)
                    val newRoots = updateFolderInTree(state.rootFolders, current.id) { folder ->
                        folder.copy(words = updatedWords)
                    }
                    state.copy(
                        wordSortOption = option,
                        rootFolders = newRoots,
                        navigationStack = rebuildStack(newRoots, state.navigationStack),
                        showSortPanel = false,
                        sortVersion = state.sortVersion + 1
                    )
                }
            }
        }
    }

    private fun applySortToFolders(
        folders: List<FolderNode>,
        option: GeneralSortOption?
    ): List<FolderNode> {
        val sorted = when (option) {
            null -> folders
            GeneralSortOption.DATE_DESC -> folders
            GeneralSortOption.DATE_ASC -> folders.reversed()
            GeneralSortOption.COUNT_DESC -> folders.sortedByDescending { it.wordsCount }
            GeneralSortOption.COUNT_ASC -> folders.sortedBy { it.wordsCount }
            GeneralSortOption.NAME_ASC -> folders.sortedBy { it.title.lowercase() }
            GeneralSortOption.NAME_DESC -> folders.sortedByDescending { it.title.lowercase() }
            GeneralSortOption.TRANSLATION_ASC -> folders
        }
        return sorted.sortedByDescending { it.isPinned }
    }

    private fun applySortToWords(words: List<WordItem>, option: GeneralSortOption?): List<WordItem> {
        if (option == null) return words
        val sorted = when (option) {
            GeneralSortOption.DATE_DESC -> words
            GeneralSortOption.DATE_ASC -> words.reversed()
            GeneralSortOption.NAME_ASC -> words.sortedBy { it.term.lowercase() }
            GeneralSortOption.NAME_DESC -> words.sortedByDescending { it.term.lowercase() }
            GeneralSortOption.TRANSLATION_ASC -> words.sortedBy { (it.translation ?: "").lowercase() }
            GeneralSortOption.COUNT_DESC,
            GeneralSortOption.COUNT_ASC -> words
        }
        return sorted.sortedByDescending { it.isPinned }
    }



    // calculations
    private fun recalculateTreeCounts(folders: List<FolderNode>): List<FolderNode> {
        return folders.map { folder ->
            val subRec = recalculateTreeCounts(folder.subFolders)
            val totalWords = folder.words.size + subRec.sumOf { it.wordsCount }
            folder.copy(
                subFolders = subRec,
                wordsCount = totalWords
            )
        }
    }

    private fun updateFolderInTree(
        folders: List<FolderNode>,
        targetId: String,
        transform: (FolderNode) -> FolderNode
    ): List<FolderNode> {
        return folders.map { folder ->
            when {
                folder.id == targetId -> transform(folder)
                folder.subFolders.isNotEmpty() -> folder.copy(
                    subFolders = updateFolderInTree(folder.subFolders, targetId, transform)
                )
                else -> folder
            }
        }
    }

    private fun findFolderById(folders: List<FolderNode>, id: String): FolderNode? {
        for (folder in folders) {
            if (folder.id == id) return folder
            findFolderById(folder.subFolders, id)?.let { return it }
        }
        return null
    }

    private fun rebuildStack(roots: List<FolderNode>, oldStack: List<FolderNode>): List<FolderNode> {
        return oldStack.mapNotNull { old -> findFolderById(roots, old.id) }
    }
}