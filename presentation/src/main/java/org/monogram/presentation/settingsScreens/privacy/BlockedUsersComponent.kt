package org.monogram.presentation.settingsScreens.privacy

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import org.monogram.domain.models.UserModel
import org.monogram.domain.repository.PrivacyRepository
import org.monogram.domain.repository.UserRepository
import org.monogram.presentation.chatsScreen.currentChat.components.VideoPlayerPool
import org.monogram.presentation.root.AppComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface BlockedUsersComponent {
    val state: Value<State>
    val videoPlayerPool: VideoPlayerPool
    fun onBackClicked()
    fun onUnblockUserClicked(userId: Long)
    fun onAddBlockedUserClicked()
    fun onUserClicked(userId: Long)

    data class State(
        val isLoading: Boolean = false,
        val blockedUsers: List<UserModel> = emptyList()
    )
}

class DefaultBlockedUsersComponent(
    context: AppComponentContext,
    private val privacyRepository: PrivacyRepository = context.container.repositories.privacyRepository,
    private val userRepository: UserRepository = context.container.repositories.userRepository,
    override val videoPlayerPool: VideoPlayerPool = context.container.utils.videoPlayerPool,
    private val onBack: () -> Unit,
    private val onProfileClick: (Long) -> Unit,
    private val onAddBlockedUser: () -> Unit
) : BlockedUsersComponent, AppComponentContext by context {

    private val _state = MutableValue(BlockedUsersComponent.State())
    override val state: Value<BlockedUsersComponent.State> = _state
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        loadBlockedUsers()
    }

    private fun loadBlockedUsers() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val blockedIds = privacyRepository.getBlockedUsers()
                val users = blockedIds.mapNotNull { id ->
                    try {
                        userRepository.getUser(id)
                    } catch (e: Exception) {
                        null
                    }
                }
                _state.value = _state.value.copy(blockedUsers = users)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    override fun onBackClicked() {
        onBack()
    }

    override fun onUnblockUserClicked(userId: Long) {
        scope.launch {
            try {
                privacyRepository.unblockUser(userId)
                loadBlockedUsers()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    override fun onAddBlockedUserClicked() {
        onAddBlockedUser()
    }

    override fun onUserClicked(userId: Long) {
        onProfileClick(userId)
    }
}