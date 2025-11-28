package com.maximpietukhov.brightscript.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.maximpietukhov.brightscript.BrightScriptTokenTypes

class BrightScriptFunctionDefinition(node: ASTNode) : ASTWrapperPsiElement(node), BrightScriptNamedElement {

    override fun getName(): String? {
        return findFunctionName()
    }

    override fun setName(name: String): PsiElement {
        // TODO: Implement rename functionality
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        // Find the FUNCTION_DECLARATION token in the AST node children
        var child = node.firstChildNode
        var foundKeyword = false

        while (child != null) {
            if (!foundKeyword) {
                val text = child.text.lowercase()
                if (text == "function" || text == "sub") {
                    foundKeyword = true
                }
            } else {
                // Find the function name token (FUNCTION_DECLARATION or IDENTIFIER)
                val elementType = child.elementType
                if (elementType == BrightScriptTokenTypes.FUNCTION_DECLARATION ||
                    elementType == BrightScriptTokenTypes.IDENTIFIER) {
                    return child.psi
                }
            }
            child = child.treeNext
        }
        return null
    }

    private fun findFunctionName(): String? {
        // Parse function name from the text directly as fallback
        val text = node.text
        val lines = text.lines()
        if (lines.isEmpty()) return null

        val firstLine = lines[0].trim().lowercase()

        // Extract name from "function name(" or "sub name("
        val regex = Regex("^(function|sub)\\s+(\\w+)", RegexOption.IGNORE_CASE)
        val match = regex.find(text)
        return match?.groupValues?.get(2)
    }

    override fun getTextOffset(): Int {
        return nameIdentifier?.textOffset ?: super.getTextOffset()
    }
}
