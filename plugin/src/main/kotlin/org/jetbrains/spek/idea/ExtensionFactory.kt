package org.jetbrains.spek.idea

import com.intellij.execution.configurations.ConfigurationType

interface ExtensionFactory {
    fun createConfigurationType(): ConfigurationType
}
