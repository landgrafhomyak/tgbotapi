package com.github.landgrafhomyak.tgbotapi.errors

@Suppress("MemberVisibilityCanBePrivate", "unused", "CanBeParameter")
open class ApiError(
    val methodName: String,
    val errorCode: Long,
    val description: String?
) : RuntimeException("$methodName: [$errorCode]" + (description?.run { " ${this@run}" } ?: ""))