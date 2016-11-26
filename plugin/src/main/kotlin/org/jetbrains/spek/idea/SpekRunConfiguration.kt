package org.jetbrains.spek.idea

import com.intellij.diagnostic.logging.LogConfigurationPanel
import com.intellij.execution.CommonJavaRunConfigurationParameters
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.JavaRunConfigurationExtensionManager
import com.intellij.execution.application.BaseJavaApplicationCommandLineState
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.JavaRunConfigurationModule
import com.intellij.execution.configurations.ModuleBasedConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.util.JavaParametersUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.SettingsEditorGroup
import com.intellij.openapi.util.JDOMExternalizerUtil
import com.intellij.util.PathUtil
import org.jdom.Element
import org.jetbrains.spek.tooling.Scope
import org.jetbrains.spek.tooling.Target
import java.nio.file.Paths
import java.util.Arrays

/**
 * @author Ranie Jade Ramiso
 */
class SpekRunConfiguration(javaRunConfigurationModule: JavaRunConfigurationModule, factory: ConfigurationFactory, name: String)
: ModuleBasedConfiguration<JavaRunConfigurationModule>(name, javaRunConfigurationModule, factory), CommonJavaRunConfigurationParameters {

    data class Data(
        var target: Target,
        var alternativeJrePath: String,
        var envs: MutableMap<String, String>,
        var isPassParentEnvs: Boolean,
        var runClass: String,
        var workingDirectory: String?,
        var programParameters: String?,
        var vmParameters: String,
        var isAlternativeJrePathEnabled: Boolean
    )

    override fun getValidModules(): MutableCollection<Module> {
        return Arrays.asList(*ModuleManager.getInstance(project).modules)
    }

    private val data = Data(
        Target.Spec("", null),
        "",
        mutableMapOf(),
        false,
        "",
        "",
        "",
        "",
        false
    )

    var target: Target
        get() {
            return data.target
        }
        set(value) {
            data.target = value
        }

//    var spec: String
//        get() {
//            return data.spec
//        }
//        set(value) {
//            data.spec = value
//        }
//
//    var scope: Scope?
//        get() {
//            return data.scope
//        }
//        set(value) {
//            data.scope = value
//        }


    override fun suggestedName(): String {
        val target = data.target

        return when (target) {
            is Target.Spec -> {
                if (target.scope != null) {
                    "${target.spec} - ${target.scope}"
                } else {
                    target.spec
                }
            }
            is Target.Package -> target.`package`
        }
    }

    override fun getConfigurationEditor(): SettingsEditor<SpekRunConfiguration> {
        return SettingsEditorGroup<SpekRunConfiguration>().apply {
            addEditor("Configuration", SpekSettingsEditor(project))
            JavaRunConfigurationExtensionManager.getInstance().appendEditors(this@SpekRunConfiguration, this)
            addEditor("Logs", LogConfigurationPanel())
        }
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return object: BaseJavaApplicationCommandLineState<SpekRunConfiguration>(environment, this) {
            override fun createJavaParameters(): JavaParameters {
                val params = JavaParameters()
                params.isUseClasspathJar = true
                val module = myConfiguration.configurationModule
                val jreHome = if (myConfiguration.isAlternativeJrePathEnabled) {
                    myConfiguration.alternativeJrePath
                } else {
                    null
                }

                if (module.module != null) {
                    val classPathType = JavaParametersUtil.getClasspathType(module, MAIN_CLASS, false)
                    JavaParametersUtil.configureModule(module, params, classPathType, jreHome)
                } else {
                    JavaParametersUtil.configureProject(
                        module.project, params, JavaParameters.JDK_AND_CLASSES_AND_TESTS, jreHome
                    )
                }

                val jars = Paths.get(PathUtil.getJarPathForClass(Scope::class.java))
                    .parent

                params.classPath.addAll(
                    mutableListOf("$jars/*")
                )

                params.mainClass = MAIN_CLASS
                setupJavaParameters(params)

                val target = this@SpekRunConfiguration.target

                when (target) {
                    is Target.Spec -> {
                        params.programParametersList.add("--spec", target.spec)

                        if (target.scope != null) {
                            params.programParametersList.add("--scope", target.scope!!.serializedForm())
                        }
                    }
                }



                return params
            }

            fun createConsole(executor: Executor, processHandler: ProcessHandler): ConsoleView {
                val consoleProperties = object: SMTRunnerConsoleProperties(
                    this@SpekRunConfiguration, "cucumber", executor
                ) {
                    override fun getTestLocator() = SpekTestLocator()
                }
                return SMTestRunnerConnectionUtil.createAndAttachConsole(
                    "spek",
                    processHandler,
                    consoleProperties
                )
            }

            override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
                val processHandler = startProcess()
                val console = createConsole(executor, processHandler)
                return DefaultExecutionResult(
                    console, processHandler, *createActions(console, processHandler, executor)
                )
            }
        }
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        writeModule(element)

        val target = this.target

        when (target) {
            is Target.Spec -> {
                JDOMExternalizerUtil.writeField(element, "target", "spec")
                JDOMExternalizerUtil.writeField(element, "spec", target.spec)
                JDOMExternalizerUtil.writeField(element, "scope", target.scope?.serializedForm())
            }
            is Target.Package -> {
                JDOMExternalizerUtil.writeField(element, "target", "package")
                JDOMExternalizerUtil.writeField(element, "package", target.`package`)
            }
        }

    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        readModule(element)
        target = when (JDOMExternalizerUtil.readField(element, "target")) {
            "spec" -> {
                val spec = JDOMExternalizerUtil.readField(element, "spec", "")
                val scope = JDOMExternalizerUtil.readField(element, "scope", "").run {
                    if (isNotEmpty()) {
                        Scope.parse(this)
                    } else {
                        null
                    }
                }
                Target.Spec(spec, scope)
            }
            "package" -> {
                Target.Package(JDOMExternalizerUtil.readField(element, "package", ""))
            }
            else -> {
                throw IllegalArgumentException("Invalid run configuration")
            }
        }
    }

    override fun setAlternativeJrePath(path: String) {
        data.alternativeJrePath = path
    }

    override fun getEnvs(): MutableMap<String, String> = data.envs

    override fun isPassParentEnvs() = data.isPassParentEnvs

    override fun getRunClass() = data.runClass

    override fun getWorkingDirectory() = data.workingDirectory

    override fun setPassParentEnvs(passParentEnvs: Boolean) {
        data.isPassParentEnvs = passParentEnvs
    }

    override fun getProgramParameters() = data.programParameters

    override fun getAlternativeJrePath() = data.alternativeJrePath

    override fun setVMParameters(value: String) {
        data.vmParameters = value
    }

    override fun setProgramParameters(value: String?) {
        data.programParameters = value
    }

    override fun isAlternativeJrePathEnabled() = data.isAlternativeJrePathEnabled

    override fun getPackage(): String {
        return ""
    }

    override fun setAlternativeJrePathEnabled(enabled: Boolean) {
        data.isAlternativeJrePathEnabled = enabled
    }

    override fun getVMParameters() = data.vmParameters

    override fun setWorkingDirectory(value: String?) {
        data.workingDirectory = value
    }

    override fun setEnvs(envs: MutableMap<String, String>) {
        data.envs = envs
    }

    companion object {
        val MAIN_CLASS = "org.jetbrains.spek.tooling.sm.MainKt"
    }
}
