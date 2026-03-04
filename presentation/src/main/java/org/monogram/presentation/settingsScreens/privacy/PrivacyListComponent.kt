package org.monogram.presentation.settingsScreens.privacy

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.monogram.domain.managers.DistrManager
import org.monogram.domain.models.PrivacyRule
import org.monogram.domain.models.PrivacyValue
import org.monogram.domain.repository.AppPreferencesProvider
import org.monogram.domain.repository.PrivacyKey
import org.monogram.domain.repository.PrivacyRepository
import org.monogram.presentation.root.AppComponentContext

interface PrivacyListComponent {
    val state: Value<State>
    fun onBackClicked()
    fun onBlockedUsersClicked()
    fun onPhoneNumberClicked()
    fun onLastSeenClicked()
    fun onProfilePhotoClicked()
    fun onBioClicked()
    fun onForwardedMessagesClicked()
    fun onCallsClicked()
    fun onGroupsAndChannelsClicked()
    fun onTwoStepVerificationClicked()
    fun onActiveSessionsClicked()
    fun onDeleteAccountClicked(reason: String)
    fun onAccountTtlChanged(days: Int)
    fun onSensitiveContentChanged(enabled: Boolean)
    fun onPasscodeClicked()
    fun onBiometricChanged(enabled: Boolean)

    data class State(
        val isLoading: Boolean = false,
        val phoneNumberPrivacy: PrivacyValue = PrivacyValue.EVERYBODY,
        val lastSeenPrivacy: PrivacyValue = PrivacyValue.EVERYBODY,
        val profilePhotoPrivacy: PrivacyValue = PrivacyValue.EVERYBODY,
        val bioPrivacy: PrivacyValue = PrivacyValue.EVERYBODY,
        val forwardedMessagesPrivacy: PrivacyValue = PrivacyValue.EVERYBODY,
        val callsPrivacy: PrivacyValue = PrivacyValue.EVERYBODY,
        val groupsAndChannelsPrivacy: PrivacyValue = PrivacyValue.EVERYBODY,
        val blockedUsersCount: Int = 0,
        val accountTtlDays: Int = 180,
        val isTwoStepVerificationEnabled: Boolean = false,
        val canShowSensitiveContent: Boolean = false,
        val isSensitiveContentEnabled: Boolean = false,
        val isPasscodeEnabled: Boolean = false,
        val isBiometricEnabled: Boolean = false,
        val error: String? = null,
        val isInstalledFromGooglePlay: Boolean = true
    )
}

class DefaultPrivacyListComponent(
    context: AppComponentContext,
    private val privacyRepository: PrivacyRepository = context.container.repositories.privacyRepository,
    private val appPreferences: AppPreferencesProvider = context.container.preferences.appPreferences,
    private val distrManager: DistrManager = context.container.utils.distrManager(),
    private val onBack: () -> Unit,
    private val onNavigateToPrivacySetting: (PrivacyKey) -> Unit,
    private val onNavigateToBlockedUsers: () -> Unit,
    private val onSessionsClick: () -> Unit,
    private val onPasscodeClick: () -> Unit
) : PrivacyListComponent, AppComponentContext by context {

    private val _state = MutableValue(PrivacyListComponent.State())
    override val state: Value<PrivacyListComponent.State> = _state
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        loadPrivacySettings()
        observePrivacyRules()
        observeAppPreferences()
    }

    private fun loadPrivacySettings() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val blockedUsers = privacyRepository.getBlockedUsers()
                val ttl = privacyRepository.getAccountTtl()
                val isTwoStepEnabled = privacyRepository.getPasswordState()
                val canShowSensitive = privacyRepository.canShowSensitiveContent()
                val isSensitiveEnabled = privacyRepository.isShowSensitiveContentEnabled()

                _state.value = _state.value.copy(
                    blockedUsersCount = blockedUsers.size,
                    accountTtlDays = ttl,
                    isTwoStepVerificationEnabled = isTwoStepEnabled,
                    canShowSensitiveContent = canShowSensitive,
                    isSensitiveContentEnabled = isSensitiveEnabled,
                    isInstalledFromGooglePlay = distrManager.isInstalledFromGooglePlay()
                )
            } catch (e: Exception) {
                // Handle error
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun observePrivacyRules() {
        observeRule(PrivacyKey.PHONE_NUMBER) { rules ->
            _state.value = _state.value.copy(phoneNumberPrivacy = rules.toPrivacyValue())
        }
        observeRule(PrivacyKey.LAST_SEEN) { rules ->
            _state.value = _state.value.copy(lastSeenPrivacy = rules.toPrivacyValue())
        }
        observeRule(PrivacyKey.PROFILE_PHOTO) { rules ->
            _state.value = _state.value.copy(profilePhotoPrivacy = rules.toPrivacyValue())
        }
        observeRule(PrivacyKey.BIO) { rules ->
            _state.value = _state.value.copy(bioPrivacy = rules.toPrivacyValue())
        }
        observeRule(PrivacyKey.FORWARDED_MESSAGES) { rules ->
            _state.value = _state.value.copy(forwardedMessagesPrivacy = rules.toPrivacyValue())
        }
        observeRule(PrivacyKey.CALLS) { rules ->
            _state.value = _state.value.copy(callsPrivacy = rules.toPrivacyValue())
        }
        observeRule(PrivacyKey.GROUPS_AND_CHANNELS) { rules ->
            _state.value = _state.value.copy(groupsAndChannelsPrivacy = rules.toPrivacyValue())
        }
    }

    private fun observeAppPreferences() {
        appPreferences.passcode.onEach { passcode ->
            _state.value = _state.value.copy(isPasscodeEnabled = passcode != null)
        }.launchIn(scope)

        appPreferences.isBiometricEnabled.onEach { enabled ->
            _state.value = _state.value.copy(isBiometricEnabled = enabled)
        }.launchIn(scope)
    }

    private fun observeRule(key: PrivacyKey, onUpdate: (List<PrivacyRule>) -> Unit) {
        privacyRepository.getPrivacyRules(key)
            .onEach { onUpdate(it) }
            .launchIn(scope)
    }

    private fun List<PrivacyRule>.toPrivacyValue(): PrivacyValue {
        return when {
            any { it is PrivacyRule.AllowAll } -> PrivacyValue.EVERYBODY
            any { it is PrivacyRule.AllowContacts } -> PrivacyValue.MY_CONTACTS
            any { it is PrivacyRule.AllowNone } -> PrivacyValue.NOBODY
            else -> PrivacyValue.EVERYBODY
        }
    }

    override fun onBackClicked() {
        onBack()
    }

    override fun onBlockedUsersClicked() {
        onNavigateToBlockedUsers()
    }

    override fun onPhoneNumberClicked() {
        onNavigateToPrivacySetting(PrivacyKey.PHONE_NUMBER)
    }

    override fun onLastSeenClicked() {
        onNavigateToPrivacySetting(PrivacyKey.LAST_SEEN)
    }

    override fun onProfilePhotoClicked() {
        onNavigateToPrivacySetting(PrivacyKey.PROFILE_PHOTO)
    }

    override fun onBioClicked() {
        onNavigateToPrivacySetting(PrivacyKey.BIO)
    }

    override fun onForwardedMessagesClicked() {
        onNavigateToPrivacySetting(PrivacyKey.FORWARDED_MESSAGES)
    }

    override fun onCallsClicked() {
        onNavigateToPrivacySetting(PrivacyKey.CALLS)
    }

    override fun onGroupsAndChannelsClicked() {
        onNavigateToPrivacySetting(PrivacyKey.GROUPS_AND_CHANNELS)
    }

    override fun onTwoStepVerificationClicked() {
    }

    override fun onActiveSessionsClicked() {
        onSessionsClick()
    }

    override fun onDeleteAccountClicked(reason: String) {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                privacyRepository.deleteAccount(reason, "")
                onBack()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    override fun onAccountTtlChanged(days: Int) {
        scope.launch {
            try {
                privacyRepository.setAccountTtl(days)
                _state.value = _state.value.copy(accountTtlDays = days)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    override fun onSensitiveContentChanged(enabled: Boolean) {
        scope.launch {
            try {
                privacyRepository.setShowSensitiveContent(enabled)
                _state.value = _state.value.copy(isSensitiveContentEnabled = enabled)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    override fun onPasscodeClicked() {
        onPasscodeClick()
    }

    override fun onBiometricChanged(enabled: Boolean) {
        appPreferences.setBiometricEnabled(enabled)
    }
}
