package org.monogram.domain.repository

interface ExternalNavigator {
    fun openUrl(url: String)
    val packageName: String
    fun navigateToLinkSettings()
    fun openOssLicenses()
}