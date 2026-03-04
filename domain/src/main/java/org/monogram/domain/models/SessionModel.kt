package org.monogram.domain.models

data class SessionModel(
    val id: Long,
    val isCurrent: Boolean,
    val isPasswordPending: Boolean,
    val isUnconfirmed: Boolean,
    val applicationName: String,
    val applicationVersion: String,
    val deviceModel: String,
    val platform: String,
    val systemVersion: String,
    val logInDate: Int,
    val lastActiveDate: Int,
    val ipAddress: String,
    val location: String,
    val isOfficial: Boolean,
    val type: SessionType
)

enum class SessionType {
    Android, Apple, Brave, Chrome, Edge, Firefox,
    Ipad, Iphone, Linux, Mac, Opera, Safari,
    Ubuntu, Unknown, Vivaldi, Windows, Xbox
}