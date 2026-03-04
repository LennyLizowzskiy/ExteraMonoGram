package org.monogram.app.di

import android.content.ClipData
import android.content.ClipboardManager
import org.monogram.domain.managers.ClipManager

class ClipManagerImpl(private val clipboardManager: ClipboardManager?) : ClipManager {
    override fun copyToClipboard(tag: String, text: String) {
        val clip = ClipData.newPlainText(tag, text)
        clipboardManager?.setPrimaryClip(clip)
    }
}