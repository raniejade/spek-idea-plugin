package org.jetbrains.spek.studio

import com.intellij.compiler.options.CompileStepBeforeRun
import com.intellij.execution.BeforeRunTask
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.JavaRunConfigurationModule
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key


class SpekAndroidConfigurationFactory(type: ConfigurationType): ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        SpekAndroidRunConfiguration(JavaRunConfigurationModule(project, true), this, "Un-named")

    override fun configureBeforeRunTaskDefaults(providerID: Key<out BeforeRunTask<BeforeRunTask<*>>>,
                                                task: BeforeRunTask<out BeforeRunTask<*>>) {
        if (providerID == CompileStepBeforeRun.ID) {
            task.isEnabled = false
        }
    }
}
