package org.jetbrains.spek.idea

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.junit.JavaRunConfigurationProducerBase
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.idea.core.getPackage
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.spek.tooling.Target

/**
 * @author Ranie Jade Ramiso
 */
class SpekJvmRunConfigurationProducer: JavaRunConfigurationProducerBase<SpekJvmRunConfiguration>(
    ConfigurationTypeUtil.findConfigurationType(SpekConfigurationType::class.java)
) {
    override fun setupConfigurationFromContext(configuration: SpekJvmRunConfiguration, context: ConfigurationContext,
                                               sourceElement: Ref<PsiElement>): Boolean {
        var configurationSet = false
        if (!sourceElement.isNull) {
            val element = sourceElement.get()!!
            if (element is KtClassOrObject) {
                // when clicking on the class file in the project view
                val cls = element.toLightClass()
                if (cls != null && SpekJvmUtils.isSpec(cls)) {
                    if (cls.qualifiedName != null) {
                        configuration.target = Target.Spec(cls.qualifiedName!!)
                        configurationSet = true
                    }
                }
            } else if (element is PsiDirectory) {
                if (context.module != null) {
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
                }

            } else if (SpekJvmUtils.isIdentifier(element)) {
                // when clicking on the source editor
                val parent = element.parent
                if (parent != null) {
                    when (parent) {
                        is KtClassOrObject -> {
                            val cls = parent.toLightClass()
                            if (cls != null && SpekJvmUtils.isSpec(cls)) {
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
                                SpekJvmUtils.isContainedInSpec(callExpression) &&
                                SpekJvmUtils.isSpecBlock(callExpression)

                            ) {
                                val spec = SpekJvmUtils.getContainingSpecClass(callExpression)
                                if (spec != null) {
                                    configuration.target = Target.Spec(
                                        spec.qualifiedName!!,
                                        SpekJvmUtils.extractPath(callExpression)
                                    )
                                    configurationSet = true
                                }
                            }
                        }
                    }
                }
            }

            if (!configurationSet && SpekJvmUtils.isInKotlinFile(element)) {
                val target = targetForNearestSurroundingSpekBlock(element)
                if (target != null) {
                    configuration.target = target
                    configurationSet = true
                }
            }
        }

        if (configurationSet) {
            configuration.setModule(context.module)
            configuration.setGeneratedName()
        }

        return configurationSet
    }

    override fun isConfigurationFromContext(configuration: SpekJvmRunConfiguration,
                                            context: ConfigurationContext): Boolean {
        val element = context.psiLocation
        var target: Target? = null

        if (element != null) {
            if (element is KtClassOrObject) {
                val cls = element.toLightClass()
                if (cls != null && SpekJvmUtils.isSpec(cls)) {
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

            }  else if (SpekJvmUtils.isIdentifier(element)) {
                val parent = element.parent
                if (parent != null) {
                    when (parent) {
                        is KtClassOrObject -> {
                            val cls = parent.toLightClass()
                            if (cls != null && SpekJvmUtils.isSpec(cls)) {
                                target = Target.Spec(cls.qualifiedName!!)
                            }
                        }
                        is KtNameReferenceExpression -> {
                            val callExpression = parent.parent
                            if (callExpression != null &&
                                callExpression is KtCallExpression &&
                                SpekJvmUtils.isContainedInSpec(callExpression) &&
                                SpekJvmUtils.isSpecBlock(callExpression)

                            ) {
                                val cls = SpekJvmUtils.getContainingSpecClass(callExpression)
                                if (cls != null) {
                                    target = Target.Spec(
                                        cls.qualifiedName!!,
                                        SpekJvmUtils.extractPath(callExpression)
                                    )
                                }
                            }
                        }
                    }


                }
            }

            if (target == null && SpekJvmUtils.isInKotlinFile(element)) {
                target = targetForNearestSurroundingSpekBlock(element)
            }
        }

        return if (target == null) {
            false
        } else {
            configuration.target == target
                && configuration.configurationModule.module == context.module
        }
    }

    private fun targetForNearestSurroundingSpekBlock(element: PsiElement): Target?
    {
        var nearestCallExpression = PsiTreeUtil.getParentOfType(element, KtCallExpression::class.java)

        while (nearestCallExpression != null) {
            val calleeExpression = nearestCallExpression.calleeExpression!! as KtNameReferenceExpression
            val resolved = calleeExpression.mainReference.resolve()

            if (resolved is KtNamedFunction && SpekJvmUtils.isGroupOrTest(resolved)) {
                val spec = SpekJvmUtils.getContainingSpecClass(nearestCallExpression)
                if (spec != null) {
                    return Target.Spec(
                            spec.qualifiedName!!,
                        SpekJvmUtils.extractPath(nearestCallExpression)
                    )
                }
            }
            nearestCallExpression = PsiTreeUtil.getParentOfType(nearestCallExpression, KtCallExpression::class.java)
        }

        return null
    }
}
