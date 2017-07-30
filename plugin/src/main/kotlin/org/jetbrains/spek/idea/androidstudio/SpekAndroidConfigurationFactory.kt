package org.jetbrains.spek.idea.androidstudio

import com.intellij.compiler.options.CompileStepBeforeRun
import com.intellij.execution.BeforeRunTask
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.JavaRunConfigurationModule
import com.intellij.openapi.util.Key
import org.jetbrains.spek.idea.SpekConfigurationFactory
import org.jetbrains.spek.idea.SpekRunConfiguration

class SpekAndroidConfigurationFactory(type: ConfigurationType): SpekConfigurationFactory(type) {
    override fun createSpekRunConfiguration(configurationModule: JavaRunConfigurationModule,
                                            factory: SpekConfigurationFactory,
                                            name: String): SpekRunConfiguration {
        return SpekAndroidRunConfiguration(configurationModule, factory, name)
    }

    override fun configureBeforeRunTaskDefaults(providerID: Key<out BeforeRunTask<BeforeRunTask<*>>>, task: BeforeRunTask<out BeforeRunTask<*>>) {
        if (providerID == CompileStepBeforeRun.ID) {
            task.isEnabled = false
        }
    }
}
