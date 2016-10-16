package org.jetbrains.spek.tooling.runner

/**
 * @author Ranie Jade Ramiso
 */
abstract class TestExecutionListener {
    open fun executionStarted(test: TestIdentifier) { }

    open fun executionFinished(test: TestIdentifier, result: TestExecutionResult) { }

    open fun executionSkipped(test: TestIdentifier, reason: String) { }
}
