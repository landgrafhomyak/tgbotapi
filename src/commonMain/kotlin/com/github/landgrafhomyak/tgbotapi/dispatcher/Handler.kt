package com.github.landgrafhomyak.tgbotapi.dispatcher

import com.github.landgrafhomyak.tgbotapi.Bot
import com.github.landgrafhomyak.tgbotapi.objects.Update

interface Handler {
    suspend fun Bot.process(trigger: Update)
}