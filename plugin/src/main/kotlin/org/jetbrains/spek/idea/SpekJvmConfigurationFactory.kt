package org.jetbrains.spek.idea

import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.JavaRunConfigurationModule

/**
 * @author Ranie Jade Ramiso
 */
class SpekJvmConfigurationFactory(type: ConfigurationType): SpekConfigurationFactory(type) {
    override fun createSpekRunConfiguration(configurationModule: JavaRunConfigurationModule,
                                            factory: SpekConfigurationFactory,
                                            name: String): SpekRunConfiguration {
        return SpekRunConfiguration(configurationModule, factory, name)
    }
}
