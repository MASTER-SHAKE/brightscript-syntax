package com.maximpietukhov.brightscript

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock

class BrightScriptBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val settings: CodeStyleSettings
) : AbstractBlock(node, wrap, alignment) {

    companion object {
        // Block types that should have their contents indented
        private val INDENT_BLOCKS = setOf(
            BrightScriptElementTypes.FUNCTION_BLOCK,
            BrightScriptElementTypes.SUB_BLOCK,
            BrightScriptElementTypes.IF_BLOCK,
            BrightScriptElementTypes.FOR_BLOCK,
            BrightScriptElementTypes.WHILE_BLOCK,
            BrightScriptElementTypes.CLASS_BLOCK,
            BrightScriptElementTypes.NAMESPACE_BLOCK,
            BrightScriptElementTypes.TRY_BLOCK
        )

        // Keywords that should stay at block level (not indented within their own block)
        private val BLOCK_BOUNDARY_KEYWORDS = setOf(
            "function", "sub", "if", "for", "while", "class", "namespace", "try",
            "else", "elseif", "elsif", "catch", "finally",
            "end", "endif", "endfunction", "endsub", "endfor", "endwhile",
            "endclass", "endnamespace", "endtry", "next", "then"
        )
    }

    override fun buildChildren(): List<Block> {
        val blocks = mutableListOf<Block>()
        var child = myNode.firstChildNode

        while (child != null) {
            if (child.elementType != TokenType.WHITE_SPACE) {
                blocks.add(
                    BrightScriptBlock(
                        child,
                        Wrap.createWrap(WrapType.NONE, false),
                        null,
                        settings
                    )
                )
            }
            child = child.treeNext
        }

        return blocks
    }

    override fun getIndent(): Indent? {
        val elementType = myNode.elementType
        val parentType = myNode.treeParent?.elementType

        // Root level - no indent
        if (parentType == null) {
            return Indent.getNoneIndent()
        }

        // Check if parent is a block that should indent its children
        if (parentType in INDENT_BLOCKS) {
            // Nested blocks (if inside function, for inside if, etc.) should be indented
            if (elementType in INDENT_BLOCKS) {
                return Indent.getNormalIndent()
            }

            // For leaf tokens, check if it's a block boundary keyword
            val text = myNode.text.lowercase().trim()
            val firstWord = text.split(Regex("\\s+")).firstOrNull() ?: ""

            // Block boundary keywords stay at block level
            if (firstWord in BLOCK_BOUNDARY_KEYWORDS) {
                return Indent.getNoneIndent()
            }

            // Everything else inside blocks should be indented
            return Indent.getNormalIndent()
        }

        return Indent.getNoneIndent()
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return null // Use default spacing
    }

    override fun isLeaf(): Boolean {
        return myNode.firstChildNode == null
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        val elementType = myNode.elementType

        // Inside blocks, new children should be indented
        if (elementType in INDENT_BLOCKS) {
            return ChildAttributes(Indent.getNormalIndent(), null)
        }

        return ChildAttributes(Indent.getNoneIndent(), null)
    }
}
