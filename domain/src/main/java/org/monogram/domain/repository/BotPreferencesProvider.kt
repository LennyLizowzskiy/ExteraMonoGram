package org.monogram.domain.repository

interface BotPreferencesProvider {
    // WebApp Permissions
    fun getWebappPermission(botId: Long, permission: String): Boolean
    fun setWebappPermission(botId: Long, permission: String, granted: Boolean)
    fun isWebappPermissionRequested(botId: Long, permission: String): Boolean

    // WebApp Storage
    fun saveWebappData(key: String, value: String)
    fun getWebappData(key: String): String?
    fun getWebappData(keys: List<String>): Map<String, String?>
    fun deleteWebappData(key: String)
    fun deleteWebappData(keys: List<String>)
    fun getWebappDataKeys(): List<String>

    // WebApp Biometry
    fun getWebappBiometryDeviceId(botId: Long): String?
    fun saveWebappBiometryDeviceId(botId: Long, deviceId: String)
    fun isWebappBiometryAccessRequested(): Boolean
    fun setWebappBiometryAccessRequested(requested: Boolean)
}
