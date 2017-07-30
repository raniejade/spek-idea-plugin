package org.jetbrains.spek.idea.androidstudio

import com.android.tools.idea.run.PreferGradleMake
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.JavaRunConfigurationModule
import org.jetbrains.spek.idea.SpekRunConfiguration

class SpekAndroidRunConfiguration(javaRunConfigurationModule: JavaRunConfigurationModule,
                                  factory: ConfigurationFactory,
                                  name: String)
    : SpekRunConfiguration(javaRunConfigurationModule, factory, name), PreferGradleMake {
}
