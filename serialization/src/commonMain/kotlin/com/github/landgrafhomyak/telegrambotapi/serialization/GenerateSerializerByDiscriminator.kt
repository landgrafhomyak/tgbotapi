package com.github.landgrafhomyak.telegrambotapi.serialization

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class GenerateSerializerByDiscriminator(val field: String, val serializerObjectName: String)
