package com.github.landgrafhomyak.telegrambotapi.serialization

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

object SerializerGenerator : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val processedClasses = mutableSetOf<KSClassDeclaration>()
        for (d in resolver.getSymbolsWithAnnotation(GenerateSerializerByDiscriminator::class.qualifiedName!!, true)) {
            d.accept(SealedClassVisitor, processedClasses)
        }
        return emptyList()
    }
}