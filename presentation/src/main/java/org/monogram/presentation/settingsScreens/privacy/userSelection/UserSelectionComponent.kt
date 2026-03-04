package org.monogram.presentation.settingsScreens.privacy.userSelection

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import org.monogram.domain.models.UserModel
import org.monogram.domain.repository.UserRepository
import org.monogram.presentation.chatsScreen.currentChat.components.VideoPlayerPool
import org.monogram.presentation.root.AppComponentContext
import kotlinx.coroutines.*

interface UserSelectionComponent {
    val state: Value<State>
    val videoPlayerPool: VideoPlayerPool
    fun onBackClicked()
    fun onSearchQueryChanged(query: String)
    fun onUserClicked(userId: Long)

    data class State(
        val searchQuery: String = "",
        val users: List<UserModel> = emptyList(),
        val isLoading: Boolean = false
    )
}

class DefaultUserSelectionComponent(
    context: AppComponentContext,
    private val userRepository: UserRepository = context.container.repositories.userRepository,
    override val videoPlayerPool: VideoPlayerPool = context.container.utils.videoPlayerPool,
    private val onBack: () -> Unit,
    private val onUserSelected: (Long) -> Unit
) : UserSelectionComponent, AppComponentContext by context {

    private val _state = MutableValue(UserSelectionComponent.State())
    override val state: Value<UserSelectionComponent.State> = _state
    private val scope = CoroutineScope(Dispatchers.Main)
    private var searchJob: Job? = null

    override fun onBackClicked() {
        onBack()
    }

    override fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        searchJob?.cancel()

        if (query.isBlank()) {
            _state.value = _state.value.copy(users = emptyList())
            return
        }

        searchJob = scope.launch {
            delay(500) // Debounce
            _state.value = _state.value.copy(isLoading = true)
            try {
                val userId = query.toLongOrNull()
                if (userId != null) {
                    val user = userRepository.getUser(userId)
                    if (user != null) {
                        _state.value = _state.value.copy(users = listOf(user))
                    } else {
                        _state.value = _state.value.copy(users = emptyList())
                    }
                } else {
                    // TODO: Implement proper search in UserRepository
                    _state.value = _state.value.copy(users = emptyList())
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(users = emptyList())
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    override fun onUserClicked(userId: Long) {
        onUserSelected(userId)
    }
}