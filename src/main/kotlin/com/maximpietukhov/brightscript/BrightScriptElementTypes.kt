package com.maximpietukhov.brightscript

import com.intellij.psi.tree.IElementType

class BrightScriptElementType(debugName: String) : IElementType(debugName, BrightScriptLanguage.INSTANCE)

object BrightScriptElementTypes {
    @JvmField val FUNCTION_BLOCK = BrightScriptElementType("FUNCTION_BLOCK")
    @JvmField val SUB_BLOCK = BrightScriptElementType("SUB_BLOCK")
    @JvmField val IF_BLOCK = BrightScriptElementType("IF_BLOCK")
    @JvmField val FOR_BLOCK = BrightScriptElementType("FOR_BLOCK")
    @JvmField val WHILE_BLOCK = BrightScriptElementType("WHILE_BLOCK")
    @JvmField val CLASS_BLOCK = BrightScriptElementType("CLASS_BLOCK")
    @JvmField val NAMESPACE_BLOCK = BrightScriptElementType("NAMESPACE_BLOCK")
    @JvmField val TRY_BLOCK = BrightScriptElementType("TRY_BLOCK")
}
