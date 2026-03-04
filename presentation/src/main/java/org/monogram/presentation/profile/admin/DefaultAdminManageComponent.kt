package org.monogram.presentation.profile.admin

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import org.monogram.domain.repository.ChatMemberStatus
import org.monogram.domain.repository.ChatsListRepository
import org.monogram.domain.repository.UserRepository
import org.monogram.presentation.chatsScreen.currentChat.components.VideoPlayerPool
import org.monogram.presentation.root.AppComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class DefaultAdminManageComponent(
    context: AppComponentContext,
    private val chatId: Long,
    private val userId: Long,
    private val userRepository: UserRepository = context.container.repositories.userRepository,
    private val chatsListRepository: ChatsListRepository = context.container.repositories.chatsListRepository,
    override val videoPlayerPool: VideoPlayerPool = context.container.utils.videoPlayerPool,
    private val onBackClicked: () -> Unit
) : AdminManageComponent, AppComponentContext by context {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _state = MutableValue(AdminManageComponent.State(chatId = chatId, userId = userId))
    override val state: Value<AdminManageComponent.State> = _state

    init {
        loadMember()
    }

    private fun loadMember() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val chat = chatsListRepository.getChatById(chatId)
                val member = userRepository.getChatMember(chatId, userId)
                val initialStatus = when (val status = member?.status) {
                    is ChatMemberStatus.Administrator -> status
                    is ChatMemberStatus.Creator -> ChatMemberStatus.Administrator(
                        customTitle = member.rank ?: ""
                    )

                    else -> ChatMemberStatus.Administrator(
                        customTitle = member?.rank ?: "",
                        canManageChat = false,
                        canChangeInfo = false,
                        canPostMessages = false,
                        canEditMessages = false,
                        canDeleteMessages = false,
                        canInviteUsers = false,
                        canRestrictMembers = false,
                        canPinMessages = false,
                        canPromoteMembers = false,
                        canManageVideoChats = false,
                        canManageTopics = false,
                        canPostStories = false,
                        canEditStories = false,
                        canDeleteStories = false,
                        canManageDirectMessages = false,
                        isAnonymous = false
                    )
                }
                _state.value = _state.value.copy(
                    member = member,
                    currentStatus = initialStatus,
                    isChannel = chat?.isChannel == true
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    override fun onBack() {
        onBackClicked()
    }

    override fun onSave() {
        val status = _state.value.currentStatus ?: return
        _state.value = _state.value.copy(isLoading = true)
        scope.launch {
            try {
                userRepository.setChatMemberStatus(chatId, userId, status)
                onBackClicked()
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    override fun onTogglePermission(permission: AdminManageComponent.Permission) {
        val current = _state.value.currentStatus as? ChatMemberStatus.Administrator ?: return
        val updated = when (permission) {
            AdminManageComponent.Permission.MANAGE_CHAT -> current.copy(canManageChat = !current.canManageChat)
            AdminManageComponent.Permission.CHANGE_INFO -> current.copy(canChangeInfo = !current.canChangeInfo)
            AdminManageComponent.Permission.POST_MESSAGES -> current.copy(canPostMessages = !current.canPostMessages)
            AdminManageComponent.Permission.EDIT_MESSAGES -> current.copy(canEditMessages = !current.canEditMessages)
            AdminManageComponent.Permission.DELETE_MESSAGES -> current.copy(canDeleteMessages = !current.canDeleteMessages)
            AdminManageComponent.Permission.INVITE_USERS -> current.copy(canInviteUsers = !current.canInviteUsers)
            AdminManageComponent.Permission.RESTRICT_MEMBERS -> current.copy(canRestrictMembers = !current.canRestrictMembers)
            AdminManageComponent.Permission.PIN_MESSAGES -> current.copy(canPinMessages = !current.canPinMessages)
            AdminManageComponent.Permission.MANAGE_TOPICS -> current.copy(canManageTopics = !current.canManageTopics)
            AdminManageComponent.Permission.PROMOTE_MEMBERS -> current.copy(canPromoteMembers = !current.canPromoteMembers)
            AdminManageComponent.Permission.MANAGE_VIDEO_CHATS -> current.copy(canManageVideoChats = !current.canManageVideoChats)
            AdminManageComponent.Permission.POST_STORIES -> current.copy(canPostStories = !current.canPostStories)
            AdminManageComponent.Permission.EDIT_STORIES -> current.copy(canEditStories = !current.canEditStories)
            AdminManageComponent.Permission.DELETE_STORIES -> current.copy(canDeleteStories = !current.canDeleteStories)
            AdminManageComponent.Permission.ANONYMOUS -> current.copy(isAnonymous = !current.isAnonymous)
        }
        _state.value = _state.value.copy(currentStatus = updated)
    }

    override fun onUpdateCustomTitle(title: String) {
        val current = _state.value.currentStatus as? ChatMemberStatus.Administrator ?: return
        _state.value = _state.value.copy(currentStatus = current.copy(customTitle = title))
    }
}
