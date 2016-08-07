package org.jetbrains.spek.idea

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.psi.KtClass

/**
 * @author Ranie Jade Ramiso
 */
class SpekLineMarkerProvider: RunLineMarkerContributor() {
    private val TOOLTIP_PROVIDER = Function<PsiElement, String> { "Run spec" }

    override fun getInfo(element: PsiElement): Info? {
        if (SpekUtils.isIdentifier(element)) {
            val parent = element.parent
            if (parent != null && parent is KtClass) {
                val lightClass = parent.toLightClass()
                if (lightClass != null && SpekUtils.isSpec(lightClass)) {
                    return Info(
                        AllIcons.RunConfigurations.TestState.Run, TOOLTIP_PROVIDER, *ExecutorAction.getActions(0)
                    )
                }
            }
        }
        return null
    }
}
