package com.github.landgrafhomyak.tgbotapi.objects

import kotlinx.serialization.Serializable

@Serializable
class Dice(
    val emoji: String,
    val value: Long
)