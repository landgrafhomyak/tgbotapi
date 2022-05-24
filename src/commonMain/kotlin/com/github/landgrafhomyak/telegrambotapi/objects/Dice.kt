package com.github.landgrafhomyak.telegrambotapi.objects

import kotlinx.serialization.Serializable

@Serializable
class Dice(
    val emoji: String,
    val value: Long
)