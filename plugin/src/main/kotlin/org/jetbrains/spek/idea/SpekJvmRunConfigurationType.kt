package org.jetbrains.spek.idea

class SpekJvmRunConfigurationType: SpekRunConfigurationType(
    "org.spekframework.spek-jvm",
    "Spek"
) {
    override fun createConfigurationFactory(type: SpekRunConfigurationType): SpekConfigurationFactory {
        return SpekJvmConfigurationFactory(type)
    }
}
