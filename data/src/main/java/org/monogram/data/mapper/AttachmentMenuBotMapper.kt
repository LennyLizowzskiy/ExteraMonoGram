package org.monogram.data.mapper

import org.drinkless.tdlib.TdApi
import org.monogram.domain.models.AttachMenuBotIconModel
import org.monogram.domain.models.AttachMenuBotModel

fun TdApi.AttachmentMenuBot.toDomain(): AttachMenuBotModel {
    return AttachMenuBotModel(
        botUserId = this.botUserId,
        name = this.name,
        icon = AttachMenuBotIconModel(name = this.name, icon = this.androidSideMenuIcon?.toDomain()),
        requestWriteAccess = this.requestWriteAccess,
        isAdded = this.isAdded,
        showInSideMenu = this.showInSideMenu,
        showInAttachMenu = this.showInAttachmentMenu,
        showInDefaultMenu = this.showInSideMenu
    )
}
