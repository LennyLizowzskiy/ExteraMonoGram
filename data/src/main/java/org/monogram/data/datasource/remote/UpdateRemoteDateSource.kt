package org.monogram.data.datasource.remote

import org.monogram.domain.models.UpdateInfo

interface UpdateRemoteDateSource {
    suspend fun fetchLatestUpdate(): UpdateInfo?
    suspend fun getTdLibVersion(): String
    suspend fun getTdLibCommitHash(): String
}