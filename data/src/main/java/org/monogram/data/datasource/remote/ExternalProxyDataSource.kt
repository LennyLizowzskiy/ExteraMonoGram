package org.monogram.data.datasource.remote

interface ExternalProxyDataSource {
    suspend fun fetchProxyUrls(): List<String>
}