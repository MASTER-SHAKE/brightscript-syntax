package com.maximpietukhov.brightscript

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType

/**
 * Annotator for BrightScript syntax error highlighting.
 * This annotator checks for common syntax errors and highlights them.
 */
class BrightScriptAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Only process BrightScript files
        if (element.containingFile?.language != BrightScriptLanguage.INSTANCE) {
            return
        }

        val elementType = element.elementType

        when (elementType) {
            // Highlight bad/unknown characters as errors
            BrightScriptTokenTypes.BAD_CHARACTER -> {
                holder.newAnnotation(HighlightSeverity.ERROR, "Unexpected character")
                    .range(element.textRange)
                    .create()
            }

            // Check for unclosed string literals
            BrightScriptTokenTypes.STRING_LITERAL -> {
                checkUnclosedString(element, holder)
            }
        }

        // Check bracket balance only at file level to avoid multiple checks
        if (element is PsiFile) {
            checkBracketBalance(element, holder)
        }
    }

    /**
     * Check if a string literal is properly closed.
     * An unclosed string will not end with a quote character.
     */
    private fun checkUnclosedString(element: PsiElement, holder: AnnotationHolder) {
        val text = element.text
        if (text.length < 2) {
            // String too short - must be unclosed (just a quote)
            holder.newAnnotation(HighlightSeverity.ERROR, "Unclosed string literal")
                .range(element.textRange)
                .create()
            return
        }

        // Check if string ends with quote
        if (!text.endsWith("\"")) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Unclosed string literal")
                .range(element.textRange)
                .create()
        }
    }

    /**
     * Check for unbalanced brackets in the file.
     * Reports errors for unmatched opening/closing brackets.
     */
    private fun checkBracketBalance(file: PsiFile, holder: AnnotationHolder) {
        val text = file.text

        // Stacks to track bracket positions
        val parenStack = mutableListOf<Int>()      // ()
        val braceStack = mutableListOf<Int>()      // {}
        val bracketStack = mutableListOf<Int>()    // []

        var inString = false
        var inTemplate = false
        var inComment = false
        var i = 0

        while (i < text.length) {
            val char = text[i]

            // Handle string literals - skip content inside strings
            if (char == '"' && !inComment && !inTemplate) {
                inString = !inString
                i++
                continue
            }

            // Handle template strings (backticks, can span lines)
            if (char == '`' && !inComment && !inString) {
                inTemplate = !inTemplate
                i++
                continue
            }

            // Handle comments - skip to end of line
            if (!inString && !inTemplate && char == '\'') {
                inComment = true
            }
            // REM comment at word start
            if (!inString && !inTemplate && !inComment && (char == 'r' || char == 'R')) {
                val wordStart = i == 0 || (!text[i - 1].isLetterOrDigit() && text[i - 1] != '_')
                if (wordStart && i + 3 <= text.length && text.substring(i, i + 3).equals("rem", ignoreCase = true)) {
                    val after = text.getOrNull(i + 3)
                    if (after == null || (!after.isLetterOrDigit() && after != '_')) {
                        inComment = true
                    }
                }
            }
            if (char == '\n') {
                inComment = false
                inString = false
            }

            // Skip if inside string or comment
            if (inString || inTemplate || inComment) {
                i++
                continue
            }

            // Track brackets
            when (char) {
                '(' -> parenStack.add(i)
                ')' -> {
                    if (parenStack.isNotEmpty()) {
                        parenStack.removeAt(parenStack.lastIndex)
                    } else {
                        // Unmatched closing paren
                        holder.newAnnotation(HighlightSeverity.ERROR, "Unmatched ')'")
                            .range(TextRange(i, i + 1))
                            .create()
                    }
                }
                '{' -> braceStack.add(i)
                '}' -> {
                    if (braceStack.isNotEmpty()) {
                        braceStack.removeAt(braceStack.lastIndex)
                    } else {
                        // Unmatched closing brace
                        holder.newAnnotation(HighlightSeverity.ERROR, "Unmatched '}'")
                            .range(TextRange(i, i + 1))
                            .create()
                    }
                }
                '[' -> bracketStack.add(i)
                ']' -> {
                    if (bracketStack.isNotEmpty()) {
                        bracketStack.removeAt(bracketStack.lastIndex)
                    } else {
                        // Unmatched closing bracket
                        holder.newAnnotation(HighlightSeverity.ERROR, "Unmatched ']'")
                            .range(TextRange(i, i + 1))
                            .create()
                    }
                }
            }
            i++
        }

        // Report unclosed opening brackets
        for (pos in parenStack) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Unclosed '('")
                .range(TextRange(pos, pos + 1))
                .create()
        }
        for (pos in braceStack) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Unclosed '{'")
                .range(TextRange(pos, pos + 1))
                .create()
        }
        for (pos in bracketStack) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Unclosed '['")
                .range(TextRange(pos, pos + 1))
                .create()
        }
    }
}
