package com.github.landgrafhomyak.telegrambotapi.serialization

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSCallableReference
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSClassifierReference
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSDeclarationContainer
import com.google.devtools.ksp.symbol.KSDynamicReference
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSModifierListOwner
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSParenthesizedReference
import com.google.devtools.ksp.symbol.KSPropertyAccessor
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSPropertyGetter
import com.google.devtools.ksp.symbol.KSPropertySetter
import com.google.devtools.ksp.symbol.KSReferenceElement
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.symbol.Modifier
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

object SealedClassVisitor : KSVisitor<MutableSet<KSClassDeclaration>, Unit> {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: MutableSet<KSClassDeclaration>) {
        if (classDeclaration.classKind != ClassKind.CLASS || Modifier.SEALED !in classDeclaration.modifiers) {
            throw GenerationError("Class `${classDeclaration.qualifiedName!!.asString()}` marked @${GenerateSerializerByDiscriminator::class.simpleName} must be sealed")
        }

        var annotationSerializable: KSAnnotation? = null
        var annotationPolymorphic: KSAnnotation? = null
        var annotationGenerateSerializerByDiscriminator: KSAnnotation? = null
        for (annRef in classDeclaration.annotations) {
            when (annRef.annotationType.resolve().declaration.qualifiedName!!.asString()) {
                Serializable::class.qualifiedName                      -> annotationSerializable = annRef
                Polymorphic::class.qualifiedName                       -> annotationPolymorphic = annRef
                GenerateSerializerByDiscriminator::class.qualifiedName -> annotationGenerateSerializerByDiscriminator = annRef
            }
        }

        if (annotationSerializable == null) {
            throw GenerationError("Missed @${Serializable::class.qualifiedName} for class `${classDeclaration.qualifiedName!!.asString()}`")
        }
        /*
        if (annotationSerializable.arguments[0].value == null) {
            throw GenerationError("Missed custom serializer in (default is incorrect) @${Serializable::class.qualifiedName} for class `${classDeclaration.qualifiedName!!.asString()}`")
        }
        */
        if (annotationPolymorphic != null) {
            throw GenerationError("Useless (custom will be generated) @${Polymorphic::class.qualifiedName} for class `${classDeclaration.qualifiedName!!.asString()}`")
        }

        for (sub in classDeclaration.getSealedSubclasses()) {
            data.add(sub)
            var annotationSerializable: KSAnnotation? = null
            var annotationSerialName: KSAnnotation? = null
            var annotationGenerateSerializer: KSAnnotation? = null
            for (annRef in sub.annotations) {
                when (annRef.annotationType.resolve().declaration.qualifiedName!!.asString()) {
                    Serializable::class.qualifiedName       -> annotationSerializable = annRef
                    SerialName::class.qualifiedName         -> annotationSerialName = annRef
                    GenerateSerializer::class.qualifiedName -> annotationGenerateSerializer = annRef
                }
            }

            if (annotationSerializable == null) {
                throw GenerationError("Missed @${Serializable::class.qualifiedName} for class `${sub.qualifiedName!!.asString()}` which is sealed subclass of ${classDeclaration.qualifiedName!!.asString()}")
            }
            /*
            if (annotationSerializable.arguments[0].value == null) {
                throw GenerationError("Missed custom serializer in (default is incorrect) @${Serializable::class.qualifiedName} for class `${sub.qualifiedName!!.asString()}` which is sealed subclass of ${classDeclaration.qualifiedName!!.asString()}")
            }
            */
            if (annotationSerialName == null) {
                throw GenerationError("Missed @${SerialName::class.qualifiedName} for class `${sub.qualifiedName!!.asString()}` which is sealed subclass of ${classDeclaration.qualifiedName!!.asString()}")
            }
            if (annotationGenerateSerializer == null) {
                throw GenerationError("Missed @${GenerateSerializer::class.qualifiedName} for class `${sub.qualifiedName!!.asString()}` which is sealed subclass of ${classDeclaration.qualifiedName!!.asString()}")
            }
        }
    }

    override fun visitAnnotated(annotated: KSAnnotated, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")


    override fun visitAnnotation(annotation: KSAnnotation, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitCallableReference(reference: KSCallableReference, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitClassifierReference(reference: KSClassifierReference, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitDeclaration(declaration: KSDeclaration, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitDeclarationContainer(declarationContainer: KSDeclarationContainer, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitDynamicReference(reference: KSDynamicReference, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitFile(file: KSFile, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitModifierListOwner(modifierListOwner: KSModifierListOwner, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitNode(node: KSNode, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitParenthesizedReference(reference: KSParenthesizedReference, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitPropertyAccessor(accessor: KSPropertyAccessor, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitPropertyGetter(getter: KSPropertyGetter, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitPropertySetter(setter: KSPropertySetter, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitReferenceElement(element: KSReferenceElement, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitTypeAlias(typeAlias: KSTypeAlias, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitTypeArgument(typeArgument: KSTypeArgument, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitTypeParameter(typeParameter: KSTypeParameter, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitTypeReference(typeReference: KSTypeReference, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitValueArgument(valueArgument: KSValueArgument, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

    override fun visitValueParameter(valueParameter: KSValueParameter, data: MutableSet<KSClassDeclaration>) =
        throw GenerationError("This visitor is only for sealed subclasses")

}