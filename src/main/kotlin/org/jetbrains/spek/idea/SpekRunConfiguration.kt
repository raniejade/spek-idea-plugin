package org.jetbrains.spek.idea

import com.intellij.diagnostic.logging.LogConfigurationPanel
import com.intellij.execution.CommonJavaRunConfigurationParameters
import com.intellij.execution.Executor
import com.intellij.execution.JavaRunConfigurationExtensionManager
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.JavaRunConfigurationModule
import com.intellij.execution.configurations.ModuleBasedConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.SettingsEditorGroup
import com.intellij.openapi.util.JDOMExternalizerUtil
import org.jdom.Element
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

    var spec: String
        get() {
            return data.spec
        }
        set(value) {
            data.spec = value
        }

    var scope: String
        get() {
            return data.scope
        }
        set(value) {
            data.scope = value
        }

    override fun getConfigurationEditor(): SettingsEditor<SpekRunConfiguration> {
        return SettingsEditorGroup<SpekRunConfiguration>().apply {
            addEditor("Configuration", SpekSettingsEditor(project))
            JavaRunConfigurationExtensionManager.getInstance().appendEditors(this@SpekRunConfiguration, this)
            addEditor("Logs", LogConfigurationPanel())
        }
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        throw UnsupportedOperationException()
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
        TODO()
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
}
