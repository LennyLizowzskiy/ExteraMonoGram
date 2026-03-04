package org.monogram.presentation.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.monogram.domain.models.ChatFullInfoModel
import org.monogram.domain.models.ChatModel
import org.monogram.domain.models.ProxyTypeModel
import org.monogram.presentation.auth.AuthComponent
import org.monogram.presentation.chatsScreen.ChatListComponent
import org.monogram.presentation.chatsScreen.NewChatComponent
import org.monogram.presentation.chatsScreen.currentChat.ChatComponent
import org.monogram.presentation.chatsScreen.currentChat.components.VideoPlayerPool
import org.monogram.presentation.chatsScreen.folders.FoldersComponent
import org.monogram.presentation.profile.ProfileComponent
import org.monogram.presentation.profile.admin.AdminManageComponent
import org.monogram.presentation.profile.admin.ChatEditComponent
import org.monogram.presentation.profile.admin.ChatPermissionsComponent
import org.monogram.presentation.profile.admin.MemberListComponent
import org.monogram.presentation.profile.logs.ProfileLogsComponent
import org.monogram.presentation.settingsScreens.about.AboutComponent
import org.monogram.presentation.settingsScreens.adblock.AdBlockComponent
import org.monogram.presentation.settingsScreens.chatSettings.ChatSettingsComponent
import org.monogram.presentation.settingsScreens.dataStorage.DataStorageComponent
import org.monogram.presentation.settingsScreens.debug.DebugComponent
import org.monogram.presentation.settingsScreens.networkUsage.NetworkUsageComponent
import org.monogram.presentation.settingsScreens.notifications.NotificationsComponent
import org.monogram.presentation.settingsScreens.powersaving.PowerSavingComponent
import org.monogram.presentation.settingsScreens.premium.PremiumComponent
import org.monogram.presentation.settingsScreens.privacy.PasscodeComponent
import org.monogram.presentation.settingsScreens.privacy.PrivacyComponent
import org.monogram.presentation.settingsScreens.profile.EditProfileComponent
import org.monogram.presentation.settingsScreens.proxy.ProxyComponent
import org.monogram.presentation.settingsScreens.sessions.SessionsComponent
import org.monogram.presentation.settingsScreens.settings.SettingsComponent
import org.monogram.presentation.settingsScreens.stickers.StickersComponent
import org.monogram.presentation.settingsScreens.storage.StorageUsageComponent
import org.monogram.presentation.stickers.core.StickerSetUiModel
import org.monogram.presentation.util.AppPreferences

interface RootComponent {
    val backHandler: BackHandler
    val childStack: Value<ChildStack<*, Child>>
    val stickerSetToPreview: StateFlow<StickerPreviewState>
    val proxyToConfirm: StateFlow<ProxyConfirmState>
    val chatToConfirmJoin: StateFlow<ChatConfirmJoinState>
    val isLocked: StateFlow<Boolean>
    val isBiometricEnabled: StateFlow<Boolean>
    val videoPlayerPool: VideoPlayerPool
    val appPreferences: AppPreferences

    fun onBack()
    fun handleLink(link: String)
    fun dismissStickerPreview()
    fun onSettingsClick()
    fun onChatsClick()
    fun dismissProxyConfirm()
    fun confirmProxy(server: String, port: Int, type: ProxyTypeModel)
    fun recheckProxyPing()
    fun dismissChatConfirmJoin()
    fun confirmJoinChat(chatId: Long)
    fun confirmJoinInviteLink(inviteLink: String)
    fun unlock(passcode: String): Boolean
    fun unlockWithBiometrics()
    fun logout()

    sealed class Child {
        class StartupChild(val component: StartupComponent) : Child()
        class AuthChild(val component: AuthComponent) : Child()
        class ChatsChild(val component: ChatListComponent) : Child()
        class NewChatChild(val component: NewChatComponent) : Child()
        class ChatDetailChild(val component: ChatComponent) : Child()
        class SettingsChild(val component: SettingsComponent) : Child()
        class EditProfileChild(val component: EditProfileComponent) : Child()
        class SessionsChild(val component: SessionsComponent) : Child()
        class FoldersChild(val component: FoldersComponent) : Child()
        class ChatSettingsChild(val component: ChatSettingsComponent) : Child()
        class DataStorageChild(val component: DataStorageComponent) : Child()
        class StorageUsageChild(val component: StorageUsageComponent) : Child()
        class NetworkUsageChild(val component: NetworkUsageComponent) : Child()
        class ProfileChild(val component: ProfileComponent) : Child()
        class PremiumChild(val component: PremiumComponent) : Child()
        class PrivacyChild(val component: PrivacyComponent) : Child()
        class AdBlockChild(val component: AdBlockComponent) : Child()
        class PowerSavingChild(val component: PowerSavingComponent) : Child()
        class NotificationsChild(val component: NotificationsComponent) : Child()
        class ProxyChild(val component: ProxyComponent) : Child()
        class ProfileLogsChild(val component: ProfileLogsComponent) : Child()
        class AdminManageChild(val component: AdminManageComponent) : Child()
        class ChatEditChild(val component: ChatEditComponent) : Child()
        class MemberListChild(val component: MemberListComponent) : Child()
        class ChatPermissionsChild(val component: ChatPermissionsComponent) : Child()
        class PasscodeChild(val component: PasscodeComponent) : Child()
        class StickersChild(val component: StickersComponent) : Child()
        class AboutChild(val component: AboutComponent) : Child()
        class DebugChild(val component: DebugComponent) : Child()
    }

    @Serializable
    data class StickerPreviewState(
        val stickerSet: StickerSetUiModel? = null
    )

    data class ProxyConfirmState(
        val server: String? = null,
        val port: Int? = null,
        val type: ProxyTypeModel? = null,
        val ping: Long? = null,
        val isChecking: Boolean = false
    )

    data class ChatConfirmJoinState(
        val chat: ChatModel? = null,
        val fullInfo: ChatFullInfoModel? = null,
        val inviteLink: String? = null,
        val inviteTitle: String? = null,
        val inviteDescription: String? = null,
        val inviteMemberCount: Int = 0,
        val inviteAvatarPath: String? = null,
        val inviteIsChannel: Boolean = false
    )
}