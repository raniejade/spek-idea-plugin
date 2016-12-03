package org.jetbrains.spek.tooling

/**
 * @author Ranie Jade Ramiso
 */
sealed class Target {
    class Spec(val spec: String, val path: Path? = null): Target() {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other?.javaClass != javaClass) {
                return false
            }

            other as Spec

            if (spec != other.spec) {
                return false
            }
            if (path != other.path) {
                return false
            }

            return true
        }

        override fun hashCode(): Int {
            var result = spec.hashCode()
            result = 31 * result + (path?.hashCode() ?: 0)
            return result
        }
    }

    class Package(val `package`: String): Target() {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other?.javaClass != javaClass) {
                return false
            }

            other as Package

            if (`package` != other.`package`) {
                return false
            }

            return true
        }

        override fun hashCode(): Int {
            return `package`.hashCode()
        }
    }
}
