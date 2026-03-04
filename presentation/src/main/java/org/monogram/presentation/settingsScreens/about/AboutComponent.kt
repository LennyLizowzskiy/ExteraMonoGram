package org.monogram.presentation.settingsScreens.about

import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.monogram.domain.models.UpdateState
import org.monogram.domain.repository.UpdateRepository
import org.monogram.presentation.root.AppComponentContext

interface AboutComponent {
    val updateState: StateFlow<UpdateState>
    val tdLibVersion: StateFlow<String>
    val tdLibCommitHash: StateFlow<String>
    fun onBackClicked()
    fun checkForUpdates()
    fun downloadUpdate()
    fun installUpdate()
    fun onTermsOfServiceClicked()
    fun onOpenSourceLicensesClicked()
}

class DefaultAboutComponent(
    context: AppComponentContext,
    private val updateRepository: UpdateRepository,
    private val onBack: () -> Unit,
    private val onTermsOfService: () -> Unit,
    private val onOpenSourceLicenses: () -> Unit
) : AboutComponent, AppComponentContext by context {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val _tdLibVersion = MutableStateFlow("Loading...")
    override val tdLibVersion: StateFlow<String> = _tdLibVersion.asStateFlow()

    private val _tdLibCommitHash = MutableStateFlow("")
    override val tdLibCommitHash: StateFlow<String> = _tdLibCommitHash.asStateFlow()

    init {
        lifecycle.doOnDestroy {
            scope.cancel()
        }

        scope.launch {
            _tdLibVersion.value = updateRepository.getTdLibVersion()
            _tdLibCommitHash.value = updateRepository.getTdLibCommitHash()
        }
    }

    override val updateState: StateFlow<UpdateState> = updateRepository.updateState

    override fun onBackClicked() {
        onBack()
    }

    override fun checkForUpdates() {
        scope.launch {
            updateRepository.checkForUpdates()
        }
    }

    override fun downloadUpdate() {
        updateRepository.downloadUpdate()
    }

    override fun installUpdate() {
        updateRepository.installUpdate()
    }

    override fun onTermsOfServiceClicked() {
        onTermsOfService()
    }

    override fun onOpenSourceLicensesClicked() {
        onOpenSourceLicenses()
    }
}
