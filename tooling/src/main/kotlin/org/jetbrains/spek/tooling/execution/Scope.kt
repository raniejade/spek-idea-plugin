package org.jetbrains.spek.tooling.execution

import java.util.*

/**
 * @author Ranie Jade Ramiso
 */
sealed class Scope(val description: String) {
    class Group(val parent: Group?, description: String): Scope(description) {

        private val _children = LinkedList<Scope>()
        val children: List<Scope> = _children

        init {
            if (parent != null) {
                parent._children.add(this)
            }
        }
    }

    class Test(val parent: Group, description: String): Scope(description)

    fun serializedForm() = Companion.serializedForm(this)

    override fun toString() = description

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Scope

        if (serializedForm() != other.serializedForm()) return false

        return true
    }

    override fun hashCode(): Int{
        return serializedForm().hashCode()
    }


    companion object {
        private fun format(scope: Scope): String {
            if (scope is Scope.Group) {
                val type = if (scope.parent == null) {
                    "spec"
                } else {
                    "group"
                }
                return "[$type:${scope.toString()}]"
            }
            return "[test:${scope.toString()}]"
        }

        private fun serializedForm(scope: Scope): String {
            if (scope is Scope.Group) {
                if (scope.parent != null) {
                    return "${serializedForm(scope.parent)}/${format(scope)}"
                }
                return format(scope)
            }
            return "${serializedForm((scope as Scope.Test).parent)}/${format(scope)}"
        }

        fun parse(scope: String): Scope {
            val split = scope.split("/")

            var parentScope: Scope.Group? = null
            return split.map {
                val typeAndDesc = it.removeSurrounding("[", "]").split(":")
                val type = typeAndDesc[0]
                val desc = typeAndDesc[1]

                if (type == "test") {
                    Scope.Test(parentScope!!, desc)
                } else {
                    val group = Scope.Group(parentScope, desc)
                    parentScope = group
                    group
                }
            }.last()
        }
    }
}
