package com.github.landgrafhomyak.telegrambotapi.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class LoginUrl(
    val url: String,
    @SerialName("forward_text")
    val forwardText: String? = null,
    @SerialName("bot_username")
    val botUsername: String? = null,
    @SerialName("request_write_access")
    val requestWriteAccess: Boolean? = null
)