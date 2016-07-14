package org.jetbrains.spek.idea

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.icons.AllIcons

/**
 * @author Ranie Jade Ramiso
 */
class SpekRunConfigurationType: ConfigurationTypeBase(
    "spek-run-configuration",
    "Spek",
    "Run Spek tests",
    AllIcons.RunConfigurations.Junit
) {
    init {
        addFactory(SpekConfigurationFactory(this))
    }
}
