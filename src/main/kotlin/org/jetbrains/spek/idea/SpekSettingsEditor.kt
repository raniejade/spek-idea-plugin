package org.jetbrains.spek.idea

import com.intellij.application.options.ModulesComboBox
import com.intellij.execution.ui.ClassBrowser
import com.intellij.ide.util.ClassFilter
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
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
    lateinit var module: ModulesComboBox
    lateinit var filter: EditorTextFieldWithBrowseButton
    lateinit var query: TextFieldWithHistory


    override fun resetEditorFrom(configuration: SpekRunConfiguration) {
        module.selectedModule = configuration.configurationModule.module
        filter.text = configuration.filter
        query.text = configuration.query
    }

    override fun applyEditorTo(configuration: SpekRunConfiguration) {
        configuration.setModule(module.selectedModule)
        configuration.filter = filter.text
        configuration.query = query.text
    }

    override fun createEditor() = panel

    private fun createUIComponents() {
        module = ModulesComboBox()
        module.fillModules(project)

        query = TextFieldWithHistory()
        filter = EditorTextFieldWithBrowseButton(
            project,
            true,
            JavaCodeFragment.VisibilityChecker.EVERYTHING_VISIBLE,
            PlainTextLanguage.INSTANCE.associatedFileType
        )

        val scope: GlobalSearchScope
        if (module.selectedModule == null) {
            scope = GlobalSearchScope.allScope(project)
        } else {
            scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module.selectedModule!!)
        }


        val browser = object: ClassBrowser(project, "") {
            override fun getFilter(): ClassFilter.ClassFilterWithScope? {


                return object: ClassFilter.ClassFilterWithScope {
                    override fun getScope() = scope

                    override fun isAccepted(aClass: PsiClass): Boolean {
                        return aClass.superTypes.contains(
                            PsiClassType.getTypeByName("io.polymorphicpanda.kspec.KSpec", project, scope)
                        )
                    }
                }
            }


            override fun findClass(className: String): PsiClass? {
                return JavaPsiFacade.getInstance(project)
                    .findClass(className, scope)
            }

        }

        browser.setField(filter)

    }
}
