package org.jetbrains.spek.idea

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.asJava.KtLightClass
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.lexer.KtToken
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

/**
 * @author Ranie Jade Ramiso
 */
object SpekUtils {
    private val GROUP_FN = arrayOf(
        "describe",
        "context",
        "given"
    )

    private val TEST_FN = arrayOf(
        "it",
        "on"
    )

    fun isSpec(cls: KtLightClass): Boolean {
        val superClass = cls.superClass
        if (superClass != null) {
            val fqName = superClass.qualifiedName
            if (fqName == "org.jetbrains.spek.api.Spek"
                || fqName == "org.jetbrains.spek.api.SubjectSpek") {
                return true
            }
        }
        return false
    }

    fun isSpecBlock(callExpression: KtCallExpression): Boolean {
        val parameters = callExpression.valueArguments
        val lambda = callExpression.lambdaArguments.firstOrNull()
        val calleeExpression = callExpression.calleeExpression!! as KtNameReferenceExpression
        val resolved = calleeExpression.mainReference.resolve()

        if (resolved != null && resolved is KtNamedFunction) {
            if (lambda != null && parameters.size == 2 && isDslExtension(resolved)) {
                val desc = parameters.first().children.firstOrNull()
                if (desc != null && desc is KtStringTemplateExpression) {
                    return isTest(resolved) || isGroup(resolved)
                }
            }
        }
        return false
    }

    fun isSpec(cls: KtClass): Boolean {
        val lcls = cls.toLightClass()
        if (lcls != null) {
            return isSpec(lcls)
        }
        return false
    }

    fun isGroup(function: KtNamedFunction): Boolean {
        return GROUP_FN.contains(function.name)
    }

    fun isTest(function: KtNamedFunction): Boolean {
        return TEST_FN.contains(function.name)
    }

    fun isContainedInSpec(callExpression: KtCallExpression): Boolean {
        val container = KtStubbedPsiUtil.getContainingDeclaration(callExpression, KtClass::class.java)
        if (container != null) {
            return isSpec(container)
        }
        return false
    }

    fun getContainingSpecClass(callExpression: KtCallExpression): KtLightClass? {
        val container = KtStubbedPsiUtil.getContainingDeclaration(callExpression, KtClass::class.java)
        if (container != null && isSpec(container)) {
            return container.toLightClass()
        }
        return null
    }

    fun isDslExtension(function: KtNamedFunction): Boolean {
        val receiverTypeReference = function.receiverTypeReference
        if (receiverTypeReference != null) {
            return (receiverTypeReference.typeElement as KtUserType).referencedName == "Dsl"
        }
        return false
    }

    fun isIdentifier(element: PsiElement): Boolean {
        val elementType = element.node.elementType
        if (elementType is KtToken) {
            return  elementType.toString() == "IDENTIFIER"
        }
        return false
    }
}
