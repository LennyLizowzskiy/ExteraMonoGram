package org.monogram.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.monogram.domain.models.UpdateState

interface UpdateRepository {
    val updateState: StateFlow<UpdateState>
    suspend fun checkForUpdates()
    fun downloadUpdate()
    fun cancelDownload()
    fun installUpdate()
    suspend fun getTdLibVersion(): String
    suspend fun getTdLibCommitHash(): String
}
