package org.jetbrains.spek.idea

import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.*
import org.jetbrains.spek.idea.tooling.execution.Scope
import kotlin.properties.Delegates

/**
 * @author Ranie Jade Ramiso
 */
class SpekModel {
    var spec by Delegates.observable<KtClassOrObject?>(null, { prop, old, new ->
        if (new != null && old != new) {
            setupScopes(new)
        }
    })


    private fun setupScopes(spec: KtClassOrObject) {
        val body = SpekUtils.getSpecBody(spec)
        val rootScope = Scope.Group(null, "class", spec.fqName!!.asString())
        KtPsiUtil.visitChildren(body, ScopeTreeBuilder(rootScope), null)
        println(rootScope)
    }


    class ScopeTreeBuilder(val root: Scope.Group): KtVisitorVoid() {
        override fun visitCallExpression(expression: KtCallExpression) {
            super.visitCallExpression(expression)
            val parameters = expression.valueArguments
            val lambda = expression.lambdaArguments.firstOrNull()
            val calleeExpression = expression.calleeExpression!! as KtNameReferenceExpression
            // attempt to resolve
            val resolved = calleeExpression.mainReference.resolve()
            if (resolved != null) {
                // get the declaration of the method
                // traverse body and check for calls to Dsl.group or Dsl.test
                val declaration = resolved as KtNamedFunction
                if (lambda != null && parameters.size == 2) {
                    val desc = parameters.first().children.firstOrNull()
                    if (desc != null && desc is KtStringTemplateExpression) {
                        val body = lambda.getLambdaExpression().bodyExpression!!
                        val scope = Scope.Group(root, calleeExpression.text, desc.text.removeSurrounding("\""))
                        KtPsiUtil.visitChildren(body, ScopeTreeBuilder(scope), null)
                    }
                }
            }
        }
    }
}
