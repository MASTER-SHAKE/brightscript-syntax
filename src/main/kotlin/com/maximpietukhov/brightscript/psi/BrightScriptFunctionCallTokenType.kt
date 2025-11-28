package com.maximpietukhov.brightscript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.ILeafElementType
import com.maximpietukhov.brightscript.BrightScriptLanguage

class BrightScriptFunctionCallTokenType : IElementType("FUNCTION_CALL", BrightScriptLanguage.INSTANCE), ILeafElementType {
    override fun createLeafNode(leafText: CharSequence): ASTNode {
        return BrightScriptFunctionCallElement(this, leafText)
    }
}
