package com.maximpietukhov.brightscript

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class BrightScriptFoldingBuilder : FoldingBuilderEx() {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        // Find all block elements in the PSI tree
        PsiTreeUtil.findChildrenOfAnyType(
            root,
            BrightScriptPsiElement::class.java
        ).forEach { element ->
            val elementType = element.node.elementType

            when (elementType) {
                BrightScriptElementTypes.FUNCTION_BLOCK,
                BrightScriptElementTypes.SUB_BLOCK,
                BrightScriptElementTypes.FOR_BLOCK,
                BrightScriptElementTypes.WHILE_BLOCK,
                BrightScriptElementTypes.CLASS_BLOCK,
                BrightScriptElementTypes.NAMESPACE_BLOCK,
                BrightScriptElementTypes.TRY_BLOCK -> {
                    val range = element.textRange
                    if (range.length > 0 && isMultiLine(element, document)) {
                        descriptors.add(FoldingDescriptor(element.node, range))
                    }
                }
                BrightScriptElementTypes.IF_BLOCK -> {
                    // For if blocks, only fold if multi-line
                    val range = element.textRange
                    if (range.length > 0 && isMultiLine(element, document)) {
                        descriptors.add(FoldingDescriptor(element.node, range))
                    }
                }
            }
        }

        return descriptors.toTypedArray()
    }

    private fun isMultiLine(element: PsiElement, document: Document): Boolean {
        val range = element.textRange
        val startLine = document.getLineNumber(range.startOffset)
        val endLine = document.getLineNumber(range.endOffset)
        return endLine > startLine
    }

    override fun getPlaceholderText(node: ASTNode): String {
        val text = node.text
        val firstLine = text.lines().firstOrNull()?.trim() ?: ""

        return when (node.elementType) {
            BrightScriptElementTypes.FUNCTION_BLOCK -> {
                extractFunctionName(firstLine, "function") ?: "function..."
            }
            BrightScriptElementTypes.SUB_BLOCK -> {
                extractFunctionName(firstLine, "sub") ?: "sub..."
            }
            BrightScriptElementTypes.CLASS_BLOCK -> {
                extractClassName(firstLine) ?: "class..."
            }
            BrightScriptElementTypes.NAMESPACE_BLOCK -> {
                extractNamespaceName(firstLine) ?: "namespace..."
            }
            BrightScriptElementTypes.IF_BLOCK -> {
                val condition = extractIfCondition(firstLine)
                if (condition != null) "if $condition..." else "if..."
            }
            BrightScriptElementTypes.FOR_BLOCK -> {
                val variable = extractForVariable(firstLine)
                if (variable != null) "for $variable..." else "for..."
            }
            BrightScriptElementTypes.WHILE_BLOCK -> {
                val condition = extractWhileCondition(firstLine)
                if (condition != null) "while $condition..." else "while..."
            }
            BrightScriptElementTypes.TRY_BLOCK -> "try..."
            else -> "..."
        }
    }

    private fun extractFunctionName(line: String, keyword: String): String? {
        // Match: function functionName() or sub subName(param1, param2)
        val regex = Regex("\\b$keyword\\s+(\\w+)\\s*\\(", RegexOption.IGNORE_CASE)
        val match = regex.find(line) ?: return null
        val name = match.groupValues[1]

        // Extract parameters up to closing paren
        val afterName = line.substringAfter("$name(")
        val params = afterName.substringBefore(")").trim()

        return if (params.isEmpty()) {
            "$keyword $name()..."
        } else {
            // Shorten if too long
            val shortParams = if (params.length > 20) params.take(20) + "..." else params
            "$keyword $name($shortParams)..."
        }
    }

    private fun extractClassName(line: String): String? {
        // Match: class ClassName
        val regex = Regex("\\bclass\\s+(\\w+)", RegexOption.IGNORE_CASE)
        val match = regex.find(line) ?: return null
        val name = match.groupValues[1]
        return "class $name..."
    }

    private fun extractNamespaceName(line: String): String? {
        // Match: namespace NamespaceName
        val regex = Regex("\\bnamespace\\s+([\\w.]+)", RegexOption.IGNORE_CASE)
        val match = regex.find(line) ?: return null
        val name = match.groupValues[1]
        return "namespace $name..."
    }

    private fun extractIfCondition(line: String): String? {
        // Match: if condition then
        val regex = Regex("\\bif\\s+(.+?)\\s+then", RegexOption.IGNORE_CASE)
        val match = regex.find(line) ?: return null
        val condition = match.groupValues[1].trim()

        // Shorten if too long
        return if (condition.length > 30) {
            condition.take(30) + "..."
        } else {
            condition
        }
    }

    private fun extractForVariable(line: String): String? {
        // Match: for i = 0 to 10 or for each item in array
        val eachRegex = Regex("\\bfor\\s+each\\s+(\\w+)", RegexOption.IGNORE_CASE)
        val eachMatch = eachRegex.find(line)
        if (eachMatch != null) {
            return "each ${eachMatch.groupValues[1]}"
        }

        val regularRegex = Regex("\\bfor\\s+(\\w+)\\s*=", RegexOption.IGNORE_CASE)
        val regularMatch = regularRegex.find(line)
        if (regularMatch != null) {
            return regularMatch.groupValues[1]
        }

        return null
    }

    private fun extractWhileCondition(line: String): String? {
        // Match: while condition
        val regex = Regex("\\bwhile\\s+(.+)", RegexOption.IGNORE_CASE)
        val match = regex.find(line) ?: return null
        val condition = match.groupValues[1].trim()

        // Shorten if too long
        return if (condition.length > 30) {
            condition.take(30) + "..."
        } else {
            condition
        }
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}
