package io.polymorphicpanda.kspec.idea

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.JavaRunConfigurationModule
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

/**
 * @author Ranie Jade Ramiso
 */
class KSpecConfigurationFactory(type: ConfigurationType): ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return KSpecRunConfiguration(JavaRunConfigurationModule(project, true), this, "Unnamed")
    }
}
