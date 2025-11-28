package com.maximpietukhov.brightscript.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.maximpietukhov.brightscript.BrightScriptFile

class BrightScriptFunctionReference(
    element: PsiElement,
    private val functionName: String
) : PsiReferenceBase<PsiElement>(element, TextRange(0, element.textLength)), PsiPolyVariantReference {

    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        // If there's a match in current file, prefer it
        return results.firstOrNull()?.element
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val currentFileResults = mutableListOf<ResolveResult>()
        val otherFileResults = mutableListOf<ResolveResult>()

        val currentFile = element.containingFile

        // Search in the current file first
        if (currentFile is BrightScriptFile) {
            findFunctionsInFile(currentFile, currentFileResults)
        }

        // If found in current file, return only that (most likely the intended target)
        if (currentFileResults.isNotEmpty()) {
            return currentFileResults.toTypedArray()
        }

        // Otherwise search in all project files
        val project = element.project
        val psiManager = PsiManager.getInstance(project)
        val scope = com.intellij.psi.search.GlobalSearchScope.projectScope(project)

        com.intellij.psi.search.FilenameIndex.getAllFilesByExt(project, "brs", scope).forEach { virtualFile ->
            val psiFile = psiManager.findFile(virtualFile)
            if (psiFile is BrightScriptFile && psiFile != currentFile) {
                findFunctionsInFile(psiFile, otherFileResults)
            }
        }

        return otherFileResults.toTypedArray()
    }

    private fun findFunctionsInFile(file: PsiFile, results: MutableList<ResolveResult>) {
        PsiTreeUtil.findChildrenOfType(file, BrightScriptFunctionDefinition::class.java).forEach { funcDef ->
            val name = funcDef.name
            if (name != null && name.equals(functionName, ignoreCase = true)) {
                results.add(PsiElementResolveResult(funcDef))
            }
        }
    }

    override fun getVariants(): Array<Any> {
        // For auto-completion (will implement later)
        return emptyArray()
    }
}
