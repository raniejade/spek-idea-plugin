package org.jetbrains.spek.idea

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.junit.JavaRunConfigurationProducerBase
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.spek.tooling.execution.Scope

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
            if (SpekUtils.isIdentifier(element)) {
                val parent = element.parent
                if (parent != null) {
                    when (parent) {
                        is KtClass -> {
                            val cls = parent.toLightClass()
                            if (cls != null && SpekUtils.isSpec(cls)) {
                                configuration.spec = cls.qualifiedName!!
                                configuration.setModule(context.module)
                                configuration.setGeneratedName()
                                return true
                            }
                        }
                        is KtNameReferenceExpression -> {
                            val callExpression = parent.parent
                            if (callExpression != null &&
                                callExpression is KtCallExpression &&
                                SpekUtils.isContainedInSpec(callExpression) &&
                                SpekUtils.isSpecBlock(callExpression)

                            ) {
                                val spec = SpekUtils.getContainingSpecClass(callExpression)
                                if (spec != null) {
                                    val scope = SpekUtils.extractScope(callExpression)
                                    configuration.spec = spec.qualifiedName!!
                                    configuration.scope = scope
                                    configuration.setModule(context.module)
                                    configuration.setGeneratedName()
                                    return true
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    override fun isConfigurationFromContext(configuration: SpekRunConfiguration,
                                            context: ConfigurationContext): Boolean {
        val element = context.psiLocation

        if (element != null) {
            if (SpekUtils.isIdentifier(element)) {
                val parent = element.parent
                if (parent != null) {
                    var spec: String? = null
                    var scope: Scope? = null
                    when (parent) {
                        is KtClass -> {
                            val cls = parent.toLightClass()
                            if (cls != null && SpekUtils.isSpec(cls)) {
                                spec = cls.qualifiedName!!
                            }
                        }
                        is KtNameReferenceExpression -> {
                            val callExpression = parent.parent
                            if (callExpression != null &&
                                callExpression is KtCallExpression &&
                                SpekUtils.isContainedInSpec(callExpression) &&
                                SpekUtils.isSpecBlock(callExpression)

                            ) {
                                val cls = SpekUtils.getContainingSpecClass(callExpression)
                                if (cls != null) {
                                    spec = cls.qualifiedName!!
                                    scope = SpekUtils.extractScope(callExpression)
                                }
                            }
                        }
                    }

                    return configuration.spec == spec &&
                        configuration.scope == scope &&
                        configuration.configurationModule.module == context.module
                }
            }
        }
        return false
    }
}
