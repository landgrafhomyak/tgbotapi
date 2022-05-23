package com.github.landgrafhomyak.telegrambotapi.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Audio(
    @SerialName("file_id")
    val fileId: String,
    @SerialName("file_unique_id")
    val fileUniqueId: String,
    val duration: Long,
    val performer: Long,
    val title: String? = null,
    val fileName: String? = null,
    val mimeType: String? = null,
    val fileSize: Long? = null,
    val thumb: PhotoSize? = null
)