package org.jetbrains.spek.idea

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.junit.JavaRunConfigurationProducerBase
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.idea.core.getPackage
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.spek.tooling.Target

/**
 * @author Ranie Jade Ramiso
 */
class SpekRunConfigurationProducer: JavaRunConfigurationProducerBase<SpekRunConfiguration>(
    ConfigurationTypeUtil.findConfigurationType(SpekRunConfigurationType::class.java)
) {
    override fun setupConfigurationFromContext(configuration: SpekRunConfiguration, context: ConfigurationContext,
                                               sourceElement: Ref<PsiElement>): Boolean {
        var configurationSet = false
        if (!sourceElement.isNull) {
            val element = sourceElement.get()!!
            if (element is KtClass) {
                // when clicking on the class file in the project view
                val cls = element.toLightClass()
                if (cls != null && SpekUtils.isSpec(cls)) {
                    if (cls.qualifiedName != null) {
                        configuration.target = Target.Spec(cls.qualifiedName!!)
                        configurationSet = true
                    }
                }
            } else if (element is PsiDirectory) {
                val moduleRootManager = ModuleRootManager.getInstance(context.module)
                val roots = moduleRootManager.getSourceRoots(JavaSourceRootType.TEST_SOURCE)

                if (VfsUtil.isUnder(element.virtualFile, roots.toSet())) {
                    val psiPackage = element.getPackage()

                    if (psiPackage != null) {
                        configuration.target = Target.Package(
                            psiPackage.qualifiedName
                        )
                        configurationSet = true
                    }
                }

            } else if (SpekUtils.isIdentifier(element)) {
                // when clicking on the source editor
                val parent = element.parent
                if (parent != null) {
                    when (parent) {
                        is KtClass -> {
                            val cls = parent.toLightClass()
                            if (cls != null && SpekUtils.isSpec(cls)) {
                                if (cls.qualifiedName != null) {
                                    configuration.target = Target.Spec(cls.qualifiedName!!)
                                    configurationSet = true
                                }
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
                                    configuration.target = Target.Spec(
                                        spec.qualifiedName!!,
                                        SpekUtils.extractPath(callExpression)
                                    )
                                    configurationSet = true
                                }
                            }
                        }
                    }
                }
            }
        }

        if (configurationSet) {
            configuration.setModule(context.module)
            configuration.setGeneratedName()
        }

        return configurationSet
    }

    override fun isConfigurationFromContext(configuration: SpekRunConfiguration,
                                            context: ConfigurationContext): Boolean {
        val element = context.psiLocation
        var target: Target? = null

        if (element != null) {
            if (element is KtClass) {
                val cls = element.toLightClass()
                if (cls != null && SpekUtils.isSpec(cls)) {
                    target = Target.Spec(cls.qualifiedName!!)
                }
            } else if (element is PsiDirectory) {
                val moduleRootManager = ModuleRootManager.getInstance(context.module)
                val roots = moduleRootManager.getSourceRoots(JavaSourceRootType.TEST_SOURCE)

                if (VfsUtil.isUnder(element.virtualFile, roots.toSet())) {
                    val psiPackage = element.getPackage()

                    if (psiPackage != null) {
                        target = Target.Package(
                            psiPackage.qualifiedName
                        )
                    }
                }

            }  else if (SpekUtils.isIdentifier(element)) {
                val parent = element.parent
                if (parent != null) {
                    when (parent) {
                        is KtClass -> {
                            val cls = parent.toLightClass()
                            if (cls != null && SpekUtils.isSpec(cls)) {
                                target = Target.Spec(cls.qualifiedName!!)
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
                                    target = Target.Spec(
                                        cls.qualifiedName!!,
                                        SpekUtils.extractPath(callExpression)
                                    )
                                }
                            }
                        }
                    }


                }
            }
        }

        return if (target == null) {
            false
        } else {
            configuration.target == target
                && configuration.configurationModule.module == context.module
        }
    }
}
