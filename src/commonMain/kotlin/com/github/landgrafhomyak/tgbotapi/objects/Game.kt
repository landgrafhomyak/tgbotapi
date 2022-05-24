package com.github.landgrafhomyak.tgbotapi.objects

import kotlinx.serialization.Serializable

@Serializable
object CallbackGame

@Serializable
class Game(
    val title: String,
    val description: String,
    val photo: Array<PhotoSize>,
    val text: String? = null
)