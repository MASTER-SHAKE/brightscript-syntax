package com.maximpietukhov.brightscript

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.maximpietukhov.brightscript.psi.BrightScriptFunctionDefinition

// Enables Find Usages (Alt+F7) for function and sub declarations
class BrightScriptFindUsagesProvider : FindUsagesProvider {

    override fun getWordsScanner(): WordsScanner = DefaultWordsScanner(
        BrightScriptLexer(),
        TokenSet.create(
            BrightScriptTokenTypes.IDENTIFIER,
            BrightScriptTokenTypes.FUNCTION_CALL,
            BrightScriptTokenTypes.FUNCTION_DECLARATION
        ),
        BrightScriptTokenTypes.COMMENTS,
        BrightScriptTokenTypes.STRINGS
    )

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
        psiElement is BrightScriptFunctionDefinition

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String =
        if (element is BrightScriptFunctionDefinition) "function" else ""

    override fun getDescriptiveName(element: PsiElement): String =
        (element as? BrightScriptFunctionDefinition)?.name ?: ""

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String =
        getDescriptiveName(element)
}
