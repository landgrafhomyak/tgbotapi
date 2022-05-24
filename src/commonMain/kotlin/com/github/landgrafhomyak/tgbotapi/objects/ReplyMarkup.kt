package com.github.landgrafhomyak.tgbotapi.objects

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
class ReplyKeyboardMarkup(
    val keyboard: Array<Array<ReplyKeyboardButton>>,
    @SerialName("resize_keyboard")
    val resizeKeyboard: Boolean? = null,
    @SerialName("one_time_keyboard")
    val oneTimeKeyboard: Boolean? = null,
    @SerialName("input_field_placeholder")
    val inputFieldPlaceholder: String? = null,
    val selective: Boolean? = null
) : ReplyMarkup()

private object ReplyKeyboardButtonSerializer : JsonContentPolymorphicSerializer<ReplyKeyboardButton>(ReplyKeyboardButton::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out ReplyKeyboardButton> {
        return if (element is JsonPrimitive) {
            if (!element.isString)
                throw SerializationException("ReplyKeyboardButton must be string or object")
            SimpleButtonSerializer
        } else {
            val obj = element.jsonObject
            when {
                "request_contact" in obj  -> ReplyKeyboardButton.ContactRequest.serializer()
                "request_location" in obj -> ReplyKeyboardButton.LocationRequest.serializer()
                "request_poll" in obj     -> ReplyKeyboardButton.PollRequest.serializer()
                "web_app" in obj          -> ReplyKeyboardButton.WebApp.serializer()
                obj.keys == setOf("text") -> ReplyKeyboardButton.Text.serializer()
                else                      -> throw SerializationException("Unknown type of reply button")
            }
        }
    }

    private object SimpleButtonSerializer : KSerializer<ReplyKeyboardButton.Text> {
        override fun deserialize(decoder: Decoder): ReplyKeyboardButton.Text =
            ReplyKeyboardButton.Text(decoder.decodeString())


        override val descriptor: SerialDescriptor
            get() = ReplyKeyboardButton.Text.serializer().descriptor

        override fun serialize(encoder: Encoder, value: ReplyKeyboardButton.Text) =
            ReplyKeyboardButton.Text.serializer().serialize(encoder, value)

    }
}

@Serializable(with = ReplyKeyboardButtonSerializer::class)
sealed class ReplyKeyboardButton {
    abstract val text: String

    @Serializable
    class Text(
        override val text: String
    ) : ReplyKeyboardButton()

    @Serializable
    class ContactRequest(
        override val text: String,
        @SerialName("request_contact")
        val requestContact: Boolean
    ) : ReplyKeyboardButton()

    @Serializable
    class LocationRequest(
        override val text: String,
        @SerialName("request_location")
        val requestLocation: Boolean
    ) : ReplyKeyboardButton()

    @Serializable
    class PollRequest(
        override val text: String,
        @SerialName("request_poll")
        @Serializable(with = KeyboardButtonPollTypeSerializer::class)
        val requestPoll: PollType?
    ) : ReplyKeyboardButton()

    @Serializable
    class WebApp(
        override val text: String,
        @SerialName("web_app")
        val requestPoll: WebAppInfo
    ) : ReplyKeyboardButton()
}

private object KeyboardButtonPollTypeSerializer : KSerializer<PollType?> {
    override fun deserialize(decoder: Decoder): PollType? {
        if (decoder is JsonDecoder) {
            return Json.decodeFromJsonElement(decoder.decodeJsonElement().jsonObject["type"] ?: return null)
        } else {
            return decoder.decodeStructure(this.descriptor) {
                var type: PollType? = null
                loop@ while (true) {
                    when (val index = decodeElementIndex(this@KeyboardButtonPollTypeSerializer.descriptor)) {
                        0                            -> type = decodeSerializableElement(this@KeyboardButtonPollTypeSerializer.descriptor, 0, PollType.serializer())
                        CompositeDecoder.DECODE_DONE -> break@loop // https://youtrack.jetbrains.com/issue/KT-42262
                        else                         -> throw SerializationException("Unexpected index: $index")
                    }
                }
                return@decodeStructure type
            }
        }
    }

    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("KeyboardButtonPollType") {
            element<String>("type", isOptional = true)
        }

    override fun serialize(encoder: Encoder, value: PollType?) {
        encoder.encodeStructure(this.descriptor) {
            if (value != null) {
                encodeSerializableElement(this@KeyboardButtonPollTypeSerializer.descriptor, 0, PollType.serializer(), value)
            }
        }
    }
}


@Serializable
class RemoveReplyKeyboard(
    @SerialName("remove_keyboard")
    private val removeKeyboard: Boolean,
    val selective: Boolean
) : ReplyMarkup()

@Serializable
class InlineKeyboardMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: Array<Array<InlineKeyboardButton>>
) : ReplyMarkup()

private object InlineKeyboardButtonSerializer : JsonContentPolymorphicSerializer<InlineKeyboardButton>(InlineKeyboardButton::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out InlineKeyboardButton> =
        element.jsonObject.let { obj ->
            when {
                "url" in obj                              -> InlineKeyboardButton.Url.serializer()
                "callback_data" in obj                    -> InlineKeyboardButton.CallbackData.serializer()
                "web_app" in obj                          -> InlineKeyboardButton.WebApp.serializer()
                "login_url" in obj                        -> InlineKeyboardButton.LoginUrl.serializer()
                "switch_inline_query" in obj              -> InlineKeyboardButton.SwitchInlineQuery.serializer()
                "switch_inline_query_current_chat" in obj -> InlineKeyboardButton.SwitchInlineQueryCurrentChat.serializer()
                "callback_game" in obj                    -> InlineKeyboardButton.CallbackGame.serializer()
                "pay" in obj                              -> InlineKeyboardButton.Pay.serializer()
                else                                      -> throw SerializationException("Unknown type of inline button")
            }
        }
}

@Serializable(with = InlineKeyboardButtonSerializer::class)
sealed class InlineKeyboardButton {
    abstract val text: String

    @Serializable
    class Url(
        override val text: String,
        val url: String
    ) : InlineKeyboardButton()

    @Serializable
    class CallbackData(
        override val text: String,
        @SerialName("callback_data")
        val callbackData: String
    ) : InlineKeyboardButton()

    @Serializable
    class WebApp(
        override val text: String,
        @SerialName("web_app")
        val webApp: WebAppInfo
    ) : InlineKeyboardButton()

    @Serializable
    class LoginUrl(
        override val text: String,
        @SerialName("login_url")
        val loginUrl: com.github.landgrafhomyak.tgbotapi.objects.LoginUrl
    ) : InlineKeyboardButton()

    @Serializable
    class SwitchInlineQuery(
        override val text: String,
        @SerialName("switch_inline_query")
        val switchInlineQuery: String
    ) : InlineKeyboardButton()

    @Serializable
    class SwitchInlineQueryCurrentChat(
        override val text: String,
        @SerialName("switch_inline_query_current_chat")
        val switchInlineQueryCurrentChat: String
    ) : InlineKeyboardButton()

    @Serializable
    class CallbackGame(
        override val text: String,
        @SerialName("callback_game")
        val callbackGame: String
    ) : InlineKeyboardButton()

    @Serializable
    class Pay(
        override val text: String,
        val pay: Boolean
    ) : InlineKeyboardButton()
}


@Serializable
class ForceReply(
    @SerialName("force_reply")
    val forceReply: Boolean,
    @SerialName("input_field_placeholder")
    val inputFieldPlaceholder: String? = null,
    val selective: Boolean? = null
) : ReplyMarkup()

private object ReplyMarkupDeSerializer : JsonContentPolymorphicSerializer<ReplyMarkup>(ReplyMarkup::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out ReplyMarkup> {
        TODO("Not yet implemented")
    }

}
private object ReplyMarkupSerializer : KSerializer<ReplyMarkup> {
    override fun deserialize(decoder: Decoder): ReplyMarkup =
        ReplyMarkupDeSerializer.deserialize(decoder)

    override val descriptor: SerialDescriptor
        get() = ReplyMarkupDeSerializer.descriptor


    override fun serialize(encoder: Encoder, value: ReplyMarkup) =
        when (value) {
            is ReplyKeyboardMarkup  -> ReplyKeyboardMarkup.serializer().serialize(encoder, value)
            is ForceReply           -> ForceReply.serializer().serialize(encoder, value)
            is InlineKeyboardMarkup -> InlineKeyboardMarkup.serializer().serialize(encoder, value)
            is RemoveReplyKeyboard  -> RemoveReplyKeyboard.serializer().serialize(encoder, value)
        }
}

@Serializable(with = ReplyMarkupSerializer::class)
sealed class ReplyMarkup {
//    companion object {
//        fun serializer() :KSerializer<ReplyMarkup> = ReplyMarkupSerializer // https://github.com/Kotlin/kotlinx.serialization/issues/1386
//    }
}