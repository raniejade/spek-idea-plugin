package org.jetbrains.spek.idea

import com.intellij.execution.configurations.ConfigurationTypeBase
import org.jetbrains.kotlin.idea.KotlinIcons

abstract class SpekConfigurationType(id: String, displayName: String): ConfigurationTypeBase(
    id,
    displayName,
    "Run specifications",
    KotlinIcons.SMALL_LOGO_13
)
