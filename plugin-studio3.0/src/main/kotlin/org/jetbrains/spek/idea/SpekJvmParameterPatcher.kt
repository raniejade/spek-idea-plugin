package org.jetbrains.spek.idea

import com.intellij.execution.configurations.JavaParameters
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.module.Module

interface SpekJvmParameterPatcher {
    fun patch(module: Module?, parameters: JavaParameters)

    companion object {
        val PARAMETER_PATCHER_EP = ExtensionPointName.create<SpekJvmParameterPatcher>("org.jetbrains.spek.spek-idea-plugin.parameterPatcher")
    }
}
