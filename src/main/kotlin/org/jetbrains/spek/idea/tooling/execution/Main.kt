package org.jetbrains.spek.idea.tooling.execution

/**
 * 1st argument - spec to run
 * 2nd argument (optional) - specific scope to only run
 *
 * @author Ranie Jade Ramiso
 */
fun main(vararg args: String) {
    val runner = if (args.size == 1) {
        SpekTestRunner(args[0])
    } else {
        SpekTestRunner(args[0], args[1])
    }

    runner.run()
}
