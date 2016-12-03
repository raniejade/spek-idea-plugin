package org.jetbrains.spek.tooling

import com.fasterxml.jackson.annotation.JsonProperty

enum class PathType {
    @JsonProperty("spec")
    SPEC,

    @JsonProperty("group")
    GROUP,

    @JsonProperty("test")
    TEST
}
