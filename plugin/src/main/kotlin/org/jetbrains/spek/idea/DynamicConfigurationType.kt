package org.jetbrains.spek.idea

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import javax.swing.Icon

class DynamicConfigurationType: ConfigurationType {
    val manager by lazy { SpekPluginManager() }
    val extensionFactory by lazy { manager.extensionFactory }
    val type by lazy { extensionFactory.createConfigurationType() }

    override fun getIcon(): Icon {
        return type.icon
    }

    override fun getConfigurationTypeDescription(): String {
        return type.configurationTypeDescription
    }

    override fun getId(): String {
        return type.id
    }

    override fun getDisplayName(): String {
        return type.displayName
    }

    override fun getConfigurationFactories(): Array<ConfigurationFactory> {
        return type.configurationFactories
    }
}
