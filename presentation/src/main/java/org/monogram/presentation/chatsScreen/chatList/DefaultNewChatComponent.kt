package org.monogram.presentation.chatsScreen.chatList

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import org.monogram.presentation.chatsScreen.NewChatComponent
import org.monogram.domain.repository.ChatsListRepository
import org.monogram.domain.repository.UserRepository
import org.monogram.presentation.chatsScreen.currentChat.components.VideoPlayerPool
import org.monogram.presentation.root.AppComponentContext
import kotlinx.coroutines.*

class DefaultNewChatComponent(
    context: AppComponentContext,
    private val userRepository: UserRepository = context.container.repositories.userRepository,
    private val chatsListRepository: ChatsListRepository = context.container.repositories.chatsListRepository,
    override val videoPlayerPool: VideoPlayerPool = context.container.utils.videoPlayerPool,
    private val onBackClicked: () -> Unit,
    private val onChatCreated: (Long) -> Unit
) : NewChatComponent, AppComponentContext by context {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _state = MutableValue(NewChatComponent.State())
    override val state: Value<NewChatComponent.State> = _state

    private var searchJob: Job? = null

    init {
        loadContacts()
    }

    private fun loadContacts() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            val contacts = userRepository.getContacts()
            _state.update { it.copy(contacts = contacts, isLoading = false) }
        }
    }

    override fun onBack() {
        onBackClicked()
    }

    override fun onUserClicked(userId: Long) {
        if (_state.value.step == NewChatComponent.Step.CONTACTS) {
            onChatCreated(userId)
        }
    }

    override fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(300)
            if (query.isNotEmpty()) {
                val results = userRepository.searchContacts(query)
                _state.update { it.copy(searchResults = results) }
            } else {
                _state.update { it.copy(searchResults = emptyList()) }
            }
        }
    }

    override fun onCreateGroup() {
        _state.update { it.copy(step = NewChatComponent.Step.GROUP_INFO) }
    }

    override fun onCreateChannel() {
        _state.update { it.copy(step = NewChatComponent.Step.CHANNEL_INFO) }
    }

    override fun onTitleChange(title: String) {
        _state.update { it.copy(title = title) }
    }

    override fun onDescriptionChange(description: String) {
        _state.update { it.copy(description = description) }
    }

    override fun onPhotoSelected(path: String?) {
        _state.update { it.copy(photoPath = path) }
    }

    override fun onToggleUserSelection(userId: Long) {
        _state.update {
            val newSelected = if (it.selectedUserIds.contains(userId)) {
                it.selectedUserIds - userId
            } else {
                it.selectedUserIds + userId
            }
            it.copy(selectedUserIds = newSelected)
        }
    }

    override fun onAutoDeleteTimeChange(seconds: Int) {
        _state.update { it.copy(autoDeleteTime = seconds) }
    }

    override fun onConfirmCreate() {
        val currentState = _state.value
        when (currentState.step) {
            NewChatComponent.Step.GROUP_MEMBERS -> {
                if (currentState.selectedUserIds.isNotEmpty()) {
                    _state.update { it.copy(step = NewChatComponent.Step.GROUP_INFO) }
                }
            }

            NewChatComponent.Step.GROUP_INFO -> {
                if (currentState.title.isNotBlank()) {
                    scope.launch {
                        val chatId = chatsListRepository.createGroup(
                            currentState.title,
                            currentState.selectedUserIds.toList(),
                            currentState.autoDeleteTime
                        )
                        if (chatId != 0L) {
                            if (currentState.photoPath != null) {
                                chatsListRepository.setChatPhoto(chatId, currentState.photoPath.toString())
                            }
                            onChatCreated(chatId)
                        }
                    }
                }
            }

            NewChatComponent.Step.CHANNEL_INFO -> {
                if (currentState.title.isNotBlank()) {
                    scope.launch {
                        val chatId = chatsListRepository.createChannel(
                            currentState.title,
                            currentState.description,
                            messageAutoDeleteTime = currentState.autoDeleteTime
                        )
                        if (chatId != 0L) {
                            if (currentState.photoPath != null) {
                                chatsListRepository.setChatPhoto(chatId, currentState.photoPath.toString())
                            }
                            onChatCreated(chatId)
                        }
                    }
                }
            }

            else -> {}
        }
    }

    override fun onStepBack() {
        _state.update {
            when (it.step) {
                NewChatComponent.Step.GROUP_MEMBERS -> it.copy(
                    step = NewChatComponent.Step.CONTACTS,
                    selectedUserIds = emptySet()
                )

                NewChatComponent.Step.GROUP_INFO -> it.copy(step = NewChatComponent.Step.CONTACTS)
                NewChatComponent.Step.CHANNEL_INFO -> it.copy(step = NewChatComponent.Step.CONTACTS)
                else -> it
            }
        }
    }
}
