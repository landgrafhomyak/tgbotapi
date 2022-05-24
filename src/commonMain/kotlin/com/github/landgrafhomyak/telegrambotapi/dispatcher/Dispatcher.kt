package com.github.landgrafhomyak.telegrambotapi.dispatcher

import com.github.landgrafhomyak.telegrambotapi.Bot
import com.github.landgrafhomyak.telegrambotapi.objects.GetUpdates
import com.github.landgrafhomyak.telegrambotapi.objects.Update
import com.github.landgrafhomyak.telegrambotapi.objects.UpdateId

class Dispatcher(val bot: Bot) {
    private val handlers = mutableListOf<Handler>()
    fun register(handler: Handler) {
        this.handlers.add(handler)
    }

    suspend fun run(
        updatesPerRequest: ULong = 1u,
        onError: Throwable.() -> Unit = { this.printStackTrace() }
    ) {
        var lastId: UpdateId = 0
        fetching@ while (true) {
            val updates: Array<Update> = try {
                this.bot.getUpdates(
                    offset = lastId + 1,
                    limit = updatesPerRequest,
                    allowed_updates = GetUpdates.Type.values()
                )
            } catch (err: Throwable) {
                throw err
                @Suppress("UNREACHABLE_CODE")
                err.onError()
                @Suppress("UNREACHABLE_CODE")
                continue@fetching
            }

            updates@ for (upd in updates) {
                lastId = upd.id
                handlers@ for (handler in this.handlers) {
                    try {
                        handler.apply {
                            this@Dispatcher.bot.process(upd)
                        }
                    } catch (err: Throwable) {
                        err.onError()
                        continue@handlers
                    }
                }
            }
        }
    }

}