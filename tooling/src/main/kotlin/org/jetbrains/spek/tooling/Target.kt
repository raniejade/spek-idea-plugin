package org.jetbrains.spek.tooling

/**
 * @author Ranie Jade Ramiso
 */
sealed class Target {
    class Spec(val spec: String, val scope: Scope? = null): Target()
    class Package(val `package`: String): Target()
}
