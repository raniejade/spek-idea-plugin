package org.jetbrains.spek.tooling.runner

/**
 * @author Ranie Jade Ramiso
 */
data class TestExecutionResult(val status: Status, val duration: Long, val throwable: Throwable? = null) {
    enum class Status {
        Success,
        Failure,
        Aborted
    }
}
