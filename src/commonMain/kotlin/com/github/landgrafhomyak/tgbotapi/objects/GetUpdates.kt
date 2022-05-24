package com.github.landgrafhomyak.tgbotapi.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GetUpdates(
    val offset: UpdateId? = null,
    val limit: ULong? = null,
    val timeout: ULong? = null,
    @Suppress("RemoveRedundantQualifierName")
    val allowed_updates: Array<GetUpdates.Type>? = null
) {
    @Serializable
    enum class Type {
        @SerialName("message")
        MESSAGE,

        @SerialName("edited_message")
        EDITED_MESSAGE,

        @SerialName("channel_post")
        CHANNEL_POST,

        @SerialName("edited_channel_post")
        EDITED_CHANNEL_POST,

        @SerialName("callback_query")
        CALLBACK_POST,
    }
}