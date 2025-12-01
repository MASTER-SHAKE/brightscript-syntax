package com.maximpietukhov.brightscript

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiFile

class BrightScriptEnterHandler : EnterHandlerDelegateAdapter() {

    companion object {
        // Keywords that should trigger auto-completion of closing tag
        private val BLOCK_KEYWORDS = mapOf(
            "function" to "end function",
            "sub" to "end sub",
            "if" to "end if",
            "for" to "end for",
            "while" to "end while",
            "class" to "end class",
            "namespace" to "end namespace",
            "try" to "end try"
        )
    }

    override fun preprocessEnter(
        file: PsiFile,
        editor: Editor,
        caretOffset: Ref<Int>,
        caretAdvance: Ref<Int>,
        dataContext: DataContext,
        originalHandler: EditorActionHandler?
    ): EnterHandlerDelegate.Result {
        // Only handle BrightScript files
        if (file.language != BrightScriptLanguage.INSTANCE) {
            return EnterHandlerDelegate.Result.Continue
        }

        val document = editor.document
        val offset = caretOffset.get()
        val lineNumber = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, lineEnd))

        // Find if there's a block keyword on this line
        val blockKeyword = findBlockKeyword(lineText) ?: return EnterHandlerDelegate.Result.Continue

        // Check if this line already has content after the keyword that suggests it's complete
        // For example: "if x = 1 then return" should not add "end if"
        if (isSingleLineStatement(lineText, blockKeyword)) {
            return EnterHandlerDelegate.Result.Continue
        }

        // Check if there's already a matching closing tag below
        if (hasMatchingClosingTag(document, lineNumber, blockKeyword)) {
            return EnterHandlerDelegate.Result.Continue
        }

        // Generate the closing tag with matching case
        val closingTag = formatEndStatement(blockKeyword, lineText)

        // Calculate indentation from current line
        val indentation = lineText.takeWhile { it.isWhitespace() }

        // Insert newline + indentation + newline + indentation + closing tag
        val insertText = "\n$indentation\n$indentation$closingTag"

        // We'll insert after the current line end
        document.insertString(lineEnd, insertText)

        // Move caret to the middle line (where user will type)
        val newCaretOffset = lineEnd + 1 + indentation.length
        editor.caretModel.moveToOffset(newCaretOffset)

        return EnterHandlerDelegate.Result.Stop
    }

    private fun findBlockKeyword(lineText: String): String? {
        val trimmed = lineText.trim().lowercase()

        // Check for each block keyword at the start of the statement
        for (keyword in BLOCK_KEYWORDS.keys) {
            // Match keyword at start of line (after whitespace)
            if (trimmed.startsWith(keyword)) {
                // Make sure it's a complete keyword, not part of another word
                val afterKeyword = trimmed.substring(keyword.length)
                if (afterKeyword.isEmpty() || !afterKeyword[0].isLetterOrDigit()) {
                    return keyword
                }
            }
        }
        return null
    }

    private fun isSingleLineStatement(lineText: String, keyword: String): Boolean {
        val trimmed = lineText.trim().lowercase()

        // For "if" statements, check if there's code after "then" on the same line
        if (keyword == "if") {
            val thenIndex = trimmed.indexOf(" then ")
            if (thenIndex != -1) {
                val afterThen = trimmed.substring(thenIndex + 6).trim()
                // If there's code after "then", it's a single-line if
                if (afterThen.isNotEmpty() && !afterThen.startsWith("'")) {
                    return true
                }
            }
            // Also check for "then" at end without space (e.g., "if x then")
            // This is NOT a single-line statement, it needs end if
        }

        // Check if line ends with a closing keyword already
        val closingKeywords = listOf("end function", "end sub", "end if", "end for", "end while",
            "endfunction", "endsub", "endif", "endfor", "endwhile", "next",
            "end class", "endclass", "end namespace", "endnamespace", "end try", "endtry")

        for (closing in closingKeywords) {
            if (trimmed.endsWith(closing)) {
                return true
            }
        }

        return false
    }

    private fun formatEndStatement(keyword: String, originalLine: String): String {
        val closingTag = BLOCK_KEYWORDS[keyword] ?: return ""

        // Find the original keyword in the line to determine its case
        val trimmed = originalLine.trimStart()
        val keywordStart = trimmed.lowercase().indexOf(keyword)
        if (keywordStart == -1) return closingTag

        val originalKeyword = trimmed.substring(keywordStart, keywordStart + keyword.length)

        // Determine case style
        return when {
            originalKeyword.all { it.isUpperCase() || !it.isLetter() } -> closingTag.uppercase()
            originalKeyword[0].isUpperCase() -> closingTag.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
            else -> closingTag
        }
    }

    private fun hasMatchingClosingTag(document: Document, startLine: Int, keyword: String): Boolean {
        val totalLines = document.lineCount
        var depth = 1  // We start with 1 because we found an opening keyword

        // Get closing tag variants for this keyword
        val closingVariants = getClosingVariants(keyword)

        // Scan lines below the current line
        for (lineNum in (startLine + 1) until totalLines) {
            val lineStart = document.getLineStartOffset(lineNum)
            val lineEnd = document.getLineEndOffset(lineNum)
            val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStart, lineEnd))
            val trimmed = lineText.trim().lowercase()

            // Skip empty lines and comments
            if (trimmed.isEmpty() || trimmed.startsWith("'") || trimmed.startsWith("rem ")) {
                continue
            }

            // Check for nested opening keyword (same type)
            if (startsWithKeyword(trimmed, keyword)) {
                // Make sure it's not a single-line statement
                if (!isSingleLineStatement(lineText, keyword)) {
                    depth++
                }
            }

            // Check for closing tag
            for (closing in closingVariants) {
                if (trimmed == closing || trimmed.startsWith("$closing ") || trimmed.startsWith("$closing'")) {
                    depth--
                    if (depth == 0) {
                        return true  // Found matching closing tag
                    }
                    break
                }
            }
        }

        return false  // No matching closing tag found
    }

    private fun getClosingVariants(keyword: String): List<String> {
        return when (keyword) {
            "function" -> listOf("end function", "endfunction")
            "sub" -> listOf("end sub", "endsub")
            "if" -> listOf("end if", "endif")
            "for" -> listOf("end for", "endfor", "next")
            "while" -> listOf("end while", "endwhile")
            "class" -> listOf("end class", "endclass")
            "namespace" -> listOf("end namespace", "endnamespace")
            "try" -> listOf("end try", "endtry")
            else -> listOf("end $keyword")
        }
    }

    private fun startsWithKeyword(trimmedLine: String, keyword: String): Boolean {
        if (!trimmedLine.startsWith(keyword)) return false
        if (trimmedLine.length == keyword.length) return true
        val charAfter = trimmedLine[keyword.length]
        return !charAfter.isLetterOrDigit() && charAfter != '_'
    }
}
