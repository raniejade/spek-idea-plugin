package org.jetbrains.spek.studio

import com.android.tools.idea.run.PreferGradleMake
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.JavaRunConfigurationModule
import org.jetbrains.spek.idea.SpekJvmRunConfiguration

class SpekAndroidRunConfiguration(javaRunConfigurationModule: JavaRunConfigurationModule,
                                  factory: ConfigurationFactory,
                                  name: String)
    : SpekJvmRunConfiguration(javaRunConfigurationModule, factory, name), PreferGradleMake
