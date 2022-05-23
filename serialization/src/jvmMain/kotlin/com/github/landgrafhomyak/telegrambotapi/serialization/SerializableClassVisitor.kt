package com.github.landgrafhomyak.telegrambotapi.serialization

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.visitor.KSDefaultVisitor

object SerializableClassVisitor: KSDefaultVisitor<ClassDescription, Unit>() {
    override fun defaultHandler(node: KSNode, data: ClassDescription) {}
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: ClassDescription) {
        super.visitClassDeclaration(classDeclaration, data)
        classDeclaration.superTypes
            .asSequence()
            .map { cls -> cls.resolve().declaration }
            .forEach { cls -> cls.accept(SerializableClassVisitor, data) }
    }

    override fun visitTypeReference(typeReference: KSTypeReference, data: ClassDescription) {
        super.visitTypeReference(typeReference, data)
        typeReference.resolve().declaration.accept(SerializableClassVisitor, data)
    }
}