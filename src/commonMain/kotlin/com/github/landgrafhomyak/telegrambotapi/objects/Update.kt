@file:UseSerializers(MessageSerializer::class)

package com.github.landgrafhomyak.telegrambotapi.objects

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Suppress("RemoveRedundantQualifierName")
private object UpdateSerializer : JsonContentPolymorphicSerializer<Update>(Update::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Update> =
        element.jsonObject.let { obj ->
            when {
                "message" in obj             -> Update.Message.serializer()
                "edited_message" in obj      -> Update.EditedMessage.serializer()
                "channel_post" in obj        -> Update.ChannelPost.serializer()
                "edited_channel_post" in obj -> Update.EditedChannelPost.serializer()
                "inline_query" in obj        -> Update.InlineQuery.serializer()
                "callback_query" in obj      -> Update.CallbackQuery.serializer()
                "shipping_query" in obj      -> Update.ShippingQuery.serializer()
                "pre_checkout_query" in obj  -> Update.PreCheckoutQuery.serializer()
                "poll" in obj                -> Update.Poll.serializer()
                "poll_answer" in obj         -> Update.PollAnswer.serializer()
                "my_chat_member" in obj      -> Update.MyChatMember.serializer()
                "chat_member" in obj         -> Update.ChatMember.serializer()
                "chat_join_request" in obj   -> Update.ChatJoinRequest.serializer()
                else                         -> throw SerializationException("Can't determine update type")
            }
        }
}

@Serializable(with = UpdateSerializer::class)
sealed class Update {
    abstract val id: Long

    @Serializable
    class Message(
        @SerialName("update_id")
        override val id: Long,
        val message: com.github.landgrafhomyak.telegrambotapi.objects.Message
    ) : Update()


    @Serializable
    class EditedMessage(
        @SerialName("update_id")
        override val id: Long,
        @SerialName("edited_message")
        val editedMessage: com.github.landgrafhomyak.telegrambotapi.objects.Message
    ) : Update()

    @Serializable
    class ChannelPost(
        @SerialName("update_id")
        override val id: Long,
        @SerialName("channel_post")
        val channelPost: com.github.landgrafhomyak.telegrambotapi.objects.ChannelPost
    ) : Update()

    @Serializable
    class EditedChannelPost(
        @SerialName("update_id")
        override val id: Long,
        @SerialName("edited_channel_post")
        val editedChannelPost: com.github.landgrafhomyak.telegrambotapi.objects.ChannelPost
    ) : Update()

    @Serializable
    class InlineQuery(
        @SerialName("update_id")
        override val id: Long,
        @SerialName("inline_query")
        val inlineQuery: Unit
    ) : Update()

    @Serializable
    class CallbackQuery(
        @SerialName("update_id")
        override val id: Long,
        @SerialName("callback_query")
        val callbackQuery: Unit
    ) : Update()

    @Serializable
    class ShippingQuery(
        @SerialName("update_id")
        override val id: Long,
        @SerialName("shipping_query")
        val shippingQuery: Unit
    ) : Update()

    @Serializable
    class PreCheckoutQuery(
        @SerialName("update_id")
        override val id: Long,
        @SerialName("pre_checkout_query")
        val preCheckoutQuery: Unit
    ) : Update()

    @Serializable
    class Poll(
        @SerialName("update_id")
        override val id: Long,
        @SerialName("poll")
        val poll: Unit
    ) : Update()

    @Serializable
    class PollAnswer(
        @SerialName("update_id")
        override val id: Long,
        @SerialName("poll_answer")
        val poll: Unit
    ) : Update()

    @Serializable
    class MyChatMember(
        @SerialName("update_id")
        override val id: Long,
        @SerialName("my_chat_member")
        val myChatMember: Unit
    ) : Update()

    @Serializable
    class ChatMember(
        @SerialName("update_id")
        override val id: Long,
        @SerialName("chat_member")
        val chatMember: Unit
    ) : Update()

    @Serializable
    class ChatJoinRequest(
        @SerialName("update_id")
        override val id: Long,
        @SerialName("chat_join_request")
        val chatMember: Unit
    ) : Update()


}