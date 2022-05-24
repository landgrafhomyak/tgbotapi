package com.github.landgrafhomyak.telegrambotapi.objects

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@Serializable
internal class SendMessage(
    @SerialName("chat_id")
    val chatId: ChatIdOrUsername,
    val text: String,
    @SerialName("parse_mode")
    val parseMode: String? = null,
    val entities: Array<MessageEntity>? = null,
    @SerialName("disable_web_page_preview")
    val disableWebPagePreview: Boolean? = null,
    @SerialName("disable_notification")
    val disableNotification: Boolean? = null,
    @SerialName("protect_content")
    val protectContent: Boolean? = null,
    @SerialName("reply_to_message_id")
    val replyToMessageId: Long? = null,
    @SerialName("allow_sending_without_reply")
    val allowSendingWithoutReply: Boolean? = null,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null
)

private class ChatIdOrUsernameSerializer : KSerializer<ChatIdOrUsername> {
    override fun deserialize(decoder: Decoder): ChatIdOrUsername {
        throw SerializationException()
    }

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("ChatIdOrUsername", kind = SerialKind.CONTEXTUAL)

    override fun serialize(encoder: Encoder, value: ChatIdOrUsername) =
        when (value) {
            is ChatIdOrUsername.Id       -> encoder.encodeLong(value.value)
            is ChatIdOrUsername.Username -> encoder.encodeString(value.value)
        }

}

@Serializable(with = ChatIdOrUsernameSerializer::class)
internal sealed interface ChatIdOrUsername {
    @JvmInline
    value class Id(val value: ChatId) : ChatIdOrUsername

    @JvmInline
    value class Username(val value: String) : ChatIdOrUsername
}