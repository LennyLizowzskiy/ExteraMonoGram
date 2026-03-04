package org.monogram.presentation.profile.admin

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import org.monogram.domain.models.GroupMemberModel
import org.monogram.domain.repository.ChatMemberStatus
import org.monogram.domain.repository.ChatMembersFilter
import org.monogram.domain.repository.UserRepository
import org.monogram.presentation.chatsScreen.currentChat.components.VideoPlayerPool
import org.monogram.presentation.root.AppComponentContext
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent

class DefaultMemberListComponent(
    context: AppComponentContext,
    private val chatId: Long,
    private val type: MemberListComponent.MemberListType,
    private val userRepository: UserRepository = context.container.repositories.userRepository,
    override val videoPlayerPool: VideoPlayerPool = context.container.utils.videoPlayerPool,
    private val onBackClicked: () -> Unit,
    private val onMemberClicked: (Long) -> Unit,
    private val onMemberLongClicked: (Long) -> Unit
) : MemberListComponent, AppComponentContext by context {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _state = MutableValue(MemberListComponent.State(chatId = chatId, type = type))
    override val state: Value<MemberListComponent.State> = _state

    private var offset = 0
    private val limit = 50
    private var searchJob: Job? = null

    init {
        loadMembers()
    }

    private fun loadMembers() {
        if (_state.value.isLoading || !_state.value.canLoadMore || _state.value.isSearchActive) return

        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val filter = when (type) {
                    MemberListComponent.MemberListType.ADMINS -> ChatMembersFilter.Administrators
                    MemberListComponent.MemberListType.MEMBERS -> ChatMembersFilter.Recent
                    MemberListComponent.MemberListType.BLACKLIST -> ChatMembersFilter.Banned
                }

                val members = userRepository.getChatMembers(chatId, offset, limit, filter)

                if (members.isEmpty()) {
                    _state.value = _state.value.copy(canLoadMore = false)
                } else {
                    offset += members.size
                    _state.value = _state.value.copy(
                        members = _state.value.members + members,
                        canLoadMore = members.size >= limit
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    override fun onBack() {
        if (_state.value.isSearchActive) {
            onToggleSearch()
        } else {
            onBackClicked()
        }
    }

    override fun onMemberClick(member: GroupMemberModel) {
        if (type == MemberListComponent.MemberListType.ADMINS) {
            onMemberLongClicked(member.user.id)
        } else {
            onMemberClicked(member.user.id)
        }
    }

    override fun onMemberLongClick(member: GroupMemberModel) = onMemberLongClicked(member.user.id)
    override fun onLoadMore() = loadMembers()

    override fun onSearch(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(300)
            if (query.isBlank()) {
                offset = 0
                _state.value = _state.value.copy(members = emptyList(), canLoadMore = true)
                loadMembers()
            } else {
                _state.value = _state.value.copy(isLoading = true)
                try {
                    val filter = ChatMembersFilter.Search(query)
                    val results = userRepository.getChatMembers(chatId, 0, 50, filter)

                    val filtered = when (type) {
                        MemberListComponent.MemberListType.ADMINS -> results.filter { it.status is ChatMemberStatus.Administrator || it.status is ChatMemberStatus.Creator }
                        MemberListComponent.MemberListType.MEMBERS -> results.filter { it.status is ChatMemberStatus.Member }
                        MemberListComponent.MemberListType.BLACKLIST -> results.filter { it.status is ChatMemberStatus.Banned }
                    }

                    _state.value = _state.value.copy(members = filtered, canLoadMore = false)
                } finally {
                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }
    }

    override fun onToggleSearch() {
        val isNowActive = !_state.value.isSearchActive
        _state.value = _state.value.copy(
            isSearchActive = isNowActive,
            searchQuery = "",
            members = if (isNowActive) _state.value.members else emptyList(),
            canLoadMore = !isNowActive
        )
        if (!isNowActive) {
            offset = 0
            loadMembers()
        }
    }

    override fun onAddMember() {
    }
}
