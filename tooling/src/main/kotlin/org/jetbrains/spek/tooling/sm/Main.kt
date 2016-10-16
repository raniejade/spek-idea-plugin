package org.jetbrains.spek.tooling.sm

import org.jetbrains.spek.tooling.runner.TestExecutionListener
import org.jetbrains.spek.tooling.runner.TestExecutionResult
import org.jetbrains.spek.tooling.runner.TestIdentifier
import org.jetbrains.spek.tooling.runner.junit.JUnitPlatformSpekRunner
import java.io.CharArrayWriter
import java.io.PrintWriter

/**
 * 1st argument - spec to run
 * 2nd argument (optional) - specific scope to only run
 *
 * @author Ranie Jade Ramiso
 */
fun main(vararg args: String) {
    val runner = if (args.size == 1) {
        JUnitPlatformSpekRunner(args[0], null)
    } else {
        JUnitPlatformSpekRunner(args[0], args[1])
    }

    runner.addListener(object: TestExecutionListener() {
        override fun executionFinished(test: TestIdentifier, result: TestExecutionResult) {
            val name = test.displayName
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
            val name = testIdentifier.displayName
            if (testIdentifier.container) {
                out("testSuiteStarted name='$name'")
            } else {
                out("testStarted name='$name'")
            }
        }

        override fun executionSkipped(testIdentifier: TestIdentifier, reason: String) {
            val name = testIdentifier.displayName
            out("testIgnored name='$name' ignoreComment='$reason'")
            out("testFinished name='$name'")
        }
    })

    runner.run()
}

private fun String.toTcSafeString(): String {
    return this.replace("\n", "|n")
        .replace("\r", "|r")
}

private fun out(event: String) {
    println("##teamcity[$event]")
}
