package org.monogram.presentation.chatsScreen.chatList

import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.monogram.domain.models.BotMenuButtonModel
import org.monogram.domain.models.UpdateState
import org.monogram.domain.repository.*
import org.monogram.presentation.chatsScreen.ChatListComponent
import org.monogram.presentation.chatsScreen.currentChat.components.VideoPlayerPool
import org.monogram.presentation.root.AppComponentContext
import org.monogram.presentation.util.AppPreferences

class DefaultChatListComponent(
    context: AppComponentContext,
    private val repository: ChatsListRepository = context.container.repositories.chatsListRepository,
    private val repositoryUser: UserRepository = context.container.repositories.userRepository,
    private val settingsRepository: SettingsRepository = context.container.repositories.settingsRepository,
    private val externalProxyRepository: ExternalProxyRepository = context.container.repositories.externalProxyRepository,
    private val updateRepository: UpdateRepository = context.container.repositories.updateRepository,
    override val appPreferences: AppPreferences = context.container.preferences.appPreferences,
    override val videoPlayerPool: VideoPlayerPool = context.container.utils.videoPlayerPool,
    private val onSelect: (Long, Long?) -> Unit,
    private val onProfileSelect: (Long) -> Unit,
    private val onSettingsClick: () -> Unit,
    private val onProxySettingsClick: () -> Unit,
    private val onConfirmForward: (Set<Long>) -> Unit = {},
    private val isForwarding: Boolean = false,
    private val onNewChatClick: () -> Unit = {},
    activeChatId: Value<Long>
) : ChatListComponent, AppComponentContext by context {

    private val _state = MutableStateFlow(ChatListComponent.State(isForwarding = isForwarding))
    override val state: StateFlow<ChatListComponent.State> = _state

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private var searchJob: Job? = null
    private var isFetchingMoreMessages = false
    private var nextMessagesOffset = ""

    init {
        activeChatId.subscribe { id ->
            _state.update { it.copy(activeChatId = id) }
        }

        lifecycle.doOnDestroy {
            scope.cancel()
        }

        scope.launch {
            repositoryUser.currentUserFlow
                .collect { user ->
                    if (user != null) {
                        _state.update { it.copy(currentUser = user) }
                    }
                }
        }

        scope.launch {
            repositoryUser.getMe()
        }

        scope.launch {
            repository.chatListFlow
                .collect { list ->
                    val distinctList = list.distinctBy { it.id }
                    _state.update {
                        val newChatsByFolder = it.chatsByFolder.toMutableMap()
                        newChatsByFolder[it.selectedFolderId] = distinctList
                        it.copy(chatsByFolder = newChatsByFolder)
                    }
                }
        }

        scope.launch {
            repository.foldersFlow
                .collect { folders ->
                    _state.update { it.copy(folders = folders) }
                }
        }

        scope.launch {
            repository.isLoadingFlow
                .collect { isLoading ->
                    _state.update {
                        val newLoadingByFolder = it.isLoadingByFolder.toMutableMap()
                        newLoadingByFolder[it.selectedFolderId] = isLoading
                        it.copy(isLoadingByFolder = newLoadingByFolder)
                    }
                }
        }

        scope.launch {
            repository.connectionStateFlow.collect { status ->
                _state.update { it.copy(connectionStatus = status) }
                updateProxyStatus()
            }
        }

        scope.launch {
            repository.isArchivePinned.collect { isPinned ->
                _state.update { it.copy(isArchivePinned = isPinned) }
            }
        }

        scope.launch {
            repository.isArchiveAlwaysVisible.collect { alwaysVisible ->
                _state.update { it.copy(isArchiveAlwaysVisible = alwaysVisible) }
            }
        }

        scope.launch {
            repository.searchHistory.collect { history ->
                _state.update { it.copy(searchHistory = history) }
            }
        }

        scope.launch {
            while (isActive) {
                updateProxyStatus()
                delay(5000)
            }
        }

        scope.launch {
            settingsRepository.getAttachMenuBots().collect { bots ->
                _state.update { it.copy(attachMenuBots = bots) }

                bots.firstOrNull()?.let { bot ->
                    if (bot.botUserId != 0L) {
                        val botInfo = repositoryUser.getBotInfo(bot.botUserId)
                        val menuButton = botInfo?.menuButton
                        if (menuButton is BotMenuButtonModel.WebApp) {
                            _state.update {
                                it.copy(
                                    botWebAppUrl = menuButton.url,
                                    botWebAppName = menuButton.text
                                )
                            }
                        }
                    }
                }
            }
        }

        scope.launch {
            updateRepository.updateState.collect { updateState ->
                _state.update { it.copy(updateState = updateState) }
            }
        }

        scope.launch {
            updateRepository.checkForUpdates()
        }

        loadMore()
    }

    private suspend fun updateProxyStatus() {
        val proxies = externalProxyRepository.getProxies()
        val isProxyEnabled = proxies.any { it.isEnabled }
        _state.update { it.copy(isProxyEnabled = isProxyEnabled) }
    }

    override fun retryConnection() {
        repository.retryConnection()
    }

    override fun onFolderClicked(id: Int) {
        if (_state.value.selectedFolderId == id) return

        _state.update { it.copy(selectedFolderId = id) }

        scope.launch(Dispatchers.IO) {
            repository.selectFolder(id)
        }
    }

    override fun loadMore(folderId: Int?) {
        val targetFolderId = folderId ?: _state.value.selectedFolderId
        if (_state.value.isLoadingByFolder[targetFolderId] == true) return

        scope.launch(Dispatchers.IO) {
            if (folderId != null && folderId != _state.value.selectedFolderId) {
                return@launch
            }
            repository.loadNextChunk(20)
        }
    }

    override fun loadMoreMessages() {
        if (isFetchingMoreMessages || nextMessagesOffset.isEmpty()) return

        isFetchingMoreMessages = true
        val query = _state.value.searchQuery
        scope.launch(Dispatchers.IO) {
            val result = repository.searchMessages(query, offset = nextMessagesOffset)
            nextMessagesOffset = result.nextOffset
            _state.update {
                it.copy(
                    messageSearchResults = it.messageSearchResults + result.messages,
                    canLoadMoreMessages = nextMessagesOffset.isNotEmpty()
                )
            }
            isFetchingMoreMessages = false
        }
    }

    override fun onChatClicked(id: Long) {
        if (_state.value.isForwarding) {
            toggleSelection(id)
        } else if (_state.value.selectedChatIds.isNotEmpty()) {
            toggleSelection(id)
        } else {
            if (_state.value.isSearchActive) {
                repository.addSearchChatId(id)
            }
            onSelect(id, null)
        }
    }

    override fun onProfileClicked(id: Long) {
        onProfileSelect(id)
    }

    override fun onMessageClicked(chatId: Long, messageId: Long) {
        if (_state.value.isForwarding) {
            toggleSelection(chatId)
        } else if (_state.value.selectedChatIds.isNotEmpty()) {
            toggleSelection(chatId)
        } else {
            if (_state.value.isSearchActive) {
                repository.addSearchChatId(chatId)
            }
            onSelect(chatId, messageId)
        }
    }

    override fun onChatLongClicked(id: Long) {
        toggleSelection(id)
    }

    override fun clearSelection() {
        _state.update { it.copy(selectedChatIds = emptySet()) }
    }

    override fun onSettingsClicked() {
        onSettingsClick()
    }

    override fun onSearchToggle() {
        _state.update {
            it.copy(
                isSearchActive = !it.isSearchActive,
                searchQuery = "",
                searchResults = emptyList(),
                globalSearchResults = emptyList(),
                messageSearchResults = emptyList(),
                canLoadMoreMessages = false
            )
        }
        nextMessagesOffset = ""
    }

    override fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }

        searchJob?.cancel()
        searchJob = scope.launch(Dispatchers.IO) {
            delay(300)
            if (query.isNotEmpty()) {
                val localResults = repository.searchChats(query)
                _state.update { it.copy(searchResults = localResults) }

                val globalResults = repository.searchPublicChats(query)
                _state.update { it.copy(globalSearchResults = globalResults) }

                val messageResults = repository.searchMessages(query)
                nextMessagesOffset = messageResults.nextOffset
                _state.update {
                    it.copy(
                        messageSearchResults = messageResults.messages,
                        canLoadMoreMessages = nextMessagesOffset.isNotEmpty()
                    )
                }
            } else {
                nextMessagesOffset = ""
                _state.update {
                    it.copy(
                        searchResults = emptyList(),
                        globalSearchResults = emptyList(),
                        messageSearchResults = emptyList(),
                        canLoadMoreMessages = false
                    )
                }
            }
        }
    }

    override fun onClearSearchHistory() {
        repository.clearSearchHistory()
    }

    override fun onRemoveSearchHistoryItem(chatId: Long) {
        repository.removeSearchChatId(chatId)
    }

    override fun onMuteSelected(mute: Boolean) {
        val selectedIds = _state.value.selectedChatIds
        val selectedChats = _state.value.chats.filter { selectedIds.contains(it.id) }
        val shouldMute = selectedChats.any { !it.isMuted }

        scope.launch(Dispatchers.IO) {
            repository.toggleMuteChats(selectedIds, shouldMute)
            clearSelection()
        }
    }

    override fun onArchiveSelected(archive: Boolean) {
        val selectedIds = _state.value.selectedChatIds
        scope.launch(Dispatchers.IO) {
            repository.toggleArchiveChats(selectedIds, archive)
            clearSelection()
        }
    }

    override fun onDeleteSelected() {
        val selectedIds = _state.value.selectedChatIds
        scope.launch(Dispatchers.IO) {
            repository.deleteChats(selectedIds)
            clearSelection()
        }
    }

    override fun onArchivePinToggle() {
        repository.setArchivePinned(!_state.value.isArchivePinned)
    }

    override fun onConfirmForwarding() {
        if (_state.value.selectedChatIds.isNotEmpty()) {
            onConfirmForward(_state.value.selectedChatIds)
        }
    }

    override fun onNewChatClicked() {
        onNewChatClick()
    }

    override fun onProxySettingsClicked() {
        onProxySettingsClick()
    }

    override fun onOpenInstantView(url: String) {
        _state.update { it.copy(instantViewUrl = url) }
    }

    override fun onDismissInstantView() {
        _state.update { it.copy(instantViewUrl = null) }
    }

    override fun onOpenWebApp(url: String, botUserId: Long, botName: String) {
        _state.update {
            it.copy(
                webAppUrl = url,
                webAppBotId = botUserId,
                webAppBotName = botName
            )
        }
    }

    override fun onDismissWebApp() {
        _state.update {
            it.copy(
                webAppUrl = null,
                webAppBotId = null,
                webAppBotName = null
            )
        }
    }

    override fun onOpenWebView(url: String) {
        _state.update { it.copy(webViewUrl = url) }
    }

    override fun onDismissWebView() {
        _state.update { it.copy(webViewUrl = null) }
    }

    override fun onUpdateClicked() {
        val currentState = _state.value.updateState
        when (currentState) {
            is UpdateState.UpdateAvailable -> {
                updateRepository.downloadUpdate()
            }

            is UpdateState.ReadyToInstall -> {
                updateRepository.installUpdate()
            }

            is UpdateState.Downloading -> {
                updateRepository.cancelDownload()
            }

            else -> {
                onSettingsClick()
            }
        }
    }

    override fun handleBack(): Boolean {
        return when {
            state.value.webViewUrl != null -> {
                onDismissWebView()
                true
            }
            state.value.webAppUrl != null -> {
                onDismissWebApp()
                true
            }
            state.value.instantViewUrl != null -> {
                onDismissInstantView()
                true
            }
            state.value.isSearchActive -> {
                onSearchToggle()
                true
            }
            state.value.selectedChatIds.isNotEmpty() -> {
                clearSelection()
                true
            }
            state.value.selectedFolderId == -2 -> {
                onFolderClicked(-1)
                true
            }

            state.value.isForwarding -> {
                onSelect(0L, null)
                true
            }
            else -> false
        }
    }

    override fun updateScrollPosition(folderId: Int, index: Int, offset: Int) {
        _state.update {
            val newPositions = it.scrollPositions.toMutableMap()
            newPositions[folderId] = index to offset
            it.copy(scrollPositions = newPositions)
        }
    }

    private fun toggleSelection(id: Long) {
        _state.update { state ->
            val newSelection = if (state.selectedChatIds.contains(id)) {
                state.selectedChatIds - id
            } else {
                state.selectedChatIds + id
            }
            state.copy(selectedChatIds = newSelection)
        }
    }
}
