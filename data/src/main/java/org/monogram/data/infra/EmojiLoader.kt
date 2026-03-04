package org.monogram.data.infra

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import java.io.File

object EmojiLoader {
    private val EMOJI_RANGES = listOf(
        0x1F600..0x1F64F, // Emoticons
        0x1F300..0x1F5FF, // Misc Symbols and Pictographs
        0x1F680..0x1F6FF, // Transport and Map
        0x1F1E6..0x1F1FF, // Flags (Regional Indicator Symbols)
        0x1F900..0x1F9FF, // Supplemental Symbols and Pictographs
        0x2600..0x26FF,   // Misc symbols
        0x2700..0x27BF,   // Dingbats
        0x1F700..0x1F77F, // Alchemical
        0x1F780..0x1F7FF, // Geometric Shapes Ext
        0x1F800..0x1F8FF  // Supplemental Arrows
    )

    private var cachedEmojiList: List<String>? = null

    fun getSupportedEmojis(context: Context): List<String> {
        cachedEmojiList?.let { return it }

        val appleFile = File(context.filesDir, "fonts/apple.ttf")
        val typeface = if (appleFile.exists()) {
            Typeface.createFromFile(appleFile)
        } else {
            Typeface.DEFAULT
        }

        val paint = Paint().apply { this.typeface = typeface }
        val result = ArrayList<String>(1200)
        val vs16 = "\uFE0F"
        for (range in EMOJI_RANGES) {
            for (code in range) {
                val char = String(Character.toChars(code))
                when {
                    paint.hasGlyph(char + vs16) -> result.add(char + vs16)
                    paint.hasGlyph(char) -> result.add(char)
                }
            }
        }
        return result.distinct().also { cachedEmojiList = it }
    }
}