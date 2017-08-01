package org.jetbrains.spek.idea

import com.intellij.openapi.extensions.ExtensionPointName

interface PluginOverride {
    fun createExtensionFactory(): ExtensionFactory

    companion object {
        val PLUGIN_OVERRIDE_EP = ExtensionPointName.create<PluginOverride>("org.jetbrains.spek.spek-idea-plugin.pluginOverride")
    }
}
