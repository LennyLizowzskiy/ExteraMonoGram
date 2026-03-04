package org.monogram.domain.models

import org.monogram.domain.repository.ChatMemberStatus

data class GroupMemberModel(
    val user: UserModel,
    val rank: String? = null,
    val status: ChatMemberStatus? = null
)