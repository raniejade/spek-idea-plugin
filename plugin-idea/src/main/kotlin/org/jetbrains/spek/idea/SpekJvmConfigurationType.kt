package org.jetbrains.spek.idea

class SpekJvmRunConfigurationType: SpekConfigurationType(
    "org.spekframework.spek-jvm",
    "Spek - JVM"
) {
    init {
        addFactory(SpekJvmConfigurationFactory(this))
    }
}

