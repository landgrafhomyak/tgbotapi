package com.github.landgrafhomyak.telegrambotapi.serialization

import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.symbol.FunctionKind
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

object SerializationConstructorVisitor : KSVisitor<Unit, Unit> {
    override fun visitAnnotated(annotated: KSAnnotated, data: Unit) {
        throw GenerationError("Expected constructor")
    }

    override fun visitAnnotation(annotation: KSAnnotation, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitCallableReference(reference: KSCallableReference, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitClassifierReference(reference: KSClassifierReference, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitDeclaration(declaration: KSDeclaration, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitDeclarationContainer(declarationContainer: KSDeclarationContainer, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitDynamicReference(reference: KSDynamicReference, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitFile(file: KSFile, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        if (!function.isConstructor()) {
            throw GenerationError("Expected constructor")
        }
    }

    override fun visitModifierListOwner(modifierListOwner: KSModifierListOwner, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitNode(node: KSNode, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitParenthesizedReference(reference: KSParenthesizedReference, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitPropertyAccessor(accessor: KSPropertyAccessor, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitPropertyGetter(getter: KSPropertyGetter, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitPropertySetter(setter: KSPropertySetter, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitReferenceElement(element: KSReferenceElement, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitTypeArgument(typeArgument: KSTypeArgument, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitTypeParameter(typeParameter: KSTypeParameter, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitTypeReference(typeReference: KSTypeReference, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitValueArgument(valueArgument: KSValueArgument, data: Unit) {
        TODO("Not yet implemented")
    }

    override fun visitValueParameter(valueParameter: KSValueParameter, data: Unit) {
        TODO("Not yet implemented")
    }

}