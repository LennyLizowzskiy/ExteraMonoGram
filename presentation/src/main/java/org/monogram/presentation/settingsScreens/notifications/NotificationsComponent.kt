package org.monogram.presentation.settingsScreens.notifications

import android.os.Parcelable
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.monogram.domain.managers.DistrManager
import org.monogram.domain.models.ChatModel
import org.monogram.domain.repository.PushProvider
import org.monogram.domain.repository.SettingsRepository
import org.monogram.domain.repository.SettingsRepository.TdNotificationScope
import org.monogram.presentation.chatsScreen.currentChat.components.VideoPlayerPool
import org.monogram.presentation.root.AppComponentContext
import org.monogram.presentation.util.AppPreferences

interface NotificationsComponent {
    val state: Value<State>
    val videoPlayerPool: VideoPlayerPool
    val childStack: Value<ChildStack<*, Child>>

    fun onBackClicked()
    fun onPrivateChatsToggled(enabled: Boolean)
    fun onGroupsToggled(enabled: Boolean)
    fun onChannelsToggled(enabled: Boolean)
    fun onInAppSoundsToggled(enabled: Boolean)
    fun onInAppVibrateToggled(enabled: Boolean)
    fun onInAppPreviewToggled(enabled: Boolean)
    fun onContactJoinedToggled(enabled: Boolean)
    fun onPinnedMessagesToggled(enabled: Boolean)
    fun onBackgroundServiceToggled(enabled: Boolean)
    fun onHideForegroundNotificationToggled(enabled: Boolean)
    fun onVibrationPatternChanged(pattern: String)
    fun onPriorityChanged(priority: Int)
    fun onRepeatNotificationsChanged(minutes: Int)
    fun onShowSenderOnlyToggled(enabled: Boolean)
    fun onPushProviderChanged(provider: PushProvider)
    fun onResetNotificationsClicked()
    fun onExceptionClicked(scope: TdNotificationScope)
    fun onChatExceptionToggled(chatId: Long, enabled: Boolean)
    fun onChatExceptionReset(chatId: Long)

    data class State(
        val privateChatsEnabled: Boolean = true,
        val groupsEnabled: Boolean = true,
        val channelsEnabled: Boolean = true,
        val inAppSounds: Boolean = true,
        val inAppVibrate: Boolean = true,
        val inAppPreview: Boolean = true,
        val contactJoined: Boolean = true,
        val pinnedMessages: Boolean = true,
        val backgroundServiceEnabled: Boolean = true,
        val hideForegroundNotification: Boolean = false,
        val vibrationPattern: String = "default",
        val priority: Int = 1,
        val repeatNotifications: Int = 0,
        val showSenderOnly: Boolean = false,
        val pushProvider: PushProvider = PushProvider.FCM,
        val isGmsAvailable: Boolean = false,
        val privateExceptions: List<ChatModel>? = null,
        val groupExceptions: List<ChatModel>? = null,
        val channelExceptions: List<ChatModel>? = null
    )

    sealed class Child {
        object Main : Child()
        class Exceptions(val scope: TdNotificationScope) : Child()
    }
}

class DefaultNotificationsComponent(
    context: AppComponentContext,
    private val appPreferences: AppPreferences = context.container.preferences.appPreferences,
    private val settingsRepository: SettingsRepository = context.container.repositories.settingsRepository,
    override val videoPlayerPool: VideoPlayerPool = context.container.utils.videoPlayerPool,
    private val distrManager: DistrManager = context.container.utils.distrManager(),
    private val onBack: () -> Unit
) : NotificationsComponent, AppComponentContext by context {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, NotificationsComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Main,
        handleBackButton = true,
        childFactory = ::createChild
    )

    private fun createChild(config: Config, context: AppComponentContext): NotificationsComponent.Child =
        when (config) {
            is Config.Main -> NotificationsComponent.Child.Main
            is Config.Exceptions -> NotificationsComponent.Child.Exceptions(config.scope)
        }

    private val _state = MutableValue(NotificationsComponent.State())
    override val state: Value<NotificationsComponent.State> = _state

    init {
        appPreferences.privateChatsNotifications.onEach {
            _state.value = _state.value.copy(privateChatsEnabled = it)
        }.launchIn(scope)

        appPreferences.groupsNotifications.onEach {
            _state.value = _state.value.copy(groupsEnabled = it)
        }.launchIn(scope)

        appPreferences.channelsNotifications.onEach {
            _state.value = _state.value.copy(channelsEnabled = it)
        }.launchIn(scope)

        appPreferences.inAppSounds.onEach {
            _state.value = _state.value.copy(inAppSounds = it)
        }.launchIn(scope)

        appPreferences.inAppVibrate.onEach {
            _state.value = _state.value.copy(inAppVibrate = it)
        }.launchIn(scope)

        appPreferences.inAppPreview.onEach {
            _state.value = _state.value.copy(inAppPreview = it)
        }.launchIn(scope)

        appPreferences.contactJoinedNotifications.onEach {
            _state.value = _state.value.copy(contactJoined = it)
        }.launchIn(scope)

        appPreferences.pinnedMessagesNotifications.onEach {
            _state.value = _state.value.copy(pinnedMessages = it)
        }.launchIn(scope)

        appPreferences.backgroundServiceEnabled.onEach {
            _state.value = _state.value.copy(backgroundServiceEnabled = it)
        }.launchIn(scope)

        appPreferences.hideForegroundNotification.onEach {
            _state.value = _state.value.copy(hideForegroundNotification = it)
        }.launchIn(scope)

        appPreferences.notificationVibrationPattern.onEach {
            _state.value = _state.value.copy(vibrationPattern = it)
        }.launchIn(scope)

        appPreferences.notificationPriority.onEach {
            _state.value = _state.value.copy(priority = it)
        }.launchIn(scope)

        appPreferences.repeatNotifications.onEach {
            _state.value = _state.value.copy(repeatNotifications = it)
        }.launchIn(scope)

        appPreferences.showSenderOnly.onEach {
            _state.value = _state.value.copy(showSenderOnly = it)
        }.launchIn(scope)

        appPreferences.pushProvider.onEach {
            _state.value = _state.value.copy(pushProvider = it)
        }.launchIn(scope)

        _state.update { it.copy(isGmsAvailable = distrManager.isGmsAvailable()) }

        syncSettings()
    }

    private fun syncSettings() {
        scope.launch {
            val privateEnabled = settingsRepository.getNotificationSettings(TdNotificationScope.PRIVATE_CHATS)
            appPreferences.setPrivateChatsNotifications(privateEnabled)

            val groupsEnabled = settingsRepository.getNotificationSettings(TdNotificationScope.GROUPS)
            appPreferences.setGroupsNotifications(groupsEnabled)

            val channelsEnabled = settingsRepository.getNotificationSettings(TdNotificationScope.CHANNELS)
            appPreferences.setChannelsNotifications(channelsEnabled)
        }
    }

    private fun loadExceptions(scope: TdNotificationScope) {
        this.scope.launch {
            _state.value = when (scope) {
                TdNotificationScope.PRIVATE_CHATS -> _state.value.copy(privateExceptions = null)
                TdNotificationScope.GROUPS -> _state.value.copy(groupExceptions = null)
                TdNotificationScope.CHANNELS -> _state.value.copy(channelExceptions = null)
            }

            val exceptions = settingsRepository.getExceptions(scope)

            _state.value = when (scope) {
                TdNotificationScope.PRIVATE_CHATS -> _state.value.copy(privateExceptions = exceptions)
                TdNotificationScope.GROUPS -> _state.value.copy(groupExceptions = exceptions)
                TdNotificationScope.CHANNELS -> _state.value.copy(channelExceptions = exceptions)
            }
        }
    }

    override fun onBackClicked() {
        if (childStack.value.items.size > 1) {
            navigation.pop()
        } else {
            onBack()
        }
    }

    override fun onPrivateChatsToggled(enabled: Boolean) {
        appPreferences.setPrivateChatsNotifications(enabled)
        scope.launch {
            settingsRepository.setNotificationSettings(TdNotificationScope.PRIVATE_CHATS, enabled)
        }
    }

    override fun onGroupsToggled(enabled: Boolean) {
        appPreferences.setGroupsNotifications(enabled)
        scope.launch {
            settingsRepository.setNotificationSettings(TdNotificationScope.GROUPS, enabled)
        }
    }

    override fun onChannelsToggled(enabled: Boolean) {
        appPreferences.setChannelsNotifications(enabled)
        scope.launch {
            settingsRepository.setNotificationSettings(TdNotificationScope.CHANNELS, enabled)
        }
    }

    override fun onInAppSoundsToggled(enabled: Boolean) {
        appPreferences.setInAppSounds(enabled)
    }

    override fun onInAppVibrateToggled(enabled: Boolean) {
        appPreferences.setInAppVibrate(enabled)
    }

    override fun onInAppPreviewToggled(enabled: Boolean) {
        appPreferences.setInAppPreview(enabled)
    }

    override fun onContactJoinedToggled(enabled: Boolean) {
        appPreferences.setContactJoinedNotifications(enabled)
    }

    override fun onPinnedMessagesToggled(enabled: Boolean) {
        appPreferences.setPinnedMessagesNotifications(enabled)
    }

    override fun onBackgroundServiceToggled(enabled: Boolean) {
        appPreferences.setBackgroundServiceEnabled(enabled)
    }

    override fun onHideForegroundNotificationToggled(enabled: Boolean) {
        appPreferences.setHideForegroundNotification(enabled)
    }

    override fun onVibrationPatternChanged(pattern: String) {
        appPreferences.setNotificationVibrationPattern(pattern)
    }

    override fun onPriorityChanged(priority: Int) {
        appPreferences.setNotificationPriority(priority)
    }

    override fun onRepeatNotificationsChanged(minutes: Int) {
        appPreferences.setRepeatNotifications(minutes)
    }

    override fun onShowSenderOnlyToggled(enabled: Boolean) {
        appPreferences.setShowSenderOnly(enabled)
    }

    override fun onPushProviderChanged(provider: PushProvider) {
        appPreferences.setPushProvider(provider)
    }

    override fun onResetNotificationsClicked() {
        onPrivateChatsToggled(true)
        onGroupsToggled(true)
        onChannelsToggled(true)
        onInAppSoundsToggled(true)
        onInAppVibrateToggled(true)
        onInAppPreviewToggled(true)
        onContactJoinedToggled(true)
        onPinnedMessagesToggled(true)
        onBackgroundServiceToggled(true)
        onHideForegroundNotificationToggled(false)
        onVibrationPatternChanged("default")
        onPriorityChanged(1)
        onRepeatNotificationsChanged(0)
        onShowSenderOnlyToggled(false)
        onPushProviderChanged(if (_state.value.isGmsAvailable) PushProvider.FCM else PushProvider.GMS_LESS)
    }

    override fun onExceptionClicked(scope: TdNotificationScope) {
        loadExceptions(scope)
        navigation.push(Config.Exceptions(scope))
    }

    override fun onChatExceptionToggled(chatId: Long, enabled: Boolean) {
        scope.launch {
            settingsRepository.setChatNotificationSettings(chatId, enabled)
            val currentChild = childStack.value.active.instance
            if (currentChild is NotificationsComponent.Child.Exceptions) {
                loadExceptions(currentChild.scope)
            }
        }
    }

    override fun onChatExceptionReset(chatId: Long) {
        scope.launch {
            settingsRepository.resetChatNotificationSettings(chatId)
            val currentChild = childStack.value.active.instance
            if (currentChild is NotificationsComponent.Child.Exceptions) {
                loadExceptions(currentChild.scope)
            }
        }
    }

    @Serializable
    sealed class Config : Parcelable {
        @Parcelize
        @Serializable
        object Main : Config()

        @Parcelize
        @Serializable
        data class Exceptions(val scope: TdNotificationScope) : Config()
    }
}
