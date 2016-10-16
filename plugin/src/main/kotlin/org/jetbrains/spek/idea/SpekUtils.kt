package org.jetbrains.spek.idea

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.lexer.KtToken
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtStubbedPsiUtil
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.spek.tooling.Scope

/**
 * @author Ranie Jade Ramiso
 */
object SpekUtils {
    private val GROUP_FN = arrayOf(
        "describe",
        "context",
        "given",
        "on"
    )

    private val TEST_FN = arrayOf(
        "it"
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

    fun isJUnit4(cls: KtClass): Boolean {
        val annotation = cls.annotationEntries.find {
            val typeReference = it.typeReference?.typeElement
            if (typeReference != null) {
                "org.junit.runner.RunWith" == (typeReference as KtUserType).referenceExpression!!
                    .mainReference.resolve()!!.getKotlinFqName()!!.asString()
            } else {
                false
            }

        }
        return annotation != null
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

    fun extractScope(callExpression: KtCallExpression): Scope {
        val lambda = getLambaExpression(callExpression)
        val parent = lambda.parent
        val calleeExpression = callExpression.calleeExpression as KtNameReferenceExpression
        val parameters = callExpression.valueArguments
        val function = calleeExpression.mainReference.resolve() as KtNamedFunction
        val stringExpression = parameters.first().children.firstOrNull() as KtStringTemplateExpression
        val description = stringExpression.text.removeSurrounding("\"")
        val parentScope = if (parent is KtLambdaArgument) {
            val parentCallExpression = parent.parent as KtCallExpression
            extractScope(parentCallExpression) as Scope.Group
        } else {
            val container = getContainingSpecClass(callExpression)
            Scope.Group(null, container!!.qualifiedName!!)
        }

        val fullDesc = "${function.name!!} $description"

        if (isTest(function)) {
            return Scope.Test(parentScope, fullDesc)
        }

        return Scope.Group(parentScope, fullDesc)

    }

    /**
     * Retrieve the lambda expression containing the call expression
     */
    fun getLambaExpression(callExpression: KtCallExpression): KtLambdaExpression {
        // CallExpression -> Block -> FunctionLiteral -> LambdaExpression
        return callExpression.parent.parent.parent as KtLambdaExpression
    }

    fun isDslExtension(function: KtNamedFunction): Boolean {
        val receiverTypeReference = function.receiverTypeReference
        if (receiverTypeReference != null) {
            return (receiverTypeReference.typeElement as KtUserType).referencedName == "Dsl"
        }
        return false
    }

    fun isIdentifier(element: PsiElement): Boolean {
        val node = element.node
        if (node != null) {
            val elementType = node.elementType
            if (elementType is KtToken) {
                return elementType.toString() == "IDENTIFIER"
            }
        }
        return false
    }
}
