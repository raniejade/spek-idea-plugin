package org.jetbrains.spek.idea.androidstudio

import org.jetbrains.spek.idea.SpekConfigurationFactory
import org.jetbrains.spek.idea.SpekConfigurationType

class SpekAndroidConfigurationType: SpekConfigurationType(
    "SpecsRunConfiguration", // hardcoded :(
    "Spek"
) {
    override fun createConfigurationFactory(type: SpekConfigurationType): SpekConfigurationFactory {
        return SpekAndroidConfigurationFactory(type)
    }
}
