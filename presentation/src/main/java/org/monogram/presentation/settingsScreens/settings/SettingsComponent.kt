package org.monogram.presentation.settingsScreens.settings

import android.os.Build
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.monogram.domain.managers.DomainManager
import org.monogram.domain.models.UserModel
import org.monogram.domain.repository.ExternalNavigator
import org.monogram.domain.repository.UserRepository
import org.monogram.presentation.chatsScreen.currentChat.components.VideoPlayerPool
import org.monogram.presentation.root.AppComponentContext

interface SettingsComponent {
    val state: Value<State>
    val videoPlayerPool: VideoPlayerPool

    fun onBackClicked()
    fun onEditProfileClicked()
    fun onLogoutClicked()
    fun onNotificationToggled(enabled: Boolean)
    fun onDevicesClicked()
    fun onFoldersClicked()
    fun onChatSettingsClicked()
    fun onDataStorageClicked()
    fun onPowerSavingClicked()
    fun onPremiumClicked()
    fun onPrivacyClicked()
    fun onNotificationsClicked()
    fun onLinkSettingsClicked()
    fun checkLinkStatus()
    fun onQrCodeClicked()
    fun onQrCodeDismissed()
    fun onProxySettingsClicked()
    fun onStickersClicked()
    fun onAboutClicked()
    fun onDebugClicked()

    data class State(
        val currentUser: UserModel? = null,
        val areNotificationsEnabled: Boolean = true,
        val isTMeLinkEnabled: Boolean = true,
        val isQrVisible: Boolean = false,
        val qrContent: String = ""
    )
}

class DefaultSettingsComponent(
    context: AppComponentContext,
    private val repository: UserRepository = context.container.repositories.userRepository,
    val externalNavigator: ExternalNavigator = context.container.utils.externalNavigator(),
    private val domainManager: DomainManager = context.container.utils.domainManager(),
    override val videoPlayerPool: VideoPlayerPool = context.container.utils.videoPlayerPool,
    private val onBack: () -> Unit,
    private val onEditProfileClick: () -> Unit,
    private val onDevicesClick: () -> Unit,
    private val onFoldersClick: () -> Unit,
    private val onChatSettingsClick: () -> Unit,
    private val onDataStorageClick: () -> Unit,
    private val onPowerSavingClick: () -> Unit,
    private val onPremiumClick: () -> Unit,
    private val onPrivacyClick: () -> Unit,
    private val onNotificationsClick: () -> Unit,
    private val onProxySettingsClick: () -> Unit,
    private val onStickersClick: () -> Unit,
    private val onAboutClick: () -> Unit,
    private val onDebugClick: () -> Unit
) : SettingsComponent, AppComponentContext by context {

    private val _state = MutableValue(SettingsComponent.State())
    override val state: Value<SettingsComponent.State> = _state
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        scope.launch {
            try {
                val me = repository.getMe()
                val link =
                    if (me.username?.isNotEmpty() == true) "https://t.me/${me.username}" else ""
                _state.value = _state.value.copy(
                    currentUser = me,
                    qrContent = link
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(currentUser = null)
            }
        }
        checkLinkStatus()
    }

    override fun onBackClicked() {
        onBack()
    }

    override fun onEditProfileClicked() {
        onEditProfileClick()
    }

    override fun onLogoutClicked() {
        repository.logOut()
    }

    override fun onNotificationToggled(enabled: Boolean) {
        _state.value = _state.value.copy(areNotificationsEnabled = enabled)
    }

    override fun onDevicesClicked() {
        onDevicesClick()
    }

    override fun onFoldersClicked() {
        onFoldersClick()
    }

    override fun onChatSettingsClicked() {
        onChatSettingsClick()
    }

    override fun onDataStorageClicked() {
        onDataStorageClick()
    }

    override fun onPowerSavingClicked() {
        onPowerSavingClick()
    }

    override fun onPremiumClicked() {
        onPremiumClick()
    }

    override fun onPrivacyClicked() {
        onPrivacyClick()
    }

    override fun onNotificationsClicked() {
        onNotificationsClick()
    }

    override fun onLinkSettingsClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            externalNavigator.navigateToLinkSettings()
        }
    }

    override fun checkLinkStatus() {
            _state.update { it.copy(isTMeLinkEnabled = domainManager.isEnabled() ) }
    }

    override fun onQrCodeClicked() {
        _state.value = _state.value.copy(isQrVisible = true)
    }

    override fun onQrCodeDismissed() {
        _state.value = _state.value.copy(isQrVisible = false)
    }

    override fun onProxySettingsClicked() {
        onProxySettingsClick()
    }

    override fun onStickersClicked() {
        onStickersClick()
    }

    override fun onAboutClicked() {
        onAboutClick()
    }

    override fun onDebugClicked() {
        onDebugClick()
    }
}
