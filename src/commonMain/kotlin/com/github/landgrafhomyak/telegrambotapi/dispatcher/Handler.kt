package com.github.landgrafhomyak.telegrambotapi.dispatcher

import com.github.landgrafhomyak.telegrambotapi.Bot
import com.github.landgrafhomyak.telegrambotapi.objects.Update

interface Handler {
    suspend fun Bot.process(trigger: Update)
}