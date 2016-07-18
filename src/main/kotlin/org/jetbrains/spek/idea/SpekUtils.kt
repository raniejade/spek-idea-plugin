package org.jetbrains.spek.idea

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.asJava.KtLightClass
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

/**
 * @author Ranie Jade Ramiso
 */
object SpekUtils {
    fun isSpec(element: PsiElement): KtLightClass? {
        val ktClass = element.getParentOfType<KtClass>(true)
        if (ktClass != null) {
            val cls = ktClass.toLightClass()
            if (cls != null && isSpec(cls)) {
                return cls
            }
        } else if (element is KtLightClass && isSpec(element)) {
            return element
        }
        return null
    }

    private fun isSpec(cls: KtLightClass): Boolean {
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
}
