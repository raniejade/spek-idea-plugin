package org.jetbrains.spek.tooling.runner

import org.jetbrains.spek.tooling.Target
import java.util.LinkedList

/**
 * @author Ranie Jade Ramiso
 */
abstract class SpekRunner(val target: Target) {
    private val listeners = LinkedList<TestExecutionListener>()

    fun addListener(listener: TestExecutionListener) {
        listeners.push(listener)
    }

    fun removeListener(listener: TestExecutionListener) {
        listeners.remove(listener)
    }

    abstract fun run()

    protected fun executionStarted(test: TestIdentifier) {
        listeners.forEach {
            it.executionStarted(test)
        }
    }

    protected fun executionFinished(test: TestIdentifier, result: TestExecutionResult) {
        listeners.forEach {
            it.executionFinished(test, result)
        }
    }

    protected fun executionSkipped(test: TestIdentifier, reason: String) {
        listeners.forEach {
            it.executionSkipped(test, reason)
        }
    }
}
