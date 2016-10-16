package org.jetbrains.spek.tooling.runner.junit

import org.jetbrains.spek.tooling.runner.SpekRunner
import org.jetbrains.spek.tooling.runner.TestExecutionResult
import org.jetbrains.spek.tooling.runner.TestIdentifier
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.EngineFilter
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import java.util.HashMap
import org.junit.platform.engine.TestExecutionResult as JUnitTestExecutionResult
import org.junit.platform.launcher.TestExecutionListener as JUnitTestExecutionListener
import org.junit.platform.launcher.TestIdentifier as JUnitTestIdentifier

/**
 * @author Ranie Jade Ramiso
 */
class JUnitPlatformSpekRunner(val spec: String, val scope: String?): SpekRunner() {
    override fun run() {
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

        val durationMap = HashMap<String, Long>()

        launcher.registerTestExecutionListeners(object: JUnitTestExecutionListener {
            override fun executionFinished(testIdentifier: JUnitTestIdentifier,
                                           testExecutionResult: JUnitTestExecutionResult) {
                if (testIdentifier.parentId.isPresent) {
                    val duration = System.currentTimeMillis() - durationMap[testIdentifier.uniqueId]!!
                    val status = when (testExecutionResult.status) {
                        JUnitTestExecutionResult.Status.SUCCESSFUL -> TestExecutionResult.Status.Success
                        JUnitTestExecutionResult.Status.FAILED -> TestExecutionResult.Status.Failure
                        JUnitTestExecutionResult.Status.ABORTED -> TestExecutionResult.Status.Aborted
                    }

                    this@JUnitPlatformSpekRunner.executionFinished(
                        TestIdentifier(testIdentifier.uniqueId, testIdentifier.displayName, testIdentifier.isContainer),
                        TestExecutionResult(status, duration, testExecutionResult.throwable.orElse(null))
                    )
                }
            }

            override fun executionStarted(testIdentifier: JUnitTestIdentifier) {
                if (testIdentifier.parentId.isPresent) {
                    durationMap.put(testIdentifier.uniqueId, System.currentTimeMillis())
                    this@JUnitPlatformSpekRunner.executionStarted(
                        TestIdentifier(testIdentifier.uniqueId, testIdentifier.displayName, testIdentifier.isContainer)
                    )
                }
            }

            override fun executionSkipped(testIdentifier: JUnitTestIdentifier, reason: String) {
                if (testIdentifier.parentId.isPresent) {
                    this@JUnitPlatformSpekRunner.executionSkipped(
                        TestIdentifier(testIdentifier.uniqueId, testIdentifier.displayName, testIdentifier.isContainer),
                        reason
                    )
                }
            }
        })

        launcher.execute(request)
    }

    companion object {
        val SPEK = "spek"
    }
}
