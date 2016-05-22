package io.polymorphicpanda.kspec.idea

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.icons.AllIcons

/**
 * @author Ranie Jade Ramiso
 */
class KSpecRunConfigurationType: ConfigurationTypeBase(
    "kspec-run-configuration",
    "KSpec",
    "Run KSpec specs",
    AllIcons.RunConfigurations.Junit
) {
    init {
        addFactory(KSpecConfigurationFactory(this))
    }
}
