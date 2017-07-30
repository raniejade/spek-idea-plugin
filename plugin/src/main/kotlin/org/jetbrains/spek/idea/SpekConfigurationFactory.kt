package org.jetbrains.spek.idea

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.JavaRunConfigurationModule
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

/**
 * @author Ranie Jade Ramiso
 */
abstract class SpekConfigurationFactory(type: ConfigurationType): ConfigurationFactory(type) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return createSpekRunConfiguration(JavaRunConfigurationModule(project, true), this, "Unnamed")
    }

    protected abstract fun createSpekRunConfiguration(configurationModule: JavaRunConfigurationModule,
                                                      factory: SpekConfigurationFactory,
                                                      name: String): SpekRunConfiguration
}
