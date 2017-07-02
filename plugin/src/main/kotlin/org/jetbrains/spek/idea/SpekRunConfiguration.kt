package org.jetbrains.spek.idea

import com.intellij.diagnostic.logging.LogConfigurationPanel
import com.intellij.execution.CommonJavaRunConfigurationParameters
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.JavaRunConfigurationExtensionManager
import com.intellij.execution.application.BaseJavaApplicationCommandLineState
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.JavaRunConfigurationModule
import com.intellij.execution.configurations.ModuleBasedConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationException
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
import com.intellij.util.Base64
import com.intellij.util.PathUtil
import org.jdom.Element
import org.jetbrains.spek.tooling.Path
import org.jetbrains.spek.tooling.PathType
import org.jetbrains.spek.tooling.Target
import org.junit.platform.commons.annotation.Testable
import org.junit.platform.engine.TestEngine
import org.junit.platform.launcher.Launcher
import org.opentest4j.TestSkippedException
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
        Target.Spec(""),
        "",
        mutableMapOf(),
        true,
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


    override fun suggestedName(): String {
        val target = data.target

        return when (target) {
            is Target.Spec -> {
                if (target.path != null) {
                    var desc = "${target.path?.description}"
                    var current: Path? = target.path!!.next

                    while (current != null) {
                        desc = "$desc->${current.description}"
                        current = current.next
                    }


                    desc
                } else {
                    target.spec
                }
            }
            is Target.Package -> "Specs in ${target.`package`}"
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

                val jars = mutableListOf(
                    /* tooling jar*/
                    PathUtil.getJarPathForClass(Path::class.java)

//                    /* jackson */
//                    PathUtil.getJarPathForClass(JsonFactory::class.java),
//                    PathUtil.getJarPathForClass(JsonCreator::class.java),
//                    PathUtil.getJarPathForClass(BeanProperty::class.java),
//                    PathUtil.getJarPathForClass(KotlinModule::class.java),
//
//                    /* jopt */
//                    PathUtil.getJarPathForClass(OptionParser::class.java)
                )

                if (module.findClass(Launcher::class.qualifiedName) == null) {
                    jars.addAll(arrayOf(
                        /* platform launcher */
                        PathUtil.getJarPathForClass(Launcher::class.java),
                        PathUtil.getJarPathForClass(TestEngine::class.java),
                        PathUtil.getJarPathForClass(Testable::class.java),
                        PathUtil.getJarPathForClass(TestSkippedException::class.java)
                    ))
                }

                params.classPath.addAll(
                    jars
                )

                params.mainClass = MAIN_CLASS
                setupJavaParameters(params)

                val target = this@SpekRunConfiguration.target

                when (target) {
                    is Target.Spec -> {
                        params.programParametersList.add("--spec", target.spec)

                        if (target.path != null) {
                            params.programParametersList.add("--path", Path.serialize(target.path!!))
                        }
                    }
                    is Target.Package -> {
                        params.programParametersList.add("--package", target.`package`)
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

                if (target.path != null) {
                    JDOMExternalizerUtil.writeField(
                        element, "path", Base64.encode(Path.serialize(target.path!!).toByteArray())
                    )
                }


            }
            is Target.Package -> {
                JDOMExternalizerUtil.writeField(element, "target", "package")
                JDOMExternalizerUtil.writeField(element, "package", target.`package`)
            }
        }

        JDOMExternalizerUtil.writeField(element, "vm-parameters", data.vmParameters)
        JDOMExternalizerUtil.writeField(element, "program-parameters", data.programParameters ?: "")
        JDOMExternalizerUtil.writeField(element, "working-directory", data.workingDirectory ?: "")
        if (data.isAlternativeJrePathEnabled) {
            JDOMExternalizerUtil.writeField(element, "alternative-jre-enabled", "true")
        }
        JDOMExternalizerUtil.writeField(element, "alternative-jre-path", data.alternativeJrePath)
        if (data.isPassParentEnvs) {
            JDOMExternalizerUtil.writeField(element, "pass-parent-envs", "true")
        }

        EnvironmentVariablesComponent.writeExternal(element, data.envs)

    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        readModule(element)
        target = when (JDOMExternalizerUtil.readField(element, "target")) {
            "spec" -> {
                val spec = JDOMExternalizerUtil.readField(element, "spec", "")

                // TODO (backwards compatibility): remove after 2 releases from v0.3.0
                val scope = JDOMExternalizerUtil.readField(element, "scope", "").run {
                    if (isNotEmpty()) {
                        val split = this.split(Regex("(?<!\\\\)/"))

                        var current: Path? = null

                        split.reversed().forEach {
                            val typeAndDesc = it.removeSurrounding("[", "]").split(":")
                            val type = when (typeAndDesc[0]) {
                                "spec" -> PathType.SPEC
                                "group" -> PathType.GROUP
                                "test" -> PathType.TEST
                                else -> throw IllegalArgumentException("not valid type '${typeAndDesc[0]}'")
                            }

                            current = Path(type, typeAndDesc[1], current)
                        }

                        current

                    } else {
                        null
                    }
                }

                val path = JDOMExternalizerUtil.readField(element, "path", "").run {
                    if (isNotEmpty()) {
                        Path.deserialize(String(Base64.decode(this)))
                    } else {
                        null
                    }
                }

                Target.Spec(spec, scope ?: path)
            }
            "package" -> {
                Target.Package(JDOMExternalizerUtil.readField(element, "package", ""))
            }
            else -> {
                Target.Spec("")
            }
        }

        data.vmParameters = JDOMExternalizerUtil.readField(element, "vm-parameters", "")
        data.programParameters = JDOMExternalizerUtil.readField(element, "program-parameters", "")
        data.workingDirectory = JDOMExternalizerUtil.readField(element, "working-directory", "")
        data.isAlternativeJrePathEnabled = JDOMExternalizerUtil.readField(element, "alternative-jre-enabled", "")
            .isNotBlank()
        data.alternativeJrePath = JDOMExternalizerUtil.readField(element, "alternative-jre-path", "")
        data.isPassParentEnvs = JDOMExternalizerUtil.readField(element, "pass-parent-envs", "").isNotBlank()

        EnvironmentVariablesComponent.readExternal(element, data.envs)
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


    override fun checkConfiguration() {
        val target = this.target
        when (target) {
            is Target.Spec -> {
                if (target.spec.isEmpty()) {
                    throw RuntimeConfigurationException("Spec can't be empty.")
                }
            }
            is Target.Package -> {
                if (target.`package`.isEmpty()) {
                    throw RuntimeConfigurationException("Package can't be empty.")
                }
            }
        }
    }

    companion object {
        val MAIN_CLASS = "org.jetbrains.spek.tooling.MainKt"
    }
}
