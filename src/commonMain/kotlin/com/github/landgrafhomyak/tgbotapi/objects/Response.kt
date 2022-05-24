package com.github.landgrafhomyak.tgbotapi.objects

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = Response.Serializer::class)
sealed class Response<out T> {
    abstract val ok: Boolean

    @Serializable
    data class Successful<out T>(
        override val ok: Boolean,
        val result: T,
    ) : Response<T>()


    @Serializable
    data class Error<out T>(
        override val ok: Boolean,
        @SerialName("error_code")
        val errorCode: Long,
        val description: String? = null,
    ) : Response<T>()

    @Suppress("RemoveRedundantQualifierName")
    internal class Serializer(
        private val resultSerializer: KSerializer<*>
    ) : JsonContentPolymorphicSerializer<Response<*>>(Response::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Response<*>> =
            when (element.jsonObject["ok"]?.jsonPrimitive?.boolean) {
                true -> Response.Successful.serializer(this.resultSerializer)
                false -> Response.Error.serializer(this.resultSerializer)
                else -> throw SerializationException("Can't determine 'ok' value in response")
            }
    }
}