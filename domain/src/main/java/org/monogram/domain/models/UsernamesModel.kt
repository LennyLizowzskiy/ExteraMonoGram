package org.monogram.domain.models

data class UsernamesModel(
    val activeUsernames: List<String> = emptyList(),
    val disabledUsernames: List<String> = emptyList(),
    val collectibleUsernames: List<String> = emptyList()
)
