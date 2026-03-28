package org.monogram.data.mapper

private val VERIFIED_CHAT_IDS = setOf(
    -1003615282448L,
    -1003768707135L,
    -1003566234286L,
    -1001270834900L,
    -1001336987857L
)

private val VERIFIED_USER_IDS = setOf(
    453024846L,
    665275967L,
    1250144551L,
    454755463L
)

fun isForcedVerifiedChat(chatId: Long): Boolean = chatId in VERIFIED_CHAT_IDS

fun isForcedVerifiedUser(userId: Long): Boolean = userId in VERIFIED_USER_IDS
