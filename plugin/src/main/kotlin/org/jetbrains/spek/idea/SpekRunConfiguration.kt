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
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.PathUtil
import org.jdom.Element
import org.jetbrains.spek.tooling.Scope
import java.nio.file.Paths
import java.util.Arrays

/**
 * @author Ranie Jade Ramiso
 */
class SpekRunConfiguration(javaRunConfigurationModule: JavaRunConfigurationModule, factory: ConfigurationFactory, name: String)
: ModuleBasedConfiguration<JavaRunConfigurationModule>(name, javaRunConfigurationModule, factory), CommonJavaRunConfigurationParameters {

    data class Data(
        var spec: String,
        var scope: Scope?,
        var alternativeJrePath: String,
        var envs: MutableMap<String, String>,
        var isPassParentEnvs: Boolean,
        var runClass: String,
        var workingDirectory: String?,
        var programParameters: String?,
        var vmParameters: String,
        var isAlternativeJrePathEnabled: Boolean
    )

    private val searchScope: GlobalSearchScope
        get() {
            return configurationModule.searchScope
        }

    override fun getValidModules(): MutableCollection<Module> {
        return Arrays.asList(*ModuleManager.getInstance(project).modules)
    }

    private val data = Data(
        "",
        null,
        "",
        mutableMapOf(),
        false,
        "",
        "",
        "",
        "",
        false
    )

    var spec: String
        get() {
            return data.spec
        }
        set(value) {
            data.spec = value
        }

    var scope: Scope?
        get() {
            return data.scope
        }
        set(value) {
            data.scope = value
        }


    override fun suggestedName(): String {
        if (scope != null) {
            return "$spec - ${scope.toString()}"
        }
        return spec
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

                params.programParametersList.add(spec)

                if (data.scope != null) {
                    params.programParametersList.add(data.scope!!.serializedForm())
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
        JDOMExternalizerUtil.writeField(element, "spec", spec)
        JDOMExternalizerUtil.writeField(element, "scope", scope?.serializedForm())
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        readModule(element)
        spec = JDOMExternalizerUtil.readField(element, "spec", "")
        val scope = JDOMExternalizerUtil.readField(element, "scope", "")
        if (!scope.isEmpty()) {
            this.scope = Scope.parse(scope)
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
