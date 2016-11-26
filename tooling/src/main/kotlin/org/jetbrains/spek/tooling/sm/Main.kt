package org.jetbrains.spek.tooling.sm

import joptsimple.OptionParser
import org.jetbrains.spek.tooling.Scope
import org.jetbrains.spek.tooling.Target
import org.jetbrains.spek.tooling.adapter.sm.ServiceMessageAdapter
import org.jetbrains.spek.tooling.runner.junit.JUnitPlatformSpekRunner

/**
 *
 * @author Ranie Jade Ramiso
 */
fun main(vararg args: String) {
    OptionParser().apply {
        val specOption = accepts("spec")
            .withRequiredArg()

        val scopeOption = accepts("scope")
            .withRequiredArg()

        val packageOption = accepts("package")
            .withRequiredArg()

        val adapterOption = accepts("adapter")
            .withRequiredArg()
            .defaultsTo("sm")

        val engineOption = accepts("engine")
            .withRequiredArg()
            .defaultsTo("junit-platform")

        val options = parse(*args)

        val target = if (options.has(specOption)) {
            val scope = if (options.has(scopeOption)) {
                Scope.parse(options.valueOf(scopeOption))
            } else {
                null
            }

            Target.Spec(options.valueOf(specOption), scope)
        } else if (options.has(packageOption)) {
            Target.Package(options.valueOf(packageOption))
        } else {
            throw IllegalArgumentException("Must provide at least spec or package argument.")
        }

        val runner = when (options.valueOf(engineOption)) {
            "junit-platform" -> JUnitPlatformSpekRunner(target)
            else -> throw IllegalArgumentException("Unsupported engine.")
        }

        val adapter = when (options.valueOf(adapterOption)) {
            "sm" -> ServiceMessageAdapter()
            else -> throw IllegalArgumentException("Unsupported adapter.")
        }

        runner.addListener(adapter)

        runner.run()
    }
}
