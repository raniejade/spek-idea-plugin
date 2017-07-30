package org.jetbrains.spek.idea.androidstudio

import org.jetbrains.spek.idea.SpekConfigurationFactory
import org.jetbrains.spek.idea.SpekRunConfigurationType

class SpekAndroidConfigurationType: SpekRunConfigurationType(
    "SpecsRunConfiguration", // hardcoded :(
    "Spek - Android"
) {
    override fun createConfigurationFactory(type: SpekRunConfigurationType): SpekConfigurationFactory {
        return SpekAndroidConfigurationFactory(type)
    }
}
