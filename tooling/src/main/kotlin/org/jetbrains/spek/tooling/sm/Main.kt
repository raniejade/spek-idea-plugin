package org.jetbrains.spek.tooling.sm

import org.jetbrains.spek.tooling.Scope
import org.jetbrains.spek.tooling.Target
import org.jetbrains.spek.tooling.adapter.sm.ServiceMessageAdapter
import org.jetbrains.spek.tooling.runner.junit.JUnitPlatformSpekRunner

/**
 * 1st argument - spec to run
 * 2nd argument (optional) - specific scope to only run
 *
 * @author Ranie Jade Ramiso
 */
fun main(vararg args: String) {
    val target = if (args.size == 1) {
        Target.Spec(args[0])
    } else {
        Target.Spec(args[0], Scope.parse(args[1]))
    }

    val runner = JUnitPlatformSpekRunner(target)

    runner.addListener(ServiceMessageAdapter())

    runner.run()
}
