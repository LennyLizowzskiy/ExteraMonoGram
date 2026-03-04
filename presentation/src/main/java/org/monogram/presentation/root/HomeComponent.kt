package org.monogram.presentation.root

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface HomeComponent {
    val state: Value<State>
    fun onRetry()

    data class State(
        val isLoading: Boolean = false,
        val error: String? = null
    )
}

class DefaultHomeComponent(
    ctx: ComponentContext,
) : HomeComponent, ComponentContext by ctx {

    private val _state = MutableValue(HomeComponent.State(isLoading = true))
    override val state = _state
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        loadChats()
    }

    override fun onRetry() {
        loadChats()
    }

    private fun loadChats() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            _state.value = _state.value.copy(isLoading = false)
        }
    }
}

@Composable
fun HomeContent(component: HomeComponent) {
    val state by component.state.subscribeAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator()
            }

            if (!state.isLoading && state.error != null) {
                Text(
                    text = state.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (!state.isLoading && state.error == null) {

            }
        }
    }
}