@file:Suppress("OPT_IN_IS_NOT_ENABLED")

package com.github.landgrafhomyak.tgbotapi.objects

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// TODO: waiting for serialization plugin support
private object MessageEntityDeSerializer : JsonContentPolymorphicSerializer<MessageEntity>(MessageEntity::class) {
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

private class AddTypeFieldSerializer<T : MessageEntity>(private val targetSerializer: KSerializer<T>) : JsonTransformingSerializer<T>(targetSerializer) {
    override fun transformSerialize(element: JsonElement): JsonElement =
        element.jsonObject.toMutableMap().apply { set("type", JsonPrimitive(this@AddTypeFieldSerializer.targetSerializer.descriptor.serialName)) }.run { JsonObject(this@run) }
}

private object MessageEntitySerializer : KSerializer<MessageEntity> {
    override fun deserialize(decoder: Decoder): MessageEntity = MessageEntityDeSerializer.deserialize(decoder)

    override val descriptor: SerialDescriptor
        get() = MessageEntityDeSerializer.descriptor

    override fun serialize(encoder: Encoder, value: MessageEntity) =
        when (value) {
            is MessageEntity.Mention       -> AddTypeFieldSerializer(MessageEntity.Mention.serializer()).serialize(encoder, value)
            is MessageEntity.Bold          -> AddTypeFieldSerializer(MessageEntity.Bold.serializer()).serialize(encoder, value)
            is MessageEntity.BotCommand    -> AddTypeFieldSerializer(MessageEntity.BotCommand.serializer()).serialize(encoder, value)
            is MessageEntity.CashTag       -> AddTypeFieldSerializer(MessageEntity.CashTag.serializer()).serialize(encoder, value)
            is MessageEntity.Code          -> AddTypeFieldSerializer(MessageEntity.Code.serializer()).serialize(encoder, value)
            is MessageEntity.Email         -> AddTypeFieldSerializer(MessageEntity.Email.serializer()).serialize(encoder, value)
            is MessageEntity.HashTag       -> AddTypeFieldSerializer(MessageEntity.HashTag.serializer()).serialize(encoder, value)
            is MessageEntity.Italic        -> AddTypeFieldSerializer(MessageEntity.Italic.serializer()).serialize(encoder, value)
            is MessageEntity.PhoneNumber   -> AddTypeFieldSerializer(MessageEntity.PhoneNumber.serializer()).serialize(encoder, value)
            is MessageEntity.Pre           -> AddTypeFieldSerializer(MessageEntity.Pre.serializer()).serialize(encoder, value)
            is MessageEntity.Spoiler       -> AddTypeFieldSerializer(MessageEntity.Spoiler.serializer()).serialize(encoder, value)
            is MessageEntity.Strikethrough -> AddTypeFieldSerializer(MessageEntity.Strikethrough.serializer()).serialize(encoder, value)
            is MessageEntity.TextLink      -> AddTypeFieldSerializer(MessageEntity.TextLink.serializer()).serialize(encoder, value)
            is MessageEntity.TextMention   -> AddTypeFieldSerializer(MessageEntity.TextMention.serializer()).serialize(encoder, value)
            is MessageEntity.Underline     -> AddTypeFieldSerializer(MessageEntity.Underline.serializer()).serialize(encoder, value)
            is MessageEntity.Url           -> AddTypeFieldSerializer(MessageEntity.Url.serializer()).serialize(encoder, value)
        }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = MessageEntitySerializer::class)
@JsonClassDiscriminator("type")
sealed class MessageEntity {
    abstract val offset: ULong
    abstract val length: ULong

    @Serializable
    @SerialName("mention")
    class Mention(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("hashtag")
    class HashTag(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Suppress("SpellCheckingInspection")
    @Serializable
    @SerialName("cashtag")
    class CashTag(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("bot_command")
    class BotCommand(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("url")
    class Url(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("email")
    class Email(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("phone_number")
    class PhoneNumber(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("bold")
    class Bold(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("italic")
    class Italic(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("underline")
    class Underline(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("strikethrough")
    class Strikethrough(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("spoiler")
    class Spoiler(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("code")
    class Code(override val offset: ULong, override val length: ULong) : MessageEntity()

    @Serializable
    @SerialName("pre")
    class Pre(override val offset: ULong, override val length: ULong, val language: String) : MessageEntity()

    @Serializable
    @SerialName("text_link")
    class TextLink(override val offset: ULong, override val length: ULong, val url: String) : MessageEntity()

    @Serializable
    @SerialName("text_mention")
    class TextMention(override val offset: ULong, override val length: ULong, val user: User) : MessageEntity()
}