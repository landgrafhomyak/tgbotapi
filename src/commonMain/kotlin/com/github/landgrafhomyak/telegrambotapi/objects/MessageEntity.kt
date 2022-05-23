@file:Suppress("OPT_IN_IS_NOT_ENABLED")

package com.github.landgrafhomyak.telegrambotapi.objects

import com.github.landgrafhomyak.telegrambotapi.serialization.GenerateSerializer
import com.github.landgrafhomyak.telegrambotapi.serialization.GenerateSerializerByDiscriminator
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// TODO: waiting for serialization plugin support
private object MessageEntitySerializer : JsonContentPolymorphicSerializer<MessageEntity>(MessageEntity::class) {
    @OptIn(ExperimentalSerializationApi::class)
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out MessageEntity> =
        element.jsonObject["type"]?.jsonPrimitive?.content.let { t ->
            // TODO: replace with sealedSubclasses in Kotlin/JS
            for (serializer in setOf(
                MessageEntity.Mention.serializer(),
                MessageEntity.HashTag.serializer(),
                MessageEntity.CashTag.serializer(),
                MessageEntity.BotCommand.serializer(),
                MessageEntity.Url.serializer(),
                MessageEntity.Email.serializer(),
                MessageEntity.PhoneNumber.serializer(),
                MessageEntity.Bold.serializer(),
                MessageEntity.Italic.serializer(),
                MessageEntity.Underline.serializer(),
                MessageEntity.PhoneNumber.serializer(),
                MessageEntity.Strikethrough.serializer(),
                MessageEntity.Spoiler.serializer(),
                MessageEntity.Code.serializer(),
                MessageEntity.Pre.serializer(),
                MessageEntity.TextLink.serializer(),
                MessageEntity.TextMention.serializer(),
            )) {
                if (serializer.descriptor.serialName == t) {
                    return@let SuppressTypeFieldSerializer(serializer)
                }
            }
            throw SerializationException("Unknown message entity type")
        }
}

private class SuppressTypeFieldSerializer<T : MessageEntity>(targetSerializer: KSerializer<T>) : JsonTransformingSerializer<T>(targetSerializer) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.jsonObject.toMutableMap().apply { remove("type") }.run { JsonObject(this@run) }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = MessageEntitySerializer::class)
@GenerateSerializerByDiscriminator("type", "MessageEntitySerializer")
sealed class MessageEntity {
    abstract val offset: ULong
    abstract val length: ULong

    //    @Serializable(with=MessageEntityMentionSerializer::class)
    @Serializable
    @SerialName("mention")
    @GenerateSerializer("MessageEntityMentionSerializer")
    class Mention(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("hashtag")
    @GenerateSerializer("MessageEntityHashTagSerializer")
    class HashTag(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Suppress("SpellCheckingInspection")
    @Serializable
    @SerialName("cashtag")
    @GenerateSerializer("MessageEntityCachTagSerializer")
    class CashTag(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("bot_command")
    @GenerateSerializer("MessageEntityBotCommandSerializer")
    class BotCommand(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("url")
    @GenerateSerializer("MessageEntityUrlSerializer")
    class Url(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("email")
    @GenerateSerializer("MessageEntityEmailSerializer")
    class Email(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("phone_number")
    @GenerateSerializer("MessageEntityPhoneNumberSerializer")
    class PhoneNumber(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("bold")
    @GenerateSerializer("MessageEntityBoldSerializer")
    class Bold(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("italic")
    @GenerateSerializer("MessageEntityItalicSerializer")
    class Italic(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("underline")
    @GenerateSerializer("MessageEntityUnderlineSerializer")
    class Underline(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("strikethrough")
    @GenerateSerializer("MessageEntityStrikethroughSerializer")
    class Strikethrough(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("spoiler")
    @GenerateSerializer("MessageEntitySpoilerSerializer")
    class Spoiler(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("code")
    @GenerateSerializer("MessageEntityCodeSerializer")
    class Code(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("pre")
    @GenerateSerializer("MessageEntityPreSerializer")
    class Pre(override val offset: ULong, override val length: ULong, val language: String) : MessageEntity()

    @Serializable
    @SerialName("text_link")
    @GenerateSerializer("MessageEntityTextLinkSerializer")
    class TextLink(override val offset: ULong, override val length: ULong, val url: String) : MessageEntity()

    @Serializable
    @SerialName("text_mention")
    @GenerateSerializer("MessageEntityTextMentionSerializer")
    class TextMention(override val offset: ULong, override val length: ULong, val user: User) : MessageEntity()
}