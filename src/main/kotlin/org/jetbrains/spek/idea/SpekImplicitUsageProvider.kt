package org.jetbrains.spek.idea

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.psi.PsiElement

/**
 * @author Ranie Jade Ramiso
 */
class SpekImplicitUsageProvider: ImplicitUsageProvider {
    override fun isImplicitWrite(element: PsiElement) = false

    override fun isImplicitRead(element: PsiElement) = false

    override fun isImplicitUsage(element: PsiElement): Boolean {
        return SpekUtils.getSpec(element) != null
    }
}
