package org.monogram.presentation.settingsScreens.stickers

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import org.monogram.domain.models.StickerSetModel
import org.monogram.domain.repository.LinkHandlerRepository
import org.monogram.domain.repository.StickerRepository
import org.monogram.presentation.root.AppComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

interface StickersComponent {
    val state: Value<State>

    fun onBackClicked()
    fun onStickerSetClicked(stickerSet: StickerSetModel)
    fun onToggleStickerSet(stickerSet: StickerSetModel)
    fun onArchiveStickerSet(stickerSet: StickerSetModel)
    fun onClearRecentStickers()
    fun onClearRecentEmojis()
    fun onTabSelected(index: Int)
    fun onSearchQueryChanged(query: String)
    fun onMoveStickerSet(fromIndex: Int, toIndex: Int)
    fun onAddStickersClicked()
    fun onDismissMiniApp()

    data class State(
        val stickerSets: List<StickerSetModel> = emptyList(),
        val emojiSets: List<StickerSetModel> = emptyList(),
        val archivedStickerSets: List<StickerSetModel> = emptyList(),
        val archivedEmojiSets: List<StickerSetModel> = emptyList(),
        val isLoading: Boolean = true,
        val selectedTabIndex: Int = 0,
        val searchQuery: String = "",
        val miniAppUrl: String? = null,
        val miniAppName: String? = null,
        val miniAppBotUserId: Long = 0L
    )
}

class DefaultStickersComponent(
    context: AppComponentContext,
    private val stickerRepository: StickerRepository = context.container.repositories.stickerRepository,
    private val onBack: () -> Unit,
    private val linkHandler: LinkHandlerRepository = context.container.repositories.linkHandlerRepository,
    private val onStickerSetSelect: (StickerSetModel) -> Unit
) : StickersComponent, AppComponentContext by context {

    private val _state = MutableValue(
        StickersComponent.State(
            stickerSets = stickerRepository.installedStickerSets.value,
            emojiSets = stickerRepository.customEmojiStickerSets.value,
            archivedStickerSets = stickerRepository.archivedStickerSets.value,
            archivedEmojiSets = stickerRepository.archivedEmojiSets.value,
            isLoading = stickerRepository.installedStickerSets.value.isEmpty()
        )
    )
    override val state: Value<StickersComponent.State> = _state
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        scope.launch {
            stickerRepository.loadInstalledStickerSets()
            stickerRepository.loadCustomEmojiStickerSets()
            stickerRepository.loadArchivedStickerSets()
            stickerRepository.loadArchivedEmojiSets()
        }

        scope.launch {
            stickerRepository.installedStickerSets.collectLatest { sets ->
                _state.value = _state.value.copy(
                    stickerSets = sets,
                    isLoading = false
                )
            }
        }

        scope.launch {
            stickerRepository.customEmojiStickerSets.collectLatest { sets ->
                _state.value = _state.value.copy(
                    emojiSets = sets
                )
            }
        }

        scope.launch {
            stickerRepository.archivedStickerSets.collectLatest { sets ->
                _state.value = _state.value.copy(
                    archivedStickerSets = sets
                )
            }
        }

        scope.launch {
            stickerRepository.archivedEmojiSets.collectLatest { sets ->
                _state.value = _state.value.copy(
                    archivedEmojiSets = sets
                )
            }
        }
    }

    override fun onBackClicked() {
        onBack()
    }

    override fun onStickerSetClicked(stickerSet: StickerSetModel) {
        onStickerSetSelect(stickerSet)
    }

    override fun onToggleStickerSet(stickerSet: StickerSetModel) {
        scope.launch {
            stickerRepository.toggleStickerSetInstalled(stickerSet.id, !stickerSet.isInstalled)
        }
    }

    override fun onArchiveStickerSet(stickerSet: StickerSetModel) {
        scope.launch {
            stickerRepository.toggleStickerSetArchived(stickerSet.id, !stickerSet.isArchived)
        }
    }

    override fun onClearRecentStickers() {
        scope.launch {
            stickerRepository.clearRecentStickers()
        }
    }

    override fun onClearRecentEmojis() {
        scope.launch {
            stickerRepository.clearRecentEmojis()
        }
    }

    override fun onTabSelected(index: Int) {
        _state.value = _state.value.copy(selectedTabIndex = index)
    }

    override fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    override fun onMoveStickerSet(fromIndex: Int, toIndex: Int) {
        val currentTabIndex = _state.value.selectedTabIndex
        val currentList = if (currentTabIndex == 0) _state.value.stickerSets else _state.value.emojiSets

        if (fromIndex !in currentList.indices || toIndex !in currentList.indices) return

        val newList = currentList.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }

        if (currentTabIndex == 0) {
            _state.value = _state.value.copy(stickerSets = newList)
        } else {
            _state.value = _state.value.copy(emojiSets = newList)
        }

        scope.launch {
            val type = if (currentTabIndex == 0) {
                StickerRepository.TdLibStickerType.REGULAR
            } else {
                StickerRepository.TdLibStickerType.CUSTOM_EMOJI
            }
            stickerRepository.reorderStickerSets(type, newList.map { it.id })
        }
    }

    override fun onAddStickersClicked() {
        _state.value = _state.value.copy(
            miniAppUrl = "menu://https://webappinternal.telegram.org/stickers",
            miniAppName = "Stickers",
            miniAppBotUserId = 429000L
        )
    }

    override fun onDismissMiniApp() {
        _state.value = _state.value.copy(
            miniAppUrl = null,
            miniAppName = null,
            miniAppBotUserId = 0L
        )
    }
}
