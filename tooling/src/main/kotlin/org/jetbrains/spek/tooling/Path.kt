package org.jetbrains.spek.tooling

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/**
 * @author Ranie Jade Ramiso
 */
class Path(@JsonProperty("type") val type: PathType,
           @JsonProperty("description") val description: String,
           @JsonProperty("next") val next: Path?) {

    companion object {
        private val mapper = ObjectMapper()
            .registerKotlinModule()

        fun serialize(path: Path) = mapper.writeValueAsString(path)

        fun deserialize(path: String): Path = mapper.readValue(path)
    }
}
