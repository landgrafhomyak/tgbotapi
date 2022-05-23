package com.github.landgrafhomyak.telegrambotapi.objects

import kotlinx.serialization.Serializable

@Serializable
class GetUpdates(
    val offset: UpdateId? = null,
    val limit: ULong? = null,
    val timeout: ULong? = null,
    @Suppress("RemoveRedundantQualifierName")
    val allowed_updates: Array<GetUpdates.Type>? = null
) {
    enum class Type {

    }
}