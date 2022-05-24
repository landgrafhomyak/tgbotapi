package com.github.landgrafhomyak.tgbotapi.objects

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed interface UserOrAnyBot {
    val id: UserId
    val isBot: Boolean
    val username: String?
    val firstName: String
}

private object UserOrBotSerializer : JsonContentPolymorphicSerializer<UserOrBot>(UserOrBot::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out UserOrBot> =
        when (element.jsonObject["is_bot"]?.jsonPrimitive?.boolean) {
            true  -> Bot.serializer()
            false -> User.serializer()
            else  -> throw SerializationException("Can't determine is user bot or not")
        }
}

@Serializable(with = UserOrBotSerializer::class)
sealed interface UserOrBot : UserOrAnyBot


@Serializable
class User(
    override val id: UserId,
    @SerialName("is_bot")
    override val isBot: Boolean,
    override val username: String? = null,
    @SerialName("first_name")
    override val firstName: String,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("language_code")
    val languageCode: String? = null
) : UserOrAnyBot, UserOrBot {
    init {
        if (this.isBot) throw IllegalArgumentException("Field 'isBot' must be false for real users")
    }
}

@Serializable
class Bot(
    override val id: UserId,
    @SerialName("is_bot")
    override val isBot: Boolean,
    override val username: String,
    @SerialName("first_name")
    override val firstName: String
) : UserOrAnyBot, UserOrBot {
    init {
        if (!this.isBot) throw IllegalArgumentException("Field 'isBot' must be true for bot users")
    }
}

@Serializable
class BotSelf(
    override val id: UserId,
    @SerialName("is_bot")
    override val isBot: Boolean,
    override val username: String,
    @SerialName("first_name")
    override val firstName: String,
    @SerialName("can_join_groups")
    val canJoinGroups: Boolean,
    @SerialName("can_read_all_group_messages")
    val canReadAllGroupMessages: Boolean,
    @SerialName("supports_inline_queries")
    val supportsInlineQueries: Boolean
) : UserOrAnyBot {
    init {
        if (!this.isBot) throw IllegalArgumentException("Field 'isBot' must be true for bot users")
    }
}