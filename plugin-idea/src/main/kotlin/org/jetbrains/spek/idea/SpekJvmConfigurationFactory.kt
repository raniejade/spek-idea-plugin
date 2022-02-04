package org.jetbrains.spek.idea

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.JavaRunConfigurationModule
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class SpekJvmConfigurationFactory(type: ConfigurationType): ConfigurationFactory(type) {

    override fun getId(): String = type.id

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        SpekJvmRunConfiguration(JavaRunConfigurationModule(project, true), this, "Un-named")
}
