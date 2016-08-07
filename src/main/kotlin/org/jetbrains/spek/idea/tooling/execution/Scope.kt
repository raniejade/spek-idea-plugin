package org.jetbrains.spek.idea.tooling.execution

import java.util.*

/**
 * @author Ranie Jade Ramiso
 */
sealed class Scope(val fn: String, val description: String) {
    class Group(val parent: Group?, fn: String, description: String): Scope(fn, description) {

        private val _children = LinkedList<Scope>()
        val children: List<Scope> = _children

        init {
            if (parent != null) {
                parent._children.add(this)
            }
        }
    }
    class Test(val parent: Group, fn: String, description: String): Scope(fn, description)
}
