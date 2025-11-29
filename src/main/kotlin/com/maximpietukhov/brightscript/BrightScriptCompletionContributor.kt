package com.maximpietukhov.brightscript

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

class BrightScriptCompletionContributor : CompletionContributor() {

    companion object {
        // Keywords for code completion
        private val KEYWORDS = listOf(
            "if", "then", "else", "elseif", "end if",
            "for", "to", "step", "next", "each", "end for",
            "while", "end while",
            "function", "end function", "sub", "end sub",
            "return", "exit", "goto", "stop",
            "and", "or", "not", "mod",
            "as", "in", "dim", "print",
            "class", "end class", "namespace", "end namespace",
            "interface", "implements", "extends",
            "public", "private", "protected", "override",
            "new", "import", "library",
            "try", "catch", "end try", "finally", "throw",
            "continue", "const", "enum", "end enum",
            "true", "false", "invalid"
        )

        // Type keywords for completion after "as"
        private val TYPE_KEYWORDS = listOf(
            "Boolean", "Integer", "LongInteger", "Float", "Double",
            "String", "Object", "Function", "Dynamic", "Void"
        )

        // Built-in functions
        private val BUILTIN_FUNCTIONS = listOf(
            // Math
            "Abs", "Atn", "Cdbl", "Cint", "Cos", "Csng", "Exp", "Fix", "Int", "Log", "Rnd", "Sgn", "Sin", "Sqr", "Tan",
            // Runtime
            "CreateObject", "Type", "GetGlobalAA", "Box", "Run", "Eval",
            "GetLastRunCompileError", "GetLastRunRuntimeError",
            // Utility
            "Sleep", "Wait", "GetInterface", "FindMemberFunction", "UpTime", "RebootSystem", "ListDir",
            "ReadAsciiFile", "WriteAsciiFile", "CopyFile", "MoveFile", "MatchFiles", "DeleteFile",
            "DeleteDirectory", "CreateDirectory", "FormatDrive", "StrToI", "RunGarbageCollector",
            "ParseJson", "FormatJson", "Tr",
            // String
            "UCase", "LCase", "Asc", "Chr", "Instr", "Left", "Len", "Mid", "Right", "Str", "StrI",
            "String", "StringI", "Val", "Substitute"
        )
    }

    init {
        // Keywords completion - at the start of statements
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(BrightScriptLanguage.INSTANCE),
            KeywordCompletionProvider()
        )
    }

    private class KeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val position = parameters.position
            val text = position.containingFile?.text ?: return
            val offset = parameters.offset

            // Check if we're after "as" keyword for type completion
            if (isAfterAsKeyword(text, offset)) {
                addTypeCompletions(result)
                return
            }

            // Add keyword completions
            addKeywordCompletions(result)

            // Add built-in function completions
            addBuiltinFunctionCompletions(result)
        }

        private fun isAfterAsKeyword(text: String, offset: Int): Boolean {
            // Look backwards to find if "as" precedes current position
            var pos = offset - 1

            // Skip current identifier being typed
            while (pos >= 0 && (text[pos].isLetterOrDigit() || text[pos] == '_')) {
                pos--
            }

            // Skip whitespace
            while (pos >= 0 && text[pos].isWhitespace()) {
                pos--
            }

            // Check for "as" keyword (case-insensitive)
            if (pos >= 1) {
                val potentialAs = text.substring(maxOf(0, pos - 1), pos + 1).lowercase()
                if (potentialAs == "as") {
                    // Make sure it's a complete "as" keyword, not part of another word
                    val beforeAs = if (pos >= 2) text[pos - 2] else ' '
                    if (!beforeAs.isLetterOrDigit() && beforeAs != '_') {
                        return true
                    }
                }
            }

            return false
        }

        private fun addKeywordCompletions(result: CompletionResultSet) {
            for (keyword in KEYWORDS) {
                // Add both lowercase and capitalized versions
                result.addElement(
                    LookupElementBuilder.create(keyword)
                        .withTypeText("keyword")
                        .bold()
                )
                if (keyword[0].isLowerCase()) {
                    val capitalized = keyword.split(" ").joinToString(" ") {
                        it.replaceFirstChar { c -> c.uppercase() }
                    }
                    result.addElement(
                        LookupElementBuilder.create(capitalized)
                            .withTypeText("keyword")
                            .bold()
                    )
                }
            }
        }

        private fun addTypeCompletions(result: CompletionResultSet) {
            for (type in TYPE_KEYWORDS) {
                result.addElement(
                    LookupElementBuilder.create(type)
                        .withTypeText("type")
                        .bold()
                )
                // Also add lowercase version
                result.addElement(
                    LookupElementBuilder.create(type.lowercase())
                        .withTypeText("type")
                        .bold()
                )
            }
        }

        private fun addBuiltinFunctionCompletions(result: CompletionResultSet) {
            for (func in BUILTIN_FUNCTIONS) {
                result.addElement(
                    LookupElementBuilder.create(func)
                        .withTypeText("built-in")
                        .withTailText("()", true)
                        .withInsertHandler { ctx, _ ->
                            val editor = ctx.editor
                            val document = editor.document
                            val tailOffset = ctx.tailOffset

                            // Check if there's already a '(' after
                            val hasParens = tailOffset < document.textLength &&
                                           document.getText(com.intellij.openapi.util.TextRange(tailOffset, tailOffset + 1)) == "("

                            if (!hasParens) {
                                document.insertString(tailOffset, "()")
                                editor.caretModel.moveToOffset(tailOffset + 1)
                            }
                        }
                )
            }
        }
    }
}
