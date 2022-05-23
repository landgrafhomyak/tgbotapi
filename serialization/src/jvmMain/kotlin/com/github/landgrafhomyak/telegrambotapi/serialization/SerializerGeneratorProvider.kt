package com.github.landgrafhomyak.telegrambotapi.serialization

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import java.io.File

internal class SerializerGeneratorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        SerializerGenerator.also { println("fghjk") }//.also { File("C:\\projets\\itmo-fb-bot\\ksp.log").createNewFile() }
}