@file:UseSerializers(InstantEpochSecondsSerializer::class)

package com.github.landgrafhomyak.tgbotapi.objects

import kotlinx.datetime.Instant
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.reflect.KClass

private object ForwardSerializer : JsonContentPolymorphicSerializer<Forward>(Forward::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Forward> =
        element.jsonObject.let { f ->
            when {
                "forward_from" in f        -> Forward.FromUser.serializer()
                "forward_sender_name" in f -> Forward.FromHiddenUser.serializer()
                "forward_from_chat" in f   ->
                    if ("forward_from_message_id" in f)
                        Forward.FromChannel.serializer()
                    else
                        Forward.FromChat.serializer()
                else                       -> throw SerializationException("Can't determine forward author")
            }
        }
}

@Serializable(with = ForwardSerializer::class)
sealed class Forward {
    abstract val date: Instant

    @Serializable
    class FromUser(
        @SerialName("forward_date")
        override val date: Instant,
        @SerialName("forward_from")
        val forwardFrom: UserOrBot
    ) : Forward()

    @Serializable
    class FromHiddenUser(
        @SerialName("forward_date")
        override val date: Instant,
        @SerialName("forward_sender_name")
        val senderName: String
    ) : Forward()

    @Serializable
    class FromChat(
        @SerialName("forward_date")
        override val date: Instant,
        @SerialName("forward_from_chat")
        val fromChat: SuperGroup,
        @SerialName("forward_signature")
        val signature: String? = null
    ) : Forward()

    @Serializable
    class FromChannel(
        @SerialName("forward_date")
        override val date: Instant,
        @SerialName("forward_from_chat")
        val fromChat: Channel,
        @SerialName("forward_from_message_id")
        val fromMessageId: MessageId,
        @SerialName("forward_signature")
        val signature: String? = null
    ) : Forward()
}

open class ForwardTransformerSerializer<T : Message>(
    targetSerializer: KSerializer<T>
) : JsonTransformingSerializer<T>(targetSerializer) {
    final override fun transformDeserialize(element: JsonElement): JsonElement {
        val newObject = element.jsonObject.toMutableMap()
        newObject
            .filterKeys { key -> key.startsWith("forward_") }
            .apply { if (isEmpty()) return@transformDeserialize element }
            .onEach { (key, _) -> newObject.remove(key) }
            .let { map -> JsonObject(map) }
            .also { obj -> newObject["forward"] = obj }
        return JsonObject(newObject)
    }

    final override fun transformSerialize(element: JsonElement): JsonElement {
        val newObject = element.jsonObject.toMutableMap()
        (newObject["forward"] ?: return element)
            .jsonObject
            .also { newObject.remove("forward") }
            .forEach { (key, value) -> newObject[key] = value }
        return JsonObject(newObject)
    }
}

private object MessageSourcePolymorphicSerializer : JsonContentPolymorphicSerializer<Message>(Message::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Message> =
        element.jsonObject.let { obj ->
            (obj["sender_chat"] ?: return@let UserMessage.serializer())
                .let { j -> Json.decodeFromJsonElement<Chat>(j) }
                .let { sc ->
                    when (sc) {
                        is SuperGroup -> AnonymousAdminMessage.serializer()
                        is Channel    -> ChannelPost.serializer()
                        else          -> throw SerializationException("Can't determine message source")
                    }
                }
        }
}

object MessageSerializer : ForwardTransformerSerializer<Message>(Message.serializer())

@Serializable(with = MessageSourcePolymorphicSerializer::class)
sealed interface Message {
    val id: MessageId
    val date: Instant
    val chat: Chat
    val replyToMessage: Message?
    val viaBot: Bot?
    val editDate: Instant?
    val hasProtectedContent: Boolean?
    val forward: Forward?
    val replyMarkup: InlineKeyboardMarkup?

    sealed interface Text : Message {
        val text: String
        val entities: Array<MessageEntity>
    }

    sealed interface Animation : Message {
        val animation: com.github.landgrafhomyak.tgbotapi.objects.Animation
        val caption: String
        val captionEntities: Array<MessageEntity>
    }

    sealed interface Audio : Message {
        val audio: com.github.landgrafhomyak.tgbotapi.objects.Audio
        val caption: String
        val captionEntities: Array<MessageEntity>
    }

    sealed interface Document : Message {
        val document: com.github.landgrafhomyak.tgbotapi.objects.Document
        val caption: String
        val captionEntities: Array<MessageEntity>
    }

    sealed interface Photo : Message {
        val photo: Array<PhotoSize>
        val caption: String
        val captionEntities: Array<MessageEntity>
    }

    sealed interface Sticker : Message {
        val sticker: com.github.landgrafhomyak.tgbotapi.objects.Sticker
    }

    sealed interface Video : Message {
        val video: com.github.landgrafhomyak.tgbotapi.objects.Video
        val caption: String
        val captionEntities: Array<MessageEntity>
    }

    sealed interface VideoNote : Message {
        val video: com.github.landgrafhomyak.tgbotapi.objects.VideoNote
    }

    sealed interface Voice : Message {
        val voice: com.github.landgrafhomyak.tgbotapi.objects.Voice
        val caption: String
        val captionEntities: Array<MessageEntity>
    }

    sealed interface Contact : Message {
        val contact: com.github.landgrafhomyak.tgbotapi.objects.Contact
    }

    sealed interface Dice : Message {
        val dice: com.github.landgrafhomyak.tgbotapi.objects.Dice
    }

    sealed interface Game : Message {
        val game: com.github.landgrafhomyak.tgbotapi.objects.Game
    }

    sealed interface Poll : Message {
        val poll: com.github.landgrafhomyak.tgbotapi.objects.Poll
    }
}

private abstract class MessageTypePolymorphicSerializer<
        T : Message,
        TEXT : T, ANIMATION : T, AUDIO : T, DOCUMENT : T, PHOTO : T, STICKER : T, VIDEO : T, VIDEO_NOTE : T, VOICE : T,
        CONTACT : T, DICE : T, GAME : T, POLL : T
        >(
    baseClass: KClass<T>,
    private val textSerializer: KSerializer<TEXT>,
    private val animationSerializer: KSerializer<ANIMATION>,
    private val audioSerializer: KSerializer<AUDIO>,
    private val documentSerializer: KSerializer<DOCUMENT>,
    private val photoSerializer: KSerializer<PHOTO>,
    private val stickerSerializer: KSerializer<STICKER>,
    private val videoSerializer: KSerializer<VIDEO>,
    private val videoNotesSerializer: KSerializer<VIDEO_NOTE>,
    private val voiceSerializer: KSerializer<VOICE>,
    private val contactSerializer: KSerializer<CONTACT>,
    private val diceSerializer: KSerializer<DICE>,
    private val gameSerializer: KSerializer<GAME>,
    private val pollSerializer: KSerializer<POLL>,
) : JsonContentPolymorphicSerializer<T>(baseClass) {

    @Suppress("UNCHECKED_CAST")
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out T> =
        element.jsonObject.let { o ->
            when {
                "text" in o       -> this.textSerializer
                "animation" in o  -> this.animationSerializer
                "audio" in o      -> this.audioSerializer
                "document" in o   -> this.documentSerializer
                "photo" in o      -> this.photoSerializer
                "sticker" in o    -> this.stickerSerializer
                "video" in o      -> this.videoSerializer
                "video_note" in o -> this.videoNotesSerializer
                "voice" in o      -> this.voiceSerializer
                "contact" in o    -> this.voiceSerializer
                "dice" in o       -> this.voiceSerializer
                "game" in o       -> this.voiceSerializer
                "poll" in o       -> this.voiceSerializer
                else              -> throw SerializationException("Can't determine message type")
            }
        }
}

private object UserMessageSerializer : MessageTypePolymorphicSerializer<
        UserMessage,
        UserTextMessage, UserAnimationMessage, UserAudioMessage, UserDocumentMessage, UserPhotoMessage, UserStickerMessage, UserVideoMessage, UserVideoNoteMessage, UserVoiceMessage,
        UserContactMessage, UserDiceMessage, UserGameMessage, UserPollMessage
        >(
    UserMessage::class,
    UserTextMessage.serializer(),
    UserAnimationMessage.serializer(),
    UserAudioMessage.serializer(),
    UserDocumentMessage.serializer(),
    UserPhotoMessage.serializer(),
    UserStickerMessage.serializer(),
    UserVideoMessage.serializer(),
    UserVideoNoteMessage.serializer(),
    UserVoiceMessage.serializer(),
    UserContactMessage.serializer(),
    UserDiceMessage.serializer(),
    UserGameMessage.serializer(),
    UserPollMessage.serializer(),
)

private object ChannelPostSerializer : MessageTypePolymorphicSerializer<
        ChannelPost,
        ChannelTextPost, ChannelAnimationPost, ChannelAudioPost, ChannelDocumentPost, ChannelPhotoPost, ChannelStickerPost, ChannelVideoPost, ChannelVideoNotePost, ChannelVoicePost,
        ChannelContactPost, ChannelDicePost, ChannelGamePost, ChannelPollPost
        >(
    ChannelPost::class,
    ChannelTextPost.serializer(),
    ChannelAnimationPost.serializer(),
    ChannelAudioPost.serializer(),
    ChannelDocumentPost.serializer(),
    ChannelPhotoPost.serializer(),
    ChannelStickerPost.serializer(),
    ChannelVideoPost.serializer(),
    ChannelVideoNotePost.serializer(),
    ChannelVoicePost.serializer(),
    ChannelContactPost.serializer(),
    ChannelDicePost.serializer(),
    ChannelGamePost.serializer(),
    ChannelPollPost.serializer(),
)

private object AnonymousAdminMessageSerializer : MessageTypePolymorphicSerializer<
        AnonymousAdminMessage,
        AnonymousAdminTextMessage, AnonymousAdminAnimationMessage, AnonymousAdminAudioMessage, AnonymousAdminDocumentMessage, AnonymousAdminPhotoMessage, AnonymousAdminStickerMessage, AnonymousAdminVideoMessage, AnonymousAdminVideoNoteMessage, AnonymousAdminVoiceMessage,
        AnonymousAdminContactMessage, AnonymousAdminDiceMessage, AnonymousAdminGameMessage, AnonymousAdminPollMessage
        >(
    AnonymousAdminMessage::class,
    AnonymousAdminTextMessage.serializer(),
    AnonymousAdminAnimationMessage.serializer(),
    AnonymousAdminAudioMessage.serializer(),
    AnonymousAdminDocumentMessage.serializer(),
    AnonymousAdminPhotoMessage.serializer(),
    AnonymousAdminStickerMessage.serializer(),
    AnonymousAdminVideoMessage.serializer(),
    AnonymousAdminVideoNoteMessage.serializer(),
    AnonymousAdminVoiceMessage.serializer(),
    AnonymousAdminContactMessage.serializer(),
    AnonymousAdminDiceMessage.serializer(),
    AnonymousAdminGameMessage.serializer(),
    AnonymousAdminPollMessage.serializer(),
)


@Serializable(with = UserMessageSerializer::class)
sealed class UserMessage : Message {
    abstract val from: UserOrBot
}

@Serializable(with = ChannelPostSerializer::class)
sealed class ChannelPost : Message {
    abstract val senderChat: Channel
    abstract val isAutomaticForward: Boolean
}

@Serializable(with = AnonymousAdminMessageSerializer::class)
sealed class AnonymousAdminMessage : Message {
    abstract val senderChat: Channel
}

@Serializable
private class UserTextMessage(
    @SerialName("message_id")
    override val id: MessageId,
    override val from: UserOrBot,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val text: String,
    override val entities: Array<MessageEntity> = arrayOf()
) : UserMessage(), Message.Text

@Serializable
private class ChannelTextPost(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    @SerialName("is_automatic_forward")
    override val isAutomaticForward: Boolean,
    override val text: String,
    override val entities: Array<MessageEntity> = arrayOf()
) : ChannelPost(), Message.Text

@Serializable
private class AnonymousAdminTextMessage(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val text: String,
    override val entities: Array<MessageEntity> = arrayOf()
) : AnonymousAdminMessage(), Message.Text


@Serializable
private class UserAnimationMessage(
    @SerialName("message_id")
    override val id: MessageId,
    override val from: UserOrBot,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val animation: Animation,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : UserMessage(), Message.Animation

@Serializable
private class ChannelAnimationPost(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    @SerialName("is_automatic_forward")
    override val isAutomaticForward: Boolean,
    override val animation: Animation,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : ChannelPost(), Message.Animation

@Serializable
private class AnonymousAdminAnimationMessage(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val animation: Animation,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : AnonymousAdminMessage(), Message.Animation

@Serializable
private class UserAudioMessage(
    @SerialName("message_id")
    override val id: MessageId,
    override val from: UserOrBot,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val audio: Audio,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : UserMessage(), Message.Audio

@Serializable
private class ChannelAudioPost(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    @SerialName("is_automatic_forward")
    override val isAutomaticForward: Boolean,
    override val audio: Audio,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : ChannelPost(), Message.Audio

@Serializable
private class AnonymousAdminAudioMessage(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val audio: Audio,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : AnonymousAdminMessage(), Message.Audio

@Serializable
private class UserDocumentMessage(
    @SerialName("message_id")
    override val id: MessageId,
    override val from: UserOrBot,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val document: Document,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : UserMessage(), Message.Document

@Serializable
private class ChannelDocumentPost(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    @SerialName("is_automatic_forward")
    override val isAutomaticForward: Boolean,
    override val document: Document,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : ChannelPost(), Message.Document

@Serializable
private class AnonymousAdminDocumentMessage(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val document: Document,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : AnonymousAdminMessage(), Message.Document

@Serializable
private class UserPhotoMessage(
    @SerialName("message_id")
    override val id: MessageId,
    override val from: UserOrBot,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val photo: Array<PhotoSize>,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : UserMessage(), Message.Photo

@Serializable
private class ChannelPhotoPost(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    @SerialName("is_automatic_forward")
    override val isAutomaticForward: Boolean,
    override val photo: Array<PhotoSize>,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : ChannelPost(), Message.Photo

@Serializable
private class AnonymousAdminPhotoMessage(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val photo: Array<PhotoSize>,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : AnonymousAdminMessage(), Message.Photo

@Serializable
private class UserStickerMessage(
    @SerialName("message_id")
    override val id: MessageId,
    override val from: UserOrBot,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val sticker: Sticker,
) : UserMessage(), Message.Sticker

@Serializable
private class ChannelStickerPost(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    @SerialName("is_automatic_forward")
    override val isAutomaticForward: Boolean,
    override val sticker: Sticker,
) : ChannelPost(), Message.Sticker

@Serializable
private class AnonymousAdminStickerMessage(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val sticker: Sticker,
) : AnonymousAdminMessage(), Message.Sticker

@Serializable
private class UserVideoMessage(
    @SerialName("message_id")
    override val id: MessageId,
    override val from: UserOrBot,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val video: Video,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : UserMessage(), Message.Video

@Serializable
private class ChannelVideoPost(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    @SerialName("is_automatic_forward")
    override val isAutomaticForward: Boolean,
    override val video: Video,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : ChannelPost(), Message.Video

@Serializable
private class AnonymousAdminVideoMessage(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val video: Video,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : AnonymousAdminMessage(), Message.Video

@Serializable
private class UserVideoNoteMessage(
    @SerialName("message_id")
    override val id: MessageId,
    override val from: UserOrBot,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val video: VideoNote,
) : UserMessage(), Message.VideoNote

@Serializable
private class ChannelVideoNotePost(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    @SerialName("is_automatic_forward")
    override val isAutomaticForward: Boolean,
    override val video: VideoNote,
) : ChannelPost(), Message.VideoNote

@Serializable
private class AnonymousAdminVideoNoteMessage(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val video: VideoNote,
) : AnonymousAdminMessage(), Message.VideoNote

@Serializable
private class UserVoiceMessage(
    @SerialName("message_id")
    override val id: MessageId,
    override val from: UserOrBot,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val voice: Voice,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : UserMessage(), Message.Voice

@Serializable
private class ChannelVoicePost(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    @SerialName("is_automatic_forward")
    override val isAutomaticForward: Boolean,
    override val voice: Voice,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : ChannelPost(), Message.Voice

@Serializable
private class AnonymousAdminVoiceMessage(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val voice: Voice,
    override val caption: String,
    @SerialName("caption_entities")
    override val captionEntities: Array<MessageEntity> = arrayOf()
) : AnonymousAdminMessage(), Message.Voice


@Serializable
private class UserContactMessage(
    @SerialName("message_id")
    override val id: MessageId,
    override val from: UserOrBot,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val contact: Contact
) : UserMessage(), Message.Contact

@Serializable
private class ChannelContactPost(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    @SerialName("is_automatic_forward")
    override val isAutomaticForward: Boolean,
    override val contact: Contact
) : ChannelPost(), Message.Contact

@Serializable
private class AnonymousAdminContactMessage(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val contact: Contact
) : AnonymousAdminMessage(), Message.Contact

@Serializable
private class UserDiceMessage(
    @SerialName("message_id")
    override val id: MessageId,
    override val from: UserOrBot,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val dice: Dice
) : UserMessage(), Message.Dice

@Serializable
private class ChannelDicePost(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    @SerialName("is_automatic_forward")
    override val isAutomaticForward: Boolean,
    override val dice: Dice
) : ChannelPost(), Message.Dice

@Serializable
private class AnonymousAdminDiceMessage(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val dice: Dice
) : AnonymousAdminMessage(), Message.Dice

@Serializable
private class UserGameMessage(
    @SerialName("message_id")
    override val id: MessageId,
    override val from: UserOrBot,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val game: Game
) : UserMessage(), Message.Game

@Serializable
private class ChannelGamePost(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    @SerialName("is_automatic_forward")
    override val isAutomaticForward: Boolean,
    override val game: Game
) : ChannelPost(), Message.Game

@Serializable
private class AnonymousAdminGameMessage(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val game: Game
) : AnonymousAdminMessage(), Message.Game

@Serializable
private class UserPollMessage(
    @SerialName("message_id")
    override val id: MessageId,
    override val from: UserOrBot,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val poll: Poll
) : UserMessage(), Message.Poll

@Serializable
private class ChannelPollPost(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    @SerialName("is_automatic_forward")
    override val isAutomaticForward: Boolean,
    override val poll: Poll
) : ChannelPost(), Message.Poll

@Serializable
private class AnonymousAdminPollMessage(
    @SerialName("message_id")
    override val id: MessageId,
    @SerialName("sender_chat")
    override val senderChat: Channel,
    override val date: Instant,
    override val chat: Chat,
    @SerialName("reply_to_message")
    override val replyToMessage: Message? = null,
    @SerialName("via_bot")
    override val viaBot: Bot? = null,
    @SerialName("edit_date")
    override val editDate: Instant? = null,
    @SerialName("has_protected_content")
    override val hasProtectedContent: Boolean? = null,
    override val forward: Forward? = null,
    @SerialName("reply_markup")
    override val replyMarkup: InlineKeyboardMarkup? = null,
    override val poll: Poll
) : AnonymousAdminMessage(), Message.Poll
