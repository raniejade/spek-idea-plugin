package org.jetbrains.spek.idea

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.JavaRunConfigurationModule
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

/**
 * @author Ranie Jade Ramiso
 */
class SpekConfigurationFactory(type: ConfigurationType): ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return SpekRunConfiguration(JavaRunConfigurationModule(project, true), this, "Unnamed")
    }
}
