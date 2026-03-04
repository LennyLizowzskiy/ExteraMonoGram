package org.monogram.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AttachMenuBotModel(
    val botUserId: Long,
    val name: String,
    val icon: AttachMenuBotIconModel?,
    val requestWriteAccess: Boolean,
    val isAdded: Boolean,
    val showInSideMenu: Boolean,
    val showInDefaultMenu: Boolean,
    val showInAttachMenu: Boolean
)

@Serializable
data class AttachMenuBotIconModel(
    val name: String,
    val icon: FileModel?
)
