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

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other?.javaClass != javaClass) {
            return false
        }

        other as Path

        if (this.type != other.type) {
            return false
        }

        if (this.description != other.description) {
            return false
        }

        if (this.next != other.next) {
            return false
        }

        return true
    }
}
