package org.monogram.domain.models

sealed interface ReplyMarkupModel {
    data class InlineKeyboard(val rows: List<List<InlineKeyboardButtonModel>>) : ReplyMarkupModel
    data class ShowKeyboard(
        val rows: List<List<KeyboardButtonModel>>,
        val isPersistent: Boolean,
        val resizeKeyboard: Boolean,
        val oneTime: Boolean,
        val isPersonal: Boolean,
        val inputFieldPlaceholder: String
    ) : ReplyMarkupModel

    data class RemoveKeyboard(val isPersonal: Boolean) : ReplyMarkupModel
    data class ForceReply(val isPersonal: Boolean, val inputFieldPlaceholder: String) : ReplyMarkupModel
}

data class InlineKeyboardButtonModel(
    val text: String,
    val type: InlineKeyboardButtonType
)

sealed interface InlineKeyboardButtonType {
    data class Url(val url: String) : InlineKeyboardButtonType
    data class Callback(val data: ByteArray) : InlineKeyboardButtonType {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Callback

            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }

    data class WebApp(val url: String) : InlineKeyboardButtonType
    data class LoginUrl(val url: String, val id: Long) : InlineKeyboardButtonType
    data class SwitchInline(val query: String) : InlineKeyboardButtonType
    data class Buy(val slug: String? = null) : InlineKeyboardButtonType
    data class User(val userId: Long) : InlineKeyboardButtonType
    object Unsupported : InlineKeyboardButtonType
}

data class KeyboardButtonModel(
    val text: String,
    val type: KeyboardButtonType
)

sealed interface KeyboardButtonType {
    object Text : KeyboardButtonType
    object RequestPhoneNumber : KeyboardButtonType
    object RequestLocation : KeyboardButtonType
    data class RequestPoll(val forceQuiz: Boolean, val forceRegular: Boolean) : KeyboardButtonType
    data class WebApp(val url: String) : KeyboardButtonType
    data class RequestUsers(val id: Int) : KeyboardButtonType
    data class RequestChat(val id: Int) : KeyboardButtonType
    object Unsupported : KeyboardButtonType
}