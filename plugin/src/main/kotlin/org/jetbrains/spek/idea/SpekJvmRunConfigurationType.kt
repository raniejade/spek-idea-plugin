package org.jetbrains.spek.idea

class SpekJvmRunConfigurationType: SpekConfigurationType(
    "org.spekframework.spek-jvm",
    "Spek"
) {
    override fun createConfigurationFactory(type: SpekConfigurationType): SpekConfigurationFactory {
        return SpekJvmConfigurationFactory(type)
    }
}
