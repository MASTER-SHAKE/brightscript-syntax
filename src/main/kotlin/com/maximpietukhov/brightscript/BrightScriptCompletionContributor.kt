package com.maximpietukhov.brightscript

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiFile
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

        // Keywords and built-ins in lowercase for filtering
        private val RESERVED_WORDS = (KEYWORDS + BUILTIN_FUNCTIONS.map { it.lowercase() } + TYPE_KEYWORDS.map { it.lowercase() }).toSet()
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
            val file = position.containingFile ?: return
            val text = file.text ?: return
            val offset = parameters.offset

            // Check if we're after "as" keyword for type completion
            if (isAfterAsKeyword(text, offset)) {
                addTypeCompletions(result)
                return
            }

            // Check if we're after a dot (member access) and get the prefix chain
            val memberAccessPrefix = getMemberAccessPrefix(text, offset)

            if (memberAccessPrefix != null) {
                // After dot - suggest only members for this specific prefix
                addMemberCompletions(file, result, memberAccessPrefix)
            } else {
                // Normal context - suggest everything
                addKeywordCompletions(result)
                addBuiltinFunctionCompletions(result)
                addIdentifierCompletions(file, result)
            }
        }

        /**
         * Get the prefix chain before the dot, e.g., for "m.selectProfile.|" returns "m.selectProfile"
         * Returns null if not after a dot
         */
        private fun getMemberAccessPrefix(text: String, offset: Int): String? {
            var pos = offset - 1

            // Skip current identifier being typed (after the dot)
            while (pos >= 0 && (text[pos].isLetterOrDigit() || text[pos] == '_')) {
                pos--
            }

            // Skip whitespace
            while (pos >= 0 && text[pos].isWhitespace() && text[pos] != '\n') {
                pos--
            }

            // Check if we're after a dot
            if (pos < 0 || text[pos] != '.') {
                return null
            }

            // Now extract the full chain before the dot (e.g., "m.selectProfile" or just "m")
            val dotPos = pos
            pos--

            // Skip whitespace before the dot
            while (pos >= 0 && text[pos].isWhitespace() && text[pos] != '\n') {
                pos--
            }

            if (pos < 0) return null

            // Find the start of the chain (go back through identifiers and dots)
            val chainEnd = pos + 1
            while (pos >= 0) {
                val c = text[pos]
                if (c.isLetterOrDigit() || c == '_' || c == '.') {
                    pos--
                } else {
                    break
                }
            }
            val chainStart = pos + 1

            if (chainStart >= chainEnd) return null

            val chain = text.substring(chainStart, chainEnd).trim()

            // Validate chain - should not start or end with dot, should not have double dots
            if (chain.isEmpty() || chain.startsWith(".") || chain.endsWith(".") || chain.contains("..")) {
                return null
            }

            return chain
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

        /**
         * Add member completions based on the prefix chain.
         * For prefix "m.selectProfile", finds all "m.selectProfile.XXX" patterns and suggests XXX
         */
        private fun addMemberCompletions(file: PsiFile, result: CompletionResultSet, prefix: String) {
            val text = file.text ?: return
            val members = mutableSetOf<String>()

            // Escape the prefix for regex (dots need escaping)
            val escapedPrefix = Regex.escape(prefix)

            // Find all patterns like "prefix.member" where prefix matches exactly
            // The pattern: prefix followed by dot followed by identifier
            val memberRegex = Regex("\\b$escapedPrefix\\.([a-zA-Z_][a-zA-Z0-9_]*)\\b", RegexOption.IGNORE_CASE)

            memberRegex.findAll(text).forEach { match ->
                val memberName = match.groupValues[1]
                if (memberName.isNotEmpty()) {
                    members.add(memberName)
                }
            }

            // Add found members
            for (member in members.sorted()) {
                result.addElement(
                    LookupElementBuilder.create(member)
                        .withTypeText(prefix)
                )
            }

            // If no specific members found for this prefix, fall back to showing common patterns
            // This helps when user is typing a new member access
            if (members.isEmpty()) {
                // Try to find members of the base object (first part of chain)
                val baseParts = prefix.split(".")
                if (baseParts.size > 1) {
                    // Try parent chain (e.g., for "m.selectProfile" try "m")
                    val parentPrefix = baseParts.dropLast(1).joinToString(".")
                    val parentRegex = Regex("\\b${Regex.escape(parentPrefix)}\\.([a-zA-Z_][a-zA-Z0-9_]*)\\b", RegexOption.IGNORE_CASE)

                    parentRegex.findAll(text).forEach { match ->
                        val memberName = match.groupValues[1]
                        if (memberName.isNotEmpty() && memberName.lowercase() != baseParts.last().lowercase()) {
                            members.add(memberName)
                        }
                    }

                    for (member in members.sorted()) {
                        result.addElement(
                            LookupElementBuilder.create(member)
                                .withTypeText("$parentPrefix.*")
                        )
                    }
                }
            }
        }

        /**
         * Add identifier completions (not after dot)
         */
        private fun addIdentifierCompletions(file: PsiFile, result: CompletionResultSet) {
            val text = file.text ?: return
            val identifiers = mutableSetOf<String>()

            // Simple regex-based extraction of identifiers
            val identifierRegex = Regex("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b")

            // Collect all identifiers
            identifierRegex.findAll(text).forEach { match ->
                val identifier = match.groupValues[1]
                // Filter out reserved words and short identifiers
                if (identifier.length >= 2 &&
                    identifier.lowercase() !in RESERVED_WORDS &&
                    !identifier.all { it.isDigit() }) {
                    identifiers.add(identifier)
                }
            }

            // Add identifiers
            for (identifier in identifiers.sorted()) {
                result.addElement(
                    LookupElementBuilder.create(identifier)
                        .withTypeText("identifier")
                )
            }
        }
    }
}
