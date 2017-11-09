package org.jetbrains.spek.studio

import org.jetbrains.spek.idea.SpekConfigurationType

class SpekAndroidConfigurationType: SpekConfigurationType(
    "SpecsRunConfiguration", // hardcoded :( see MakeBeforeRunTaskProvider and AndroidCommonUtils
    "Spek - Android"
) {
    init {
        addFactory(SpekAndroidConfigurationFactory(this))
    }
}
