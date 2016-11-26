package org.jetbrains.spek.idea

import com.intellij.application.options.ModulesComboBox
import com.intellij.execution.configuration.BrowseModuleValueActionListener
import com.intellij.execution.ui.ClassBrowser
import com.intellij.execution.ui.CommonJavaParametersPanel
import com.intellij.execution.ui.ConfigurationModuleSelector
import com.intellij.execution.ui.DefaultJreSelector
import com.intellij.execution.ui.JrePathEditor
import com.intellij.ide.util.ClassFilter
import com.intellij.ide.util.PackageChooserDialog
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.module.Module
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.psi.JavaCodeFragment
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.EditorTextFieldWithBrowseButton
import com.intellij.ui.ListCellRendererWrapper
import org.jetbrains.spek.tooling.Scope
import org.jetbrains.spek.tooling.Target
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JTextField
import kotlin.reflect.KClass

/**
 * @author Ranie Jade Ramiso
 */
class SpekSettingsEditor(val project: Project): SettingsEditor<SpekRunConfiguration>() {
    lateinit var panel: JPanel
    lateinit var specPanel: JPanel
    lateinit var packagePanel: JPanel

    lateinit var commonJavaParameters: CommonJavaParametersPanel
    lateinit var module: LabeledComponent<ModulesComboBox>
    lateinit var jrePathEditor: JrePathEditor
    lateinit var spec: LabeledComponent<EditorTextFieldWithBrowseButton>
    lateinit var scope: LabeledComponent<TextFieldWithBrowseButton>
    lateinit var testPackage: LabeledComponent<TextFieldWithBrowseButton>
    lateinit var type: LabeledComponent<JComboBox<KClass<out Target>>>

    var moduleSelector: ConfigurationModuleSelector

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

    private var selectedPackage: String
        get() {
            return testPackage.component.text
        }
        set (value) {
            testPackage.component.text = value
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

        type.component.selectedIndex = 0

        val packageBrowser = object: BrowseModuleValueActionListener<JTextField>(project) {
            override fun showDialog(): String {
                val dialog = PackageChooserDialog("Select package", project)
                dialog.show()

                return dialog.selectedPackage?.qualifiedName ?: ""
            }
        }

        packageBrowser.setField(testPackage.component)
    }


    override fun resetEditorFrom(configuration: SpekRunConfiguration) {
        selectedModule = configuration.configurationModule.module
        moduleSelector.reset(configuration)
        commonJavaParameters.reset(configuration)

        val target = configuration.target

        when (target) {
            is Target.Spec -> {
                type.component.selectedItem = Target.Spec::class
                selectedSpec = target.spec
                selectedScope = target.scope?.serializedForm() ?: ""
            }
            is Target.Package -> {
                type.component.selectedItem = Target.Package::class
                selectedPackage = target.`package`
            }
        }
    }

    override fun applyEditorTo(configuration: SpekRunConfiguration) {
        configuration.setModule(selectedModule)
        moduleSelector.applyTo(configuration)
        commonJavaParameters.applyTo(configuration)


        val selectedType = type.component.selectedItem

        when (selectedType) {
            Target.Spec::class -> {
                val scope = if (selectedScope.isNotEmpty()) {
                    Scope.parse(selectedScope)
                } else {
                    null
                }
                configuration.target = Target.Spec(selectedSpec, scope)
            }
            Target.Package::class -> {
                configuration.target = Target.Package(selectedPackage)
            }
        }
    }

    override fun createEditor() = panel

    private fun createUIComponents() {
        scope = LabeledComponent.create(
            TextFieldWithBrowseButton(),
            "Scope", "West"
        )

        scope.component.isEditable = false

        spec = LabeledComponent.create(EditorTextFieldWithBrowseButton(
            project,
            true,
            JavaCodeFragment.VisibilityChecker.EVERYTHING_VISIBLE,
            PlainTextLanguage.INSTANCE.associatedFileType
        ), "Spec", "West")

        spec.component.childComponent.addDocumentListener(object: DocumentListener {
            override fun documentChanged(event: DocumentEvent?) {
                selectedScope = ""
            }

            override fun beforeDocumentChange(event: DocumentEvent?) {
            }

        })

        testPackage = LabeledComponent.create(
            TextFieldWithBrowseButton(),
            "Scope", "West"
        )

        testPackage.component.isEditable = false

        val comboBox = JComboBox<KClass<out Target>>()
        type = LabeledComponent.create(
            comboBox, "Type", "West"
        )

        comboBox.model = DefaultComboBoxModel(
            arrayOf(
                Target.Spec::class,
                Target.Package::class
            )
        )

        comboBox.renderer = object: ListCellRendererWrapper<KClass<out Target>>() {
            override fun customize(list: JList<*>, value: KClass<out Target>, index: Int,
                                   selected: Boolean, hasFocus: Boolean) {
                when (value) {
                    Target.Spec::class -> this.setText("Spec")
                    Target.Package::class -> this.setText("Package")
                }
            }
        }

        comboBox.addActionListener {
            when (comboBox.selectedItem) {
                Target.Spec::class -> {
                    specPanel.isVisible = true
                    packagePanel.isVisible = false
                }
                Target.Package::class -> {
                    specPanel.isVisible = false
                    packagePanel.isVisible = true
                }
            }
        }

    }
}
