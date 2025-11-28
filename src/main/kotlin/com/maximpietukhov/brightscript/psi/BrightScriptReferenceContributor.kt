package com.maximpietukhov.brightscript.psi

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.maximpietukhov.brightscript.BrightScriptLanguage
import com.maximpietukhov.brightscript.BrightScriptTokenTypes

class BrightScriptReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // Register reference provider for FUNCTION_CALL tokens
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement()
                .withElementType(StandardPatterns.`object`(BrightScriptTokenTypes.FUNCTION_CALL)),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val name = element.text
                    if (name.isNotEmpty()) {
                        return arrayOf(BrightScriptFunctionReference(element, name))
                    }
                    return PsiReference.EMPTY_ARRAY
                }
            }
        )
    }
}
