package org.jetbrains.spek.idea

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.icons.AllIcons

/**
 * @author Ranie Jade Ramiso
 */
abstract class SpekRunConfigurationType(id: String, displayName: String): ConfigurationTypeBase(
    id,
    displayName,
    "Run Spek tests",
    AllIcons.RunConfigurations.Junit
) {
    protected abstract fun createConfigurationFactory(type: SpekRunConfigurationType): SpekConfigurationFactory

    init {
        addFactory(createConfigurationFactory(this))
    }

}
