package com.bettergram.core

import kotlinx.coroutines.CoroutineScope

interface ScopeProvider {
    val appScope: CoroutineScope
}