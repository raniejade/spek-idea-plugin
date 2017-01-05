package org.jetbrains.spek.idea

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtStubbedPsiUtil

/**
 * @author Ranie Jade Ramiso
 */
class SpekLineMarkerProvider: RunLineMarkerContributor() {
    private val TOOLTIP_PROVIDER = Function<PsiElement, String> { "Run spec" }

    override fun getInfo(element: PsiElement): Info? {
        if (SpekUtils.isIdentifier(element)) {
            val parent = element.parent
            if (parent != null) {
                when (parent) {
                    is KtClassOrObject -> {
                        if (SpekUtils.isSpec(parent) && !SpekUtils.isJUnit4(parent)) {
                            return Info(
                                AllIcons.RunConfigurations.TestState.Run,
                                TOOLTIP_PROVIDER,
                                *ExecutorAction.getActions(0)
                            )
                        }
                    }
                    is KtNameReferenceExpression -> {
                        val callExpression = parent.parent
                        val container = KtStubbedPsiUtil.getContainingDeclaration(
                            callExpression, KtClassOrObject::class.java
                        )
                        if (callExpression != null
                            && callExpression is KtCallExpression
                            && container != null
                            && SpekUtils.isSpec(container)
                            && SpekUtils.isSpecBlock(callExpression)
                            && !SpekUtils.isJUnit4(container)

                        ) {
                            return Info(
                                AllIcons.RunConfigurations.TestState.Run,
                                TOOLTIP_PROVIDER,
                                *ExecutorAction.getActions(0)
                            )
                        }
                    }
                }
            }
        }
        return null
    }
}
