package org.jetbrains.spek.idea.tooling.execution

import org.junit.platform.engine.DiscoveryFilter
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClassFilter
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.EngineFilter
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import java.io.CharArrayWriter
import java.io.PrintWriter

/**
 * @author Ranie Jade Ramiso
 */
class SpekTestRunner(val spec: String, val scope: String? = null) {
    fun run() {
        val builder = LauncherDiscoveryRequestBuilder.request()
            .filters(EngineFilter.includeEngines(SPEK))

        if (scope != null) {
            var root = UniqueId.forEngine(SPEK)
            UniqueId.parse(scope).segments.forEach {
                root = root.append(it.type, it.value)
            }
            builder.selectors(DiscoverySelectors.selectUniqueId(root))
        }

        val request = builder
            .selectors(DiscoverySelectors.selectClass(spec))
            .build()

        val launcher = LauncherFactory.create()

        launcher.registerTestExecutionListeners(object: TestExecutionListener {
            override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
                if (testIdentifier.parentId.isPresent) {
                    val name = testIdentifier.displayName
                    if (testIdentifier.isContainer) {
                        out("testSuiteFinished name='$name'")
                    } else {
                        if (testExecutionResult.status != TestExecutionResult.Status.SUCCESSFUL) {
                            val throwable = testExecutionResult.throwable.get()
                            val writer = CharArrayWriter()
                            throwable.printStackTrace(PrintWriter(writer))
                            val details = writer.toString()
                                .replace("\n", "|n")
                                .replace("\r", "|r")

                            out("testFailed name='$name' message='${throwable.message}' details='$details'")

                        } else {
                            out("testFinished name='$name'")
                        }
                    }
                }
            }

            override fun executionStarted(testIdentifier: TestIdentifier) {
                if (testIdentifier.parentId.isPresent) {
                    val name = testIdentifier.displayName
                    if (testIdentifier.isContainer) {
                        out("testSuiteStarted name='$name'")
                    } else {
                        out("testStarted name='$name'")
                    }
                }
            }

            override fun testPlanExecutionStarted(testPlan: TestPlan) {
                out("enteredTheMatrix")
            }

            override fun executionSkipped(testIdentifier: TestIdentifier, reason: String) {
                val name = testIdentifier.displayName
                out("testIgnored name='$name' ignoreComment='$reason'")
                out("testFinished name='$name'")
            }
        })

        launcher.execute(request)
    }

    private fun out(event: String) {
        println("##teamcity[$event]")
    }

    companion object {
        const val SPEK = "spek"
    }
}
