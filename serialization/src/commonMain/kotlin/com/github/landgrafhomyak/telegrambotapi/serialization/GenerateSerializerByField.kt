package com.github.landgrafhomyak.telegrambotapi.serialization

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class GenerateSerializerByField(val serializerObjectName: String)
