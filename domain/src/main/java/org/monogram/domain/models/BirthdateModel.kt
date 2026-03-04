package org.monogram.domain.models

data class BirthdateModel(
    val day: Int,
    val month: Int,
    val year: Int? = null
)
