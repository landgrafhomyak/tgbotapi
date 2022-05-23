package com.github.landgrafhomyak.telegrambotapi.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class VideoNote(
    @SerialName("file_id")
    val fileId: String,
    @SerialName("file_unique_id")
    val fileUniqueId: String,
    val length: Long,
    val duration: Long,
    @SerialName("file_size")
    val fileSize: Long? = null
)