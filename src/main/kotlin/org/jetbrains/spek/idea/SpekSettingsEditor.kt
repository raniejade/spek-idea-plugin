package org.jetbrains.spek.idea

import com.intellij.application.options.ModulesComboBox
import com.intellij.execution.ui.*
import com.intellij.ide.util.ClassFilter
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.module.Module
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.psi.JavaCodeFragment
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.EditorTextFieldWithBrowseButton
import com.intellij.ui.TextFieldWithHistory
import javax.swing.JPanel

/**
 * @author Ranie Jade Ramiso
 */
class SpekSettingsEditor(val project: Project): SettingsEditor<SpekRunConfiguration>() {
    lateinit var panel: JPanel
    lateinit var commonJavaParameters: CommonJavaParametersPanel
    lateinit var module: LabeledComponent<ModulesComboBox>
    lateinit var jrePathEditor: JrePathEditor
    lateinit var spec: LabeledComponent<EditorTextFieldWithBrowseButton>
    lateinit var scope: LabeledComponent<TextFieldWithHistory>

    lateinit var moduleSelector: ConfigurationModuleSelector

    private val searchScope: GlobalSearchScope
        get() {
            return if (selectedModule == null) {
                GlobalSearchScope.allScope(project)
            } else {
                GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(selectedModule!!)
            }
        }

    private var selectedModule: Module?
        get() {
            return module.component.selectedModule
        }
        set(value) {
            module.component.selectedModule = value
        }

    private var selectedSpec: String
        get() {
            return spec.component.text
        }
        set(value) {
            spec.component.text = value
        }

    private var selectedScope: String
        get() {
            return scope.component.text
        }
        set(value) {
            scope.component.text = value
        }

    init {
        module.component.fillModules(project)
        moduleSelector = ConfigurationModuleSelector(project, module.component)
        jrePathEditor.setDefaultJreSelector(DefaultJreSelector.fromModuleDependencies(module.component, false))
        commonJavaParameters.setModuleContext(selectedModule)
        commonJavaParameters.setHasModuleMacro()
        module.component.addActionListener {
            commonJavaParameters.setModuleContext(selectedModule)
        }

        val spekCls = PsiClassType.getTypeByName("org.jetbrains.spek.api.Spek", project, searchScope)
        val subjectSpekCls = PsiClassType.getTypeByName("org.jetbrains.spek.api.SubjectSpek", project, searchScope)

        val browser = object: ClassBrowser(project, "") {
            override fun getFilter(): ClassFilter.ClassFilterWithScope? {


                return object: ClassFilter.ClassFilterWithScope {
                    override fun getScope() = searchScope

                    override fun isAccepted(aClass: PsiClass): Boolean {
                        return aClass.superTypes.filter {
                            it == spekCls || it.rawType() == subjectSpekCls
                        }.any()
                    }
                }
            }


            override fun findClass(className: String): PsiClass? {
                return JavaPsiFacade.getInstance(project)
                    .findClass(className, searchScope)
            }
        }

        browser.setField(spec.component)
    }


    override fun resetEditorFrom(configuration: SpekRunConfiguration) {
        selectedModule = configuration.configurationModule.module
        moduleSelector.reset(configuration)
        commonJavaParameters.reset(configuration)
        selectedSpec = configuration.spec
        selectedScope = configuration.scope
    }

    override fun applyEditorTo(configuration: SpekRunConfiguration) {
        configuration.setModule(selectedModule)
        moduleSelector.applyTo(configuration)
        commonJavaParameters.applyTo(configuration)
        configuration.spec = selectedSpec
        configuration.scope = selectedScope
    }

    override fun createEditor() = panel

    private fun createUIComponents() {
        scope = LabeledComponent.create(
            TextFieldWithHistory(), "Scope", "West"
        )
        scope.isEnabled = false

        spec = LabeledComponent.create(EditorTextFieldWithBrowseButton(
            project,
            true,
            JavaCodeFragment.VisibilityChecker.EVERYTHING_VISIBLE,
            PlainTextLanguage.INSTANCE.associatedFileType
        ), "Spec", "West")

    }
}
