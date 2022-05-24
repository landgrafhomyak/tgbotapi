package com.github.landgrafhomyak.tgbotapi.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Sticker(
    @SerialName("file_id")
    val fileId: String,
    @SerialName("file_unique_id")
    val fileUniqueId: String,
    val width: Long,
    val height: Long,
    @SerialName("is_animated")
    val isAnimation: Boolean,
    @SerialName("is_video")
    val isVideo: Boolean,
    val thumb: PhotoSize? = null,
    val emoji: String? = null,
    @SerialName("set_name")
    val setName: String? = null,
    @SerialName("file_size")
    val fileSize: Long? = null
)