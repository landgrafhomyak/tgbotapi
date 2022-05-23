package com.github.landgrafhomyak.telegrambotapi.objects

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed interface AnyChat {
    val chatId: ChatId
    val chatType: ChatType
}

@Serializable
enum class ChatType {
    @SerialName("private")
    PRIVATE,

    @SerialName("group")
    GROUP,

    @SerialName("supergroup")
    SUPERGROUP,

    @SerialName("channel")
    CHANNEL
}

sealed interface GroupCommon : AnyChat {
    val title: String
}

sealed interface SuperGroupCommon : AnyChat {
    val title: String
    val username: String?
}

sealed interface ChannelCommon : AnyChat {
    val title: String
    val username: String?
}

sealed interface PrivateChatCommon : AnyChat {
    val username: String?
    val firstName: String?
    val lastName: String?
}

private object ChatSerializer : JsonContentPolymorphicSerializer<Chat>(Chat::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Chat> =
        element.jsonObject["type"]?.jsonPrimitive
            .let { v ->
                return@let if (v?.isString == true)
                    v.toString()
                else
                    throw SerializationException("Can't get field 'chat_type' or it has invalid type")
            }
            .let { s -> Json.decodeFromString<ChatType>(s) }
            .let { t ->
                when (t) {
                    ChatType.GROUP      -> Group.serializer()
                    ChatType.SUPERGROUP -> SuperGroup.serializer()
                    ChatType.CHANNEL    -> Channel.serializer()
                    ChatType.PRIVATE    -> PrivateChat.serializer()
                }
            }
}

@Serializable(with = ChatSerializer::class)
sealed interface Chat : AnyChat

@Serializable
class Group(
    @SerialName("id")
    override val chatId: ChatId,
    @SerialName("type")
    override val chatType: ChatType,
    override val title: String
) : AnyChat, GroupCommon, Chat {
    init {
        if (this.chatType != ChatType.GROUP) throw IllegalArgumentException("'chatType' of Group must be ChatType.GROUP")
    }
}


@Serializable
class SuperGroup(
    @SerialName("id")
    override val chatId: ChatId,
    @SerialName("type")
    override val chatType: ChatType,
    override val title: String,
    override val username: String? = null
) : AnyChat, SuperGroupCommon, Chat {
    init {
        if (this.chatType != ChatType.SUPERGROUP) throw IllegalArgumentException("'chatType' of SuperGroup must be ChatType.SUPERGROUP")
    }
}

@Serializable
class Channel(
    @SerialName("id")
    override val chatId: ChatId,
    @SerialName("type")
    override val chatType: ChatType,
    override val title: String,
    override val username: String? = null
) : AnyChat, ChannelCommon, Chat {
    init {
        if (this.chatType != ChatType.CHANNEL) throw IllegalArgumentException("'chatType' of Channel must be ChatType.CHANNEL")
    }
}

@Serializable
class PrivateChat(
    @SerialName("id")
    override val chatId: ChatId,
    @SerialName("type")
    override val chatType: ChatType,
    override val username: String? = null,
    @SerialName("first_name")
    override val firstName: String? = null,
    @SerialName("last_name")
    override val lastName: String? = null
) : AnyChat, PrivateChatCommon, Chat {
    init {
        if (this.chatType != ChatType.PRIVATE) throw IllegalArgumentException("'chatType' of PrivateChat must be ChatType.PRIVATE")
    }
}