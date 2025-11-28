package com.maximpietukhov.brightscript.psi

import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType

class BrightScriptFunctionCallElement(type: IElementType, text: CharSequence) : LeafPsiElement(type, text) {

    override fun getReference(): PsiReference? {
        val name = text.toString()
        if (name.isNotEmpty()) {
            return BrightScriptFunctionReference(this, name)
        }
        return null
    }

    override fun getReferences(): Array<PsiReference> {
        val ref = reference
        return if (ref != null) arrayOf(ref) else PsiReference.EMPTY_ARRAY
    }
}
