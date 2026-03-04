package org.monogram.presentation.settingsScreens.dataStorage

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.monogram.presentation.root.AppComponentContext
import org.monogram.presentation.util.AppPreferences

interface DataStorageComponent {
    val state: Value<State>
    fun onBackClicked()
    fun onAutoDownloadMobileChanged(enabled: Boolean)
    fun onAutoDownloadWifiChanged(enabled: Boolean)
    fun onAutoDownloadRoamingChanged(enabled: Boolean)
    fun onAutoDownloadFilesChanged(enabled: Boolean)
    fun onAutoDownloadStickersChanged(enabled: Boolean)
    fun onAutoDownloadVideoNotesChanged(enabled: Boolean)
    fun onAutoplayGifsChanged(enabled: Boolean)
    fun onAutoplayVideosChanged(enabled: Boolean)
    fun onEnableStreamingChanged(enabled: Boolean)
    fun onStorageUsageClicked()
    fun onNetworkUsageClicked()

    data class State(
        val autoDownloadMobile: Boolean = true,
        val autoDownloadWifi: Boolean = true,
        val autoDownloadRoaming: Boolean = false,
        val autoDownloadFiles: Boolean = false,
        val autoDownloadStickers: Boolean = true,
        val autoDownloadVideoNotes: Boolean = true,
        val autoplayGifs: Boolean = true,
        val autoplayVideos: Boolean = true,
        val enableStreaming: Boolean = true
    )
}

class DefaultDataStorageComponent(
    context: AppComponentContext,
    private val appPreferences: AppPreferences = context.container.preferences.appPreferences,
    private val onBack: () -> Unit,
    private val onStorageUsage: () -> Unit,
    private val onNetworkUsage: () -> Unit
) : DataStorageComponent, AppComponentContext by context {

    private val _state = MutableValue(DataStorageComponent.State())
    override val state: Value<DataStorageComponent.State> = _state
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        appPreferences.autoDownloadMobile.onEach {
            _state.value = _state.value.copy(autoDownloadMobile = it)
        }.launchIn(scope)

        appPreferences.autoDownloadWifi.onEach {
            _state.value = _state.value.copy(autoDownloadWifi = it)
        }.launchIn(scope)

        appPreferences.autoDownloadRoaming.onEach {
            _state.value = _state.value.copy(autoDownloadRoaming = it)
        }.launchIn(scope)

        appPreferences.autoDownloadFiles.onEach {
            _state.value = _state.value.copy(autoDownloadFiles = it)
        }.launchIn(scope)

        appPreferences.autoDownloadStickers.onEach {
            _state.value = _state.value.copy(autoDownloadStickers = it)
        }.launchIn(scope)

        appPreferences.autoDownloadVideoNotes.onEach {
            _state.value = _state.value.copy(autoDownloadVideoNotes = it)
        }.launchIn(scope)

        appPreferences.autoplayGifs.onEach {
            _state.value = _state.value.copy(autoplayGifs = it)
        }.launchIn(scope)

        appPreferences.autoplayVideos.onEach {
            _state.value = _state.value.copy(autoplayVideos = it)
        }.launchIn(scope)

        appPreferences.enableStreaming.onEach {
            _state.value = _state.value.copy(enableStreaming = it)
        }.launchIn(scope)
    }

    override fun onBackClicked() {
        onBack()
    }

    override fun onAutoDownloadMobileChanged(enabled: Boolean) {
        appPreferences.setAutoDownloadMobile(enabled)
    }

    override fun onAutoDownloadWifiChanged(enabled: Boolean) {
        appPreferences.setAutoDownloadWifi(enabled)
    }

    override fun onAutoDownloadRoamingChanged(enabled: Boolean) {
        appPreferences.setAutoDownloadRoaming(enabled)
    }

    override fun onAutoDownloadFilesChanged(enabled: Boolean) {
        appPreferences.setAutoDownloadFiles(enabled)
    }

    override fun onAutoDownloadStickersChanged(enabled: Boolean) {
        appPreferences.setAutoDownloadStickers(enabled)
    }

    override fun onAutoDownloadVideoNotesChanged(enabled: Boolean) {
        appPreferences.setAutoDownloadVideoNotes(enabled)
    }

    override fun onAutoplayGifsChanged(enabled: Boolean) {
        appPreferences.setAutoplayGifs(enabled)
    }

    override fun onAutoplayVideosChanged(enabled: Boolean) {
        appPreferences.setAutoplayVideos(enabled)
    }

    override fun onEnableStreamingChanged(enabled: Boolean) {
        appPreferences.setEnableStreaming(enabled)
    }

    override fun onStorageUsageClicked() {
        onStorageUsage()
    }

    override fun onNetworkUsageClicked() {
        onNetworkUsage()
    }
}
