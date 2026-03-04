package org.monogram.domain.managers

interface ClipManager {
    fun copyToClipboard(tag: String, text: String)
}