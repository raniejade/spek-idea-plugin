package org.jetbrains.spek.idea

import com.intellij.execution.Location
import com.intellij.execution.testframework.sm.runner.SMTestLocator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope

/**
 * @author Ranie Jade Ramiso
 */
class SpekTestLocator: SMTestLocator {
    override fun getLocation(protocol: String, path: String,
                             project: Project, scope: GlobalSearchScope): MutableList<Location<PsiElement>> {
        return mutableListOf()
    }
}
