package org.jetbrains.spek.tooling.adapter.sm

import org.jetbrains.spek.tooling.adapter.Adapter
import org.jetbrains.spek.tooling.runner.TestExecutionResult
import org.jetbrains.spek.tooling.runner.TestIdentifier
import java.io.CharArrayWriter
import java.io.PrintWriter

/**
 * @author Ranie Jade Ramiso
 */
class ServiceMessageAdapter: Adapter() {
    override fun executionFinished(test: TestIdentifier, result: TestExecutionResult) {
        val name = test.displayName.toTcSafeString()
        if (test.container) {
            out("testSuiteFinished name='$name'")
        } else {
            val duration = result.duration
            if (result.status != TestExecutionResult.Status.Success) {
                val throwable = result.throwable!!
                val writer = CharArrayWriter()
                throwable.printStackTrace(PrintWriter(writer))
                val details = writer.toString()
                    .toTcSafeString()

                val message = throwable.message?.toTcSafeString()

                out("testFailed name='$name' duration='$duration' message='$message' details='$details'")

            } else {
                out("testFinished name='$name' duration='$duration'")
            }
        }
    }

    override fun executionStarted(testIdentifier: TestIdentifier) {
        val name = testIdentifier.displayName.toTcSafeString()
        if (testIdentifier.container) {
            out("testSuiteStarted name='$name'")
        } else {
            out("testStarted name='$name'")
        }
    }

    override fun executionSkipped(testIdentifier: TestIdentifier, reason: String) {
        val name = testIdentifier.displayName.toTcSafeString()
        out("testIgnored name='$name' ignoreComment='$reason'")
        out("testFinished name='$name'")
    }
}

private fun String.toTcSafeString(): String {
    return this.replace("|", "||")
        .replace("\n", "|n")
        .replace("\r", "|r")
        .replace("'", "|'")
        .replace("[", "|[")
        .replace("]", "|]")
        .replace(Regex("""\\u(\d\d\d\d)""")) {
            "|0x${it.groupValues[1]}"
        }
}

private fun out(event: String) {
    println("##teamcity[$event]")
}

