package org.monogram.data.infra

import com.bettergram.core.DispatcherProvider
import com.bettergram.core.ScopeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DefaultScopeProvider(
    dispatcherProvider: DispatcherProvider
) : ScopeProvider {
    override val appScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + dispatcherProvider.default
    )
}