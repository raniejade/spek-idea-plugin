package org.jetbrains.spek.idea.androidstudio

import com.intellij.execution.configurations.ConfigurationType
import org.jetbrains.spek.idea.ExtensionFactory
import org.jetbrains.spek.idea.PluginOverride

class PluginAndroidOverride: PluginOverride {
    override fun createExtensionFactory(): ExtensionFactory {
        return object: ExtensionFactory {
            override fun createConfigurationType(): ConfigurationType {
                return SpekAndroidConfigurationType()
            }
        }
    }
}
