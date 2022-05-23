@file:Suppress("OPT_IN_IS_NOT_ENABLED")

package com.github.landgrafhomyak.telegrambotapi

import com.github.landgrafhomyak.telegrambotapi.errors.*
import com.github.landgrafhomyak.telegrambotapi.objects.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.contracts.*

class TelegramBotMethods<EngineType: HttpClientEngineConfig>(private val token: String, val engine: HttpClientEngineFactory<EngineType>) {
    private val client = HttpClient(engine) {
        install(ContentNegotiation) {
            json(
                Json {
                    encodeDefaults = false
                }
            )
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun defaultErrorFactory(method: String, response: Response.Error<*>): Nothing =
        throw ApiError(method, response.errorCode, response.description)

    @OptIn(ExperimentalContracts::class)
    private suspend inline fun <reified I, reified O> request(
        method: String,
        data: I,
        onError: (Response.Error<O>) -> Nothing = { response -> this.defaultErrorFactory(method, response) }
    ): O {
        contract {
            callsInPlace(onError, InvocationKind.AT_MOST_ONCE)
        }

        when (val response = this.client.post("https://api.telegram.org/bot${this.token}/${method}") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }.body<Response<O>>()) {
            is Response.Successful -> return response.result
            is Response.Error      -> onError(response)
        }
    }

    @Suppress("RemoveExplicitTypeArguments")
    suspend fun getUpdates(
        offset: UpdateId? = null,
        limit: ULong? = null,
        timeout: ULong? = null,
        allowed_updates: Array<GetUpdates.Type>? = null
    ): Array<Update> = this.request<GetUpdates, Array<Update>>(
        "getUpdates", GetUpdates(
            offset = offset,
            limit = limit,
            timeout = timeout,
            allowed_updates = allowed_updates
        )
    )

    @Suppress("RemoveExplicitTypeArguments")
    suspend fun getMe(): BotSelf = this.request<GetMe, BotSelf>("getMe", GetMe)
}