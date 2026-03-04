package org.monogram.presentation.profile.admin

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import org.monogram.domain.repository.ChatsListRepository
import org.monogram.domain.repository.UserRepository
import org.monogram.presentation.chatsScreen.currentChat.components.VideoPlayerPool
import org.monogram.presentation.root.AppComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class DefaultChatEditComponent(
    context: AppComponentContext,
    private val chatId: Long,
    private val chatsListRepository: ChatsListRepository = context.container.repositories.chatsListRepository,
    private val userRepository: UserRepository = context.container.repositories.userRepository,
    override val videoPlayerPool: VideoPlayerPool = context.container.utils.videoPlayerPool,
    private val onBackClicked: () -> Unit,
    private val onManageAdminsClicked: (Long) -> Unit,
    private val onManageMembersClicked: (Long) -> Unit,
    private val onManageBlacklistClicked: (Long) -> Unit,
    private val onManagePermissionsClicked: (Long) -> Unit
) : ChatEditComponent, AppComponentContext by context {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _state = MutableValue(ChatEditComponent.State(chatId = chatId))
    override val state: Value<ChatEditComponent.State> = _state

    init {
        loadChatData()
    }

    private fun loadChatData() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val chat = chatsListRepository.getChatById(chatId)
            val fullInfo = userRepository.getChatFullInfo(chatId)
            if (chat != null) {
                _state.value = _state.value.copy(
                    chat = chat,
                    title = chat.title,
                    description = fullInfo?.description ?: "",
                    username = chat.username ?: "",
                    isPublic = !chat.username.isNullOrEmpty(),
                    isForum = chat.isForum,
                    isTranslatable = if (chat.isChannel) chat.hasAutomaticTranslation else chat.isTranslatable,
                    avatarPath = chat.avatarPath,
                    canDelete = chat.isAdmin,
                    isLoading = false
                )
            }
        }
    }

    override fun onBack() = onBackClicked()

    override fun onUpdateTitle(title: String) {
        _state.value = _state.value.copy(title = title)
    }

    override fun onUpdateDescription(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    override fun onUpdateUsername(username: String) {
        _state.value = _state.value.copy(username = username)
    }

    override fun onTogglePublic(isPublic: Boolean) {
        _state.value = _state.value.copy(isPublic = isPublic)
    }

    override fun onToggleTopics(enabled: Boolean) {
        _state.value = _state.value.copy(isForum = enabled)
        scope.launch {
            chatsListRepository.toggleChatIsForum(chatId, enabled)
        }
    }

    override fun onToggleAutoTranslate(enabled: Boolean) {
        _state.value = _state.value.copy(isTranslatable = enabled)
        scope.launch {
            chatsListRepository.toggleChatIsTranslatable(chatId, enabled)
        }
    }

    override fun onChangeAvatar(path: String) {
        _state.value = _state.value.copy(avatarPath = path)
        scope.launch {
            chatsListRepository.setChatPhoto(chatId, path)
        }
    }

    override fun onSave() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val currentState = _state.value
            if (currentState.title != currentState.chat?.title) {
                chatsListRepository.setChatTitle(chatId, currentState.title)
            }
            val fullInfo = userRepository.getChatFullInfo(chatId)
            if (currentState.description != (fullInfo?.description ?: "")) {
                chatsListRepository.setChatDescription(chatId, currentState.description)
            }
            if (currentState.username != (currentState.chat?.username ?: "")) {
                chatsListRepository.setChatUsername(chatId, currentState.username)
            }
            onBackClicked()
        }
    }

    override fun onDeleteChat() {
        scope.launch {
            chatsListRepository.deleteChats(setOf(chatId))
            onBackClicked()
        }
    }

    override fun onManageAdmins() = onManageAdminsClicked(chatId)
    override fun onManageMembers() = onManageMembersClicked(chatId)
    override fun onManageBlacklist() = onManageBlacklistClicked(chatId)
    override fun onManagePermissions() = onManagePermissionsClicked(chatId)
}
