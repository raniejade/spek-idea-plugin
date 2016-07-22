package org.jetbrains.spek.idea

import com.intellij.diagnostic.logging.LogConfigurationPanel
import com.intellij.execution.*
import com.intellij.execution.application.BaseJavaApplicationCommandLineState
import com.intellij.execution.configurations.*
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
import com.intellij.psi.PsiClassType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.PathUtil
import org.jdom.Element
import org.jetbrains.kotlin.asJava.KtLightClass
import org.jetbrains.kotlin.asJava.KtLightClassForExplicitDeclaration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.spek.idea.tooling.execution.SpekTestRunner
import org.junit.platform.commons.util.PreconditionViolationException
import org.junit.platform.engine.TestEngine
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.opentest4j.TestAbortedException
import java.util.*

/**
 * @author Ranie Jade Ramiso
 */
class SpekRunConfiguration(javaRunConfigurationModule: JavaRunConfigurationModule, factory: ConfigurationFactory, name: String)
: ModuleBasedConfiguration<JavaRunConfigurationModule>(name, javaRunConfigurationModule, factory), CommonJavaRunConfigurationParameters {

    data class Data(
        var spec: String,
        var scope: String,
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
        "",
        "",
        mutableMapOf(),
        false,
        "",
        "",
        "",
        "",
        false
    )

    private val model = SpekModel()

    var spec: String
        get() {
            return data.spec
        }
        set(value) {
            data.spec = value
            if (value.isNotEmpty()) {
                model.spec = (PsiClassType.getTypeByName(value, project, searchScope)
                    .resolve() as KtLightClassForExplicitDeclaration).kotlinOrigin
            }
        }

    var scope: String
        get() {
            return data.scope
        }
        set(value) {
            data.scope = value
        }


    override fun suggestedName(): String {
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

                val toolingJar = PathUtil.getJarPathForClass(SpekTestRunner::class.java)

                // TODO: temporary - list directory
                val junitLauncherJar = PathUtil.getJarPathForClass(LauncherDiscoveryRequest::class.java)
                val junitCommonJar = PathUtil.getJarPathForClass(PreconditionViolationException::class.java)
                val junitPlatformJar = PathUtil.getJarPathForClass(TestEngine::class.java)
                val openTest4jJar = PathUtil.getJarPathForClass(TestAbortedException::class.java)

                params.classPath.addAll(
                    mutableListOf(toolingJar, openTest4jJar, junitCommonJar, junitPlatformJar, junitLauncherJar)
                )

                params.mainClass = MAIN_CLASS
                setupJavaParameters(params)

                params.programParametersList.add(data.spec)

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
        JDOMExternalizerUtil.writeField(element, "scope", scope)
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        readModule(element)
        spec = JDOMExternalizerUtil.readField(element, "spec", "")
        scope = JDOMExternalizerUtil.readField(element, "scope", "")
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
        val MAIN_CLASS = "org.jetbrains.spek.idea.tooling.execution.MainKt"
    }
}
