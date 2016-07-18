package org.jetbrains.spek.idea.tooling.execution

/**
 * @author Ranie Jade Ramiso
 */
fun main(vararg args: String) {
//    println("##teamcity[testSuiteStarted name='${args[0]}' locationHint='java:suite://${args[0]}']")
//    println("##teamcity[testSuiteFinished name='${args[0]}']")

    SpekTestRunner(args[0]).run()
}
