package org.jetbrains.spek.idea

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.junit.JavaRunConfigurationProducerBase
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.psi.KtClass

/**
 * @author Ranie Jade Ramiso
 */
class SpekRunConfigurationProducer: JavaRunConfigurationProducerBase<SpekRunConfiguration>(
    ConfigurationTypeUtil.findConfigurationType(SpekRunConfigurationType::class.java)
) {
    override fun setupConfigurationFromContext(configuration: SpekRunConfiguration, context: ConfigurationContext,
                                               sourceElement: Ref<PsiElement>): Boolean {
        if (!sourceElement.isNull) {
            val element = sourceElement.get()!!
            val parent = element.parent
            if (parent != null && parent is KtClass) {
                val cls = parent.toLightClass()
                if (cls != null && SpekUtils.isSpec(cls)) {
                    configuration.spec = cls.qualifiedName!!
                    configuration.setModule(context.module)
                    configuration.setGeneratedName()
                    return true
                }
            }
        }
        return false
    }

    override fun isConfigurationFromContext(configuration: SpekRunConfiguration,
                                            context: ConfigurationContext): Boolean {
        return false
    }
}
