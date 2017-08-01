package org.jetbrains.spek.idea

import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.extensions.Extensions

class SpekPluginManager {
    val extensionFactory by lazy {
        val override = Extensions.getExtensions(PluginOverride.PLUGIN_OVERRIDE_EP)
            .firstOrNull()

        override?.createExtensionFactory() ?: createDefaultExtensionFactory()
    }

    private fun createDefaultExtensionFactory(): ExtensionFactory {
        return object: ExtensionFactory {
            override fun createConfigurationType(): ConfigurationType {
                return SpekJvmRunConfigurationType()
            }
        }
    }
}
