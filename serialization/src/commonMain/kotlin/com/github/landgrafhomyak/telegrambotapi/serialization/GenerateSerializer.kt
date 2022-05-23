package com.github.landgrafhomyak.telegrambotapi.serialization

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CONSTRUCTOR, AnnotationTarget.CLASS)
annotation class GenerateSerializer(val serializerName: String)
