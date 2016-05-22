package io.polymorphicpanda.kspec.idea

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.JavaRunConfigurationModule
import com.intellij.execution.configurations.ModuleBasedConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.util.JDOMExternalizerUtil
import org.jdom.Element
import java.util.*

/**
 * @author Ranie Jade Ramiso
 */
class KSpecRunConfiguration(javaRunConfigurationModule: JavaRunConfigurationModule, factory: ConfigurationFactory, name: String)
: ModuleBasedConfiguration<JavaRunConfigurationModule>(name, javaRunConfigurationModule, factory) {

    override fun getValidModules(): MutableCollection<Module> {
        return Arrays.asList(*ModuleManager.getInstance(project).modules)
    }

    var filter = ""
    var query = ""

    override fun getConfigurationEditor() = KSpecSettingsEditor(project)

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        throw UnsupportedOperationException()
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        writeModule(element)
        JDOMExternalizerUtil.writeField(element, "filter", filter)
        JDOMExternalizerUtil.writeField(element, "query", query)
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        readModule(element)
        filter = JDOMExternalizerUtil.readField(element, "filter", "")
        query = JDOMExternalizerUtil.readField(element, "query", "")
    }
}
