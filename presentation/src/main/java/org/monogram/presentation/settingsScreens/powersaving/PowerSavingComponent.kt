package org.monogram.presentation.settingsScreens.powersaving

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import org.monogram.domain.repository.AppPreferencesProvider
import org.monogram.presentation.root.AppComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface PowerSavingComponent {
    val state: Value<State>

    fun onBackClicked()
    fun onChatAnimationsToggled(enabled: Boolean)
    fun onBackgroundServiceToggled(enabled: Boolean)
    fun onPowerSavingModeToggled(enabled: Boolean)
    fun onWakeLockToggled(enabled: Boolean)
    fun onBatteryOptimizationToggled(enabled: Boolean)

    data class State(
        val isChatAnimationsEnabled: Boolean = true,
        val backgroundServiceEnabled: Boolean = true,
        val isPowerSavingModeEnabled: Boolean = false,
        val isWakeLockEnabled: Boolean = true,
        val batteryOptimizationEnabled: Boolean = false
    )
}

class DefaultPowerSavingComponent(
    context: AppComponentContext,
    private val appPreferences: AppPreferencesProvider = context.container.preferences.appPreferences,
    private val onBack: () -> Unit
) : PowerSavingComponent, AppComponentContext by context {

    private val _state = MutableValue(PowerSavingComponent.State())
    override val state: Value<PowerSavingComponent.State> = _state
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        appPreferences.isChatAnimationsEnabled.onEach {
            _state.value = _state.value.copy(isChatAnimationsEnabled = it)
        }.launchIn(scope)

        appPreferences.backgroundServiceEnabled.onEach {
            _state.value = _state.value.copy(backgroundServiceEnabled = it)
        }.launchIn(scope)

        appPreferences.isPowerSavingMode.onEach {
            _state.value = _state.value.copy(isPowerSavingModeEnabled = it)
        }.launchIn(scope)

        appPreferences.isWakeLockEnabled.onEach {
            _state.value = _state.value.copy(isWakeLockEnabled = it)
        }.launchIn(scope)

        appPreferences.batteryOptimizationEnabled.onEach {
            _state.value = _state.value.copy(batteryOptimizationEnabled = it)
        }.launchIn(scope)
    }

    override fun onBackClicked() {
        onBack()
    }

    override fun onChatAnimationsToggled(enabled: Boolean) {
        appPreferences.setChatAnimationsEnabled(enabled)
    }

    override fun onBackgroundServiceToggled(enabled: Boolean) {
        appPreferences.setBackgroundServiceEnabled(enabled)
    }

    override fun onPowerSavingModeToggled(enabled: Boolean) {
        appPreferences.setPowerSavingMode(enabled)
    }

    override fun onWakeLockToggled(enabled: Boolean) {
        appPreferences.setWakeLockEnabled(enabled)
    }

    override fun onBatteryOptimizationToggled(enabled: Boolean) {
        appPreferences.setBatteryOptimizationEnabled(enabled)
    }
}