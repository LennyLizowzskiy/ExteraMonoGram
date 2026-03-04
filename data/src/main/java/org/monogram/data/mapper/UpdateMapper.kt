package org.monogram.data.mapper

import org.drinkless.tdlib.TdApi
import org.monogram.domain.models.MessageEntity
import org.monogram.domain.models.MessageEntityType
import org.monogram.domain.models.RichText
import org.monogram.domain.models.UpdateInfo

fun TdApi.FormattedText.toChangelog(): List<RichText> {
    val text = this.text
    val markerIndex = text.indexOf("Changelog:", ignoreCase = true)
        .takeIf { it != -1 } ?: return emptyList()

    val changelogText = text.substring(markerIndex + "Changelog:".length).trimStart()
    val actualStart = text.indexOf(changelogText, markerIndex + "Changelog:".length)
    var currentOffset = actualStart

    return changelogText.lines().mapNotNull { line ->
        val trimmed = line.trim()
        val lineStart = text.indexOf(line, currentOffset)
        val trimmedStart = lineStart + line.indexOf(trimmed)
        currentOffset = lineStart + line.length

        if (trimmed.isEmpty()) return@mapNotNull null

        val numberingMatch = Regex("""^\d+\.\s*""").find(trimmed)
        val (finalText, finalStart) = if (numberingMatch != null) {
            trimmed.substring(numberingMatch.value.length) to (trimmedStart + numberingMatch.value.length)
        } else {
            trimmed to trimmedStart
        }

        val entities = this.entities?.mapNotNull { entity ->
            val overlapStart = maxOf(entity.offset, finalStart)
            val overlapEnd = minOf(entity.offset + entity.length, finalStart + finalText.length)
            if (overlapStart >= overlapEnd) return@mapNotNull null
            entity.toDomain()?.copy(
                offset = overlapStart - finalStart,
                length = overlapEnd - overlapStart
            )
        } ?: emptyList()

        RichText(finalText, entities)
    }
}

fun TdApi.TextEntity.toDomain(): MessageEntity? {
    val type = when (val t = this.type) {
        is TdApi.TextEntityTypeBold -> MessageEntityType.Bold
        is TdApi.TextEntityTypeItalic -> MessageEntityType.Italic
        is TdApi.TextEntityTypeUnderline -> MessageEntityType.Underline
        is TdApi.TextEntityTypeStrikethrough -> MessageEntityType.Strikethrough
        is TdApi.TextEntityTypeSpoiler -> MessageEntityType.Spoiler
        is TdApi.TextEntityTypeCode -> MessageEntityType.Code
        is TdApi.TextEntityTypePre -> MessageEntityType.Pre()
        is TdApi.TextEntityTypeTextUrl -> MessageEntityType.TextUrl(t.url)
        is TdApi.TextEntityTypeMention -> MessageEntityType.Mention
        is TdApi.TextEntityTypeMentionName -> MessageEntityType.TextMention(t.userId)
        is TdApi.TextEntityTypeHashtag -> MessageEntityType.Hashtag
        is TdApi.TextEntityTypeBotCommand -> MessageEntityType.BotCommand
        is TdApi.TextEntityTypeUrl -> MessageEntityType.Url
        is TdApi.TextEntityTypeEmailAddress -> MessageEntityType.Email
        is TdApi.TextEntityTypePhoneNumber -> MessageEntityType.PhoneNumber
        is TdApi.TextEntityTypeBankCardNumber -> MessageEntityType.BankCardNumber
        is TdApi.TextEntityTypeCustomEmoji -> MessageEntityType.CustomEmoji(t.customEmojiId)
        else -> return null
    }
    return MessageEntity(this.offset, this.length, type)
}

fun TdApi.MessageDocument.toUpdateInfo(): UpdateInfo? {
    val text = this.caption.text
    val match = Regex("""(\d+\.\d+\.\d+)\s*\((\d+)\)""").find(text) ?: return null
    return UpdateInfo(
        version = match.groupValues[1],
        versionCode = match.groupValues[2].toInt(),
        description = text,
        changelog = this.caption.toChangelog(),
        fileId = this.document.document.id,
        fileName = this.document.fileName,
        fileSize = this.document.document.size
    )
}