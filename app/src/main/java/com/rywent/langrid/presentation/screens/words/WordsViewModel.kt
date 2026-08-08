package com.rywent.langrid.presentation.screens.words

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rywent.langrid.data.repository.FolderRepository
import com.rywent.langrid.data.repository.WordRepository
import com.rywent.langrid.presentation.screens.words.components.GeneralSortOption
import com.rywent.langrid.presentation.screens.words.components.SortCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordsViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WordsUiState())
    val uiState: StateFlow<WordsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            folderRepository.observeFolderTree().collect { tree ->
                _uiState.update { state ->
                    val sortedRoots = applySortToFolders(tree, state.topicSortOption)
                    val withInnerSort = applyInnerSorts(sortedRoots, state)
                    state.copy(
                        isLoading = false,
                        rootFolders = withInnerSort,
                        navigationStack = rebuildStack(withInnerSort, state.navigationStack)
                    )
                }
            }
        }
    }

    private fun applyInnerSorts(
        roots: List<FolderNode>,
        state: WordsUiState
    ): List<FolderNode> {
        fun mapNode(node: FolderNode): FolderNode {
            val subs = applySortToFolders(
                node.subFolders.map { mapNode(it) },
                state.folderSortOption
            )
            val words = applySortToWords(node.words, state.wordSortOption)
            return node.copy(subFolders = subs, words = words)
        }
        return roots.map { mapNode(it) }
    }

    // search

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

    private fun performDeepSearch(
        folders: List<FolderNode>,
        query: String
    ): List<SearchResultItem> {
        val folderResults = mutableListOf<SearchResultItem.FolderResult>()
        val wordResults = mutableListOf<SearchResultItem.WordResult>()

        fun traverse(list: List<FolderNode>, path: List<String>) {
            for (folder in list) {
                val newPath = path + folder.title
                if (folder.title.contains(query, ignoreCase = true) ||
                    folder.description?.contains(query, ignoreCase = true) == true
                ) {
                    folderResults += SearchResultItem.FolderResult(
                        folder,
                        newPath.joinToString(" / ")
                    )
                }
                for (word in folder.words) {
                    if (word.term.contains(query, ignoreCase = true) ||
                        word.translation?.contains(query, ignoreCase = true) == true
                    ) {
                        wordResults += SearchResultItem.WordResult(
                            word,
                            folder,
                            newPath.joinToString(" / ")
                        )
                    }
                }
                if (folder.subFolders.isNotEmpty()) traverse(folder.subFolders, newPath)
            }
        }

        traverse(folders, emptyList())
        return folderResults + wordResults
    }

    // navigation

    fun navigateIntoFolder(folder: FolderNode) {
        _uiState.update {
            it.copy(
                navigationStack = it.navigationStack + folder,
                searchQuery = "",
                searchResults = emptyList(),
                isSearchActive = false
            )
        }
    }

    fun navigateToFolderByPath(targetFolderId: String) {
        val path = mutableListOf<FolderNode>()
        fun findPath(folders: List<FolderNode>, stack: List<FolderNode>): Boolean {
            for (f in folders) {
                if (f.id == targetFolderId) {
                    path.addAll(stack + f)
                    return true
                }
                if (f.subFolders.isNotEmpty() && findPath(f.subFolders, stack + f)) return true
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
            state.copy(
                navigationStack = if (index <= 0) emptyList()
                else state.navigationStack.take(index)
            )
        }
    }

    fun goBack() {
        _uiState.update { state ->
            if (state.navigationStack.isEmpty()) state
            else state.copy(navigationStack = state.navigationStack.dropLast(1))
        }
    }

    // move

    fun onMoveWord(wordId: String) {
        val current = _uiState.value.currentFolder ?: return
        val themeRoot = _uiState.value.navigationStack.firstOrNull() ?: current
        val destinations = flattenThemeFolders(themeRoot)
            .filter { it.folderId != current.id }
        _uiState.update {
            it.copy(
                showMovePanel = true,
                moveTarget = MoveTarget.Word(wordId, current.id),
                moveDestinations = destinations
            )
        }
    }

    fun onMoveSubfolder(folderId: String) {
        val current = _uiState.value.currentFolder ?: return
        val themeRoot = _uiState.value.navigationStack.firstOrNull() ?: current
        val forbidden = collectIds(findFolderById(listOf(themeRoot), folderId))
        val destinations = flattenThemeFolders(themeRoot)
            .filter { it.folderId !in forbidden }
        _uiState.update {
            it.copy(
                showMovePanel = true,
                moveTarget = MoveTarget.Folder(folderId, current.id),
                moveDestinations = destinations
            )
        }
    }

    fun onDismissMovePanel() {
        _uiState.update {
            it.copy(showMovePanel = false, moveTarget = null, moveDestinations = emptyList())
        }
    }

    fun confirmMove(destinationFolderId: String) {
        val target = _uiState.value.moveTarget ?: return
        viewModelScope.launch {
            when (target) {
                is MoveTarget.Word ->
                    wordRepository.move(target.wordId, destinationFolderId)
                is MoveTarget.Folder ->
                    folderRepository.move(target.folderId, destinationFolderId)
            }
            _uiState.update {
                it.copy(showMovePanel = false, moveTarget = null, moveDestinations = emptyList())
            }
        }
    }

    private fun flattenThemeFolders(themeRoot: FolderNode): List<MoveDestination> {
        val result = mutableListOf<MoveDestination>()

        fun walk(node: FolderNode, path: String) {
            result.add(
                MoveDestination(
                    folderId = node.id,
                    title = node.title,
                    path = path
                )
            )
            node.subFolders.forEach { subFolder ->
                walk(subFolder, "$path / ${subFolder.title}")
            }
        }

        walk(themeRoot, themeRoot.title)
        return result
    }
    private fun collectIds(folder: FolderNode?): Set<String> {
        if (folder == null) return emptySet()
        return setOf(folder.id) + folder.subFolders.flatMap { collectIds(it) }
    }


    // create theme

    fun onCreateThemeClick() {
        _uiState.update { it.copy(showCreateTopicPanel = true) }
    }

    fun onDismissCreateThemePanel() {
        _uiState.update { it.copy(showCreateTopicPanel = false) }
    }

    fun createTheme(
        title: String,
        description: String?,
        icon: ImageVector,
        nativeLanguage: String,
        targetLanguage: String
    ) {
        viewModelScope.launch {
            folderRepository.createTheme(
                title, description, icon, nativeLanguage, targetLanguage
            )
            _uiState.update { it.copy(showCreateTopicPanel = false) }
        }
    }

    // create subfolder

    fun onCreateSubfolderClick() {
        _uiState.update { it.copy(showCreateSubfolderPanel = true) }
    }

    fun onDismissCreateSubfolderPanel() {
        _uiState.update { it.copy(showCreateSubfolderPanel = false) }
    }

    fun createSubfolder(title: String, description: String?) {
        val parentId = _uiState.value.currentFolder?.id ?: return
        viewModelScope.launch {
            folderRepository.createSubfolder(parentId, title, description)
            _uiState.update { it.copy(showCreateSubfolderPanel = false) }
        }
    }

    // create word

    fun onCreateWordClick() {
        _uiState.update { it.copy(showCreateWordPanel = true) }
    }

    fun onDismissCreateWordPanel() {
        _uiState.update { it.copy(showCreateWordPanel = false) }
    }

    fun createWord(word: WordItem) {
        val folderId = _uiState.value.currentFolder?.id ?: return
        viewModelScope.launch {
            wordRepository.create(folderId, word)
            _uiState.update { it.copy(showCreateWordPanel = false) }
        }
    }

    // theme pin / delete / edit

    fun togglePinTheme(folderId: String) {
        viewModelScope.launch { folderRepository.togglePin(folderId) }
    }

    fun deleteTheme(folderId: String) {
        viewModelScope.launch {
            folderRepository.delete(folderId)
            _uiState.update { it.copy(sortVersion = it.sortVersion + 1) }
        }
    }

    fun onEditTheme(folderId: String) {
        val folder = _uiState.value.rootFolders.find { it.id == folderId } ?: return
        _uiState.update {
            it.copy(showEditTopicPanel = true, selectedFolderForEdit = folder)
        }
    }

    fun onDismissEditThemePanel() {
        _uiState.update {
            it.copy(showEditTopicPanel = false, selectedFolderForEdit = null)
        }
    }

    fun updateTheme(title: String, description: String?, icon: ImageVector) {
        val id = _uiState.value.selectedFolderForEdit?.id ?: return
        viewModelScope.launch {
            folderRepository.updateTheme(id, title, description, icon)
            _uiState.update {
                it.copy(showEditTopicPanel = false, selectedFolderForEdit = null)
            }
        }
    }

    // subfolder pin / delete / edit

    fun togglePinSubfolder(folderId: String) {
        viewModelScope.launch { folderRepository.togglePin(folderId) }
    }

    fun deleteSubfolder(folderId: String) {
        viewModelScope.launch { folderRepository.delete(folderId) }
    }

    fun onEditSubfolder(folderId: String) {
        val folder = findFolderById(_uiState.value.rootFolders, folderId) ?: return
        _uiState.update {
            it.copy(showEditSubfolderPanel = true, selectedFolderForEdit = folder)
        }
    }

    fun onDismissEditSubfolderPanel() {
        _uiState.update {
            it.copy(showEditSubfolderPanel = false, selectedFolderForEdit = null)
        }
    }

    fun updateSubfolder(title: String, description: String?) {
        val id = _uiState.value.selectedFolderForEdit?.id ?: return
        viewModelScope.launch {
            folderRepository.updateSubfolder(id, title, description)
            _uiState.update {
                it.copy(showEditSubfolderPanel = false, selectedFolderForEdit = null)
            }
        }
    }

    // word pin / delete / edit

    fun togglePinWord(wordId: String) {
        viewModelScope.launch { wordRepository.togglePin(wordId) }
    }

    fun deleteWord(wordId: String) {
        viewModelScope.launch { wordRepository.delete(wordId) }
    }

    fun onWordClick(word: WordItem) {
        _uiState.update {
            it.copy(showWordDetailsPanel = true, selectedWord = word)
        }
    }

    fun onDismissWordDetails() {
        _uiState.update {
            it.copy(showWordDetailsPanel = false, selectedWord = null)
        }
    }

    fun onEditWord(wordId: String) {
        val word = _uiState.value.currentFolder?.words?.find { it.id == wordId } ?: return
        _uiState.update {
            it.copy(
                selectedWord = word,
                showEditWordPanel = true,
                showWordDetailsPanel = false
            )
        }
    }

    fun onEditWordFromDetails() {
        _uiState.update {
            it.copy(showWordDetailsPanel = false, showEditWordPanel = true)
        }
    }

    fun onDismissEditWordPanel() {
        _uiState.update {
            it.copy(showEditWordPanel = false, selectedWord = null)
        }
    }

    fun updateWord(updated: WordItem) {
        val folderId = _uiState.value.currentFolder?.id ?: return
        viewModelScope.launch {
            wordRepository.update(folderId, updated)
            _uiState.update {
                it.copy(showEditWordPanel = false, selectedWord = null)
            }
        }
    }

    // sort

    fun onSortClick(category: SortCategory) {
        _uiState.update { it.copy(showSortPanel = true, sortCategory = category) }
    }

    fun onDismissSortPanel() {
        _uiState.update { it.copy(showSortPanel = false) }
    }

    fun setSortOption(option: GeneralSortOption?) {
        _uiState.update { state ->
            when (state.sortCategory) {
                SortCategory.THEMES -> state.copy(
                    topicSortOption = option,
                    rootFolders = applySortToFolders(state.rootFolders, option),
                    showSortPanel = false,
                    sortVersion = state.sortVersion + 1
                )
                SortCategory.FOLDERS -> {
                    val current = state.currentFolder ?: return@update state
                    val updatedSubs = applySortToFolders(current.subFolders, option)
                    val newRoots = updateFolderInTree(state.rootFolders, current.id) {
                        it.copy(subFolders = updatedSubs)
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
                    val newRoots = updateFolderInTree(state.rootFolders, current.id) {
                        it.copy(words = updatedWords)
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

    private fun applySortToWords(
        words: List<WordItem>,
        option: GeneralSortOption?
    ): List<WordItem> {
        if (option == null) return words.sortedByDescending { it.isPinned }
        val sorted = when (option) {
            GeneralSortOption.DATE_DESC -> words
            GeneralSortOption.DATE_ASC -> words.reversed()
            GeneralSortOption.NAME_ASC -> words.sortedBy { it.term.lowercase() }
            GeneralSortOption.NAME_DESC -> words.sortedByDescending { it.term.lowercase() }
            GeneralSortOption.TRANSLATION_ASC ->
                words.sortedBy { (it.translation ?: "").lowercase() }
            GeneralSortOption.COUNT_DESC,
            GeneralSortOption.COUNT_ASC -> words
        }
        return sorted.sortedByDescending { it.isPinned }
    }

    // tree helpers

    private fun updateFolderInTree(
        folders: List<FolderNode>,
        targetId: String,
        transform: (FolderNode) -> FolderNode
    ): List<FolderNode> = folders.map { folder ->
        when {
            folder.id == targetId -> transform(folder)
            folder.subFolders.isNotEmpty() -> folder.copy(
                subFolders = updateFolderInTree(folder.subFolders, targetId, transform)
            )
            else -> folder
        }
    }

    private fun findFolderById(folders: List<FolderNode>, id: String): FolderNode? {
        for (folder in folders) {
            if (folder.id == id) return folder
            findFolderById(folder.subFolders, id)?.let { return it }
        }
        return null
    }

    private fun rebuildStack(
        roots: List<FolderNode>,
        oldStack: List<FolderNode>
    ): List<FolderNode> = oldStack.mapNotNull { old -> findFolderById(roots, old.id) }
}