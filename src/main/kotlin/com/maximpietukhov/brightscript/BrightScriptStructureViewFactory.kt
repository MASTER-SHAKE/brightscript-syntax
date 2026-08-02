package com.maximpietukhov.brightscript

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.maximpietukhov.brightscript.psi.BrightScriptFunctionDefinition
import javax.swing.Icon

// Structure View (Ctrl+F12 / Alt+7) - lists functions and subs in the file
class BrightScriptStructureViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder? {
        if (psiFile !is BrightScriptFile) return null
        return object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel {
                return BrightScriptStructureViewModel(psiFile, editor)
            }
        }
    }
}

class BrightScriptStructureViewModel(file: BrightScriptFile, editor: Editor?) :
    StructureViewModelBase(file, editor, BrightScriptStructureViewElement(file)),
    StructureViewModel.ElementInfoProvider {

    override fun getSorters(): Array<Sorter> = arrayOf(Sorter.ALPHA_SORTER)

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean = false

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean =
        element.value is BrightScriptFunctionDefinition
}

class BrightScriptStructureViewElement(private val element: PsiElement) :
    StructureViewTreeElement, SortableTreeElement {

    override fun getValue(): Any = element

    override fun navigate(requestFocus: Boolean) {
        (element as? NavigatablePsiElement)?.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = (element as? NavigatablePsiElement)?.canNavigate() ?: false

    override fun canNavigateToSource(): Boolean = (element as? NavigatablePsiElement)?.canNavigateToSource() ?: false

    override fun getAlphaSortKey(): String = when (element) {
        is BrightScriptFunctionDefinition -> element.name ?: ""
        is PsiFile -> element.name
        else -> ""
    }

    override fun getPresentation(): ItemPresentation =
        PresentationData(presentableText(), null, icon(), null)

    private fun presentableText(): String {
        if (element is PsiFile) return element.name
        if (element is BrightScriptFunctionDefinition) {
            // show "name(params)" extracted from the header line
            val header = element.text.lineSequence().firstOrNull() ?: ""
            val match = Regex("^\\s*(function|sub)\\s+(\\w+)\\s*\\(([^)]*)\\)", RegexOption.IGNORE_CASE).find(header)
            if (match != null) {
                val name = match.groupValues[2]
                val params = match.groupValues[3].trim()
                return if (params.isEmpty()) "$name()" else "$name($params)"
            }
            return element.name ?: "unnamed"
        }
        return ""
    }

    private fun icon(): Icon? = when (element) {
        is BrightScriptFunctionDefinition -> AllIcons.Nodes.Function
        is PsiFile -> BrightScriptIcons.FILE
        else -> null
    }

    override fun getChildren(): Array<TreeElement> {
        if (element is BrightScriptFile) {
            return PsiTreeUtil.findChildrenOfType(element, BrightScriptFunctionDefinition::class.java)
                .map { BrightScriptStructureViewElement(it) }
                .toTypedArray()
        }
        return emptyArray()
    }
}
