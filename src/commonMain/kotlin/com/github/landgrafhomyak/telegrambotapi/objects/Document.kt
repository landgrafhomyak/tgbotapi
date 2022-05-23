package com.github.landgrafhomyak.telegrambotapi.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Document(
    @SerialName("file_id")
    val fileId: String,
    @SerialName("file_unique_id")
    val fileUniqueId: String,
    val thumb: PhotoSize? = null,
    @SerialName("file_name")
    val fileName: String? = null,
    @SerialName("mime_type")
    val mimeType: String? = null,
    @SerialName("file_size")
    val fileSize: Long? = null
)