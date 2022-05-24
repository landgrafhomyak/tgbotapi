package com.github.landgrafhomyak.tgbotapi.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CallbackQuery(
    val id: String,
    val from: User,
    val message: Message? = null,
    @SerialName("inline_message_id")
    val inlineMessageId: String? = null,
    @SerialName("chat_instance")
    val chatInstance: String? = null,
//    @Serializable(with=InstantEpochSecondsSerializer::class)
    val data: String? = null,
    val gameShortName: String? = null
)