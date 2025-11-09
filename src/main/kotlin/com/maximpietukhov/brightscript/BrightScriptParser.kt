package com.maximpietukhov.brightscript

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

class BrightScriptParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val marker = builder.mark()

        while (!builder.eof()) {
            parseStatement(builder)
        }

        marker.done(root)
        return builder.treeBuilt
    }

    private fun parseStatement(builder: PsiBuilder) {
        val tokenText = builder.tokenText?.lowercase()

        when {
            tokenText == "function" -> parseFunctionBlock(builder)
            tokenText == "sub" -> parseSubBlock(builder)
            tokenText == "if" -> parseIfBlock(builder)
            tokenText == "for" -> parseForBlock(builder)
            tokenText == "while" -> parseWhileBlock(builder)
            tokenText == "class" -> parseClassBlock(builder)
            tokenText == "namespace" -> parseNamespaceBlock(builder)
            tokenText == "try" -> parseTryBlock(builder)
            else -> builder.advanceLexer()
        }
    }

    private fun parseFunctionBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'function'

        // Skip function signature until newline or start of body
        while (!builder.eof()) {
            val text = builder.tokenText?.lowercase()
            if (text in listOf("if", "for", "while", "sub", "function", "class", "try", "end", "endfunction")) {
                break
            }
            if (builder.tokenType == com.intellij.psi.TokenType.WHITE_SPACE &&
                builder.tokenText?.contains('\n') == true) {
                builder.advanceLexer()
                break
            }
            builder.advanceLexer()
        }

        // Parse function body until "end function" or "endfunction"
        while (!builder.eof()) {
            val text = builder.tokenText?.lowercase()
            when {
                text == "function" -> parseFunctionBlock(builder)
                text == "sub" -> parseSubBlock(builder)
                text == "if" -> parseIfBlock(builder)
                text == "for" -> parseForBlock(builder)
                text == "while" -> parseWhileBlock(builder)
                text == "class" -> parseClassBlock(builder)
                text == "try" -> parseTryBlock(builder)
                text == "namespace" -> parseNamespaceBlock(builder)
                text == "end" -> {
                    builder.advanceLexer() // consume 'end'
                    if (builder.tokenText?.lowercase() == "function") {
                        builder.advanceLexer() // consume 'function'
                        break // Exit - block ended
                    } else {
                        // Not our closing tag, skip both 'end' and the next token
                        builder.advanceLexer()
                    }
                }
                text == "endfunction" -> {
                    builder.advanceLexer()
                    break // Exit - block ended
                }
                else -> builder.advanceLexer()
            }
        }

        marker.done(BrightScriptElementTypes.FUNCTION_BLOCK)
    }

    private fun parseSubBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'sub'

        // Skip sub signature until newline or start of body
        while (!builder.eof()) {
            val text = builder.tokenText?.lowercase()
            if (text in listOf("if", "for", "while", "sub", "function", "class", "try", "end", "endsub")) {
                break
            }
            if (builder.tokenType == com.intellij.psi.TokenType.WHITE_SPACE &&
                builder.tokenText?.contains('\n') == true) {
                builder.advanceLexer()
                break
            }
            builder.advanceLexer()
        }

        // Parse sub body until "end sub" or "endsub"
        while (!builder.eof()) {
            val text = builder.tokenText?.lowercase()
            when {
                text == "function" -> parseFunctionBlock(builder)
                text == "sub" -> parseSubBlock(builder)
                text == "if" -> parseIfBlock(builder)
                text == "for" -> parseForBlock(builder)
                text == "while" -> parseWhileBlock(builder)
                text == "class" -> parseClassBlock(builder)
                text == "try" -> parseTryBlock(builder)
                text == "namespace" -> parseNamespaceBlock(builder)
                text == "end" -> {
                    builder.advanceLexer() // consume 'end'
                    if (builder.tokenText?.lowercase() == "sub") {
                        builder.advanceLexer() // consume 'sub'
                        break // Exit - block ended
                    } else {
                        // Not our closing tag, skip both 'end' and the next token
                        builder.advanceLexer()
                    }
                }
                text == "endsub" -> {
                    builder.advanceLexer()
                    break // Exit - block ended
                }
                else -> builder.advanceLexer()
            }
        }

        marker.done(BrightScriptElementTypes.SUB_BLOCK)
    }

    private fun parseIfBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'if'

        // Skip until 'then' or newline - 'then' is optional in BrightScript if followed by newline
        while (!builder.eof()) {
            val currentText = builder.tokenText?.lowercase()

            // Check if we hit block start keywords - means no 'then' and likely invalid
            if (currentText in listOf("if", "for", "while", "sub", "function", "class", "try", "end", "endif")) {
                break
            }

            // Found 'then' - consume it and start parsing body
            if (currentText == "then") {
                builder.advanceLexer() // consume 'then'
                break
            }

            // Check for newline - in BrightScript, newline can replace 'then'
            if (builder.tokenType == com.intellij.psi.TokenType.WHITE_SPACE &&
                builder.tokenText?.contains('\n') == true) {
                builder.advanceLexer() // consume newline
                break
            }

            builder.advanceLexer()
        }

        // Parse the if body until "end if" or "endif"
        while (!builder.eof()) {
            val text = builder.tokenText?.lowercase()
            when {
                text == "if" -> parseIfBlock(builder) // Recursively parse nested if
                text == "for" -> parseForBlock(builder)
                text == "while" -> parseWhileBlock(builder)
                text == "function" -> parseFunctionBlock(builder)
                text == "sub" -> parseSubBlock(builder)
                text == "class" -> parseClassBlock(builder)
                text == "try" -> parseTryBlock(builder)
                text == "namespace" -> parseNamespaceBlock(builder)
                text in listOf("else", "elseif", "elsif") -> {
                    // else/elseif/elsif are part of this if block, just skip them
                    builder.advanceLexer()
                    // If this is 'else if' (two words), skip the condition part until newline or 'then'
                    if (text == "else" && builder.tokenText?.lowercase() == "if") {
                        builder.advanceLexer() // skip 'if'
                        // Skip condition until 'then' or newline
                        while (!builder.eof()) {
                            val condText = builder.tokenText?.lowercase()

                            // Check if we hit block start keywords - means no 'then' and likely end of condition
                            if (condText in listOf("if", "for", "while", "sub", "function", "class", "try", "end", "endif", "else")) {
                                break
                            }

                            if (condText == "then") {
                                builder.advanceLexer()
                                break
                            }
                            if (builder.tokenType == com.intellij.psi.TokenType.WHITE_SPACE &&
                                builder.tokenText?.contains('\n') == true) {
                                builder.advanceLexer()
                                break
                            }
                            builder.advanceLexer()
                        }
                    } else if (text in listOf("elseif", "elsif")) {
                        // Skip condition for elseif/elsif until 'then' or newline
                        while (!builder.eof()) {
                            val condText = builder.tokenText?.lowercase()

                            // Check if we hit block start keywords - means no 'then' and likely end of condition
                            if (condText in listOf("if", "for", "while", "sub", "function", "class", "try", "end", "endif", "else")) {
                                break
                            }

                            if (condText == "then") {
                                builder.advanceLexer()
                                break
                            }
                            if (builder.tokenType == com.intellij.psi.TokenType.WHITE_SPACE &&
                                builder.tokenText?.contains('\n') == true) {
                                builder.advanceLexer()
                                break
                            }
                            builder.advanceLexer()
                        }
                    }
                }
                text == "end" -> {
                    val endMarker = builder.mark()
                    builder.advanceLexer() // consume 'end'
                    if (builder.tokenText?.lowercase() == "if") {
                        endMarker.drop() // Keep the consumed tokens
                        builder.advanceLexer() // consume 'if'
                        break // Exit - block ended
                    } else {
                        // Not our closing tag - this is closing parent block (e.g., end function, end sub)
                        endMarker.rollbackTo() // Rollback, don't consume 'end'
                        break // Exit - parent block is ending
                    }
                }
                text == "endif" -> {
                    builder.advanceLexer()
                    break // Exit - block ended
                }
                text in listOf("endfunction", "endsub", "endfor", "endwhile", "endclass", "endnamespace", "endtry") -> {
                    // Parent block is ending, exit without consuming
                    break
                }
                else -> builder.advanceLexer()
            }
        }

        marker.done(BrightScriptElementTypes.IF_BLOCK)
    }

    private fun parseForBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'for'

        // Parse until "end for", "endfor", or "next"
        while (!builder.eof()) {
            val text = builder.tokenText?.lowercase()
            when {
                text == "for" -> parseForBlock(builder) // Recursively parse nested for
                text == "if" -> parseIfBlock(builder)
                text == "while" -> parseWhileBlock(builder)
                text == "function" -> parseFunctionBlock(builder)
                text == "sub" -> parseSubBlock(builder)
                text == "class" -> parseClassBlock(builder)
                text == "try" -> parseTryBlock(builder)
                text == "namespace" -> parseNamespaceBlock(builder)
                text == "end" -> {
                    val endMarker = builder.mark()
                    builder.advanceLexer() // consume 'end'
                    if (builder.tokenText?.lowercase() == "for") {
                        endMarker.drop() // Keep the consumed tokens
                        builder.advanceLexer() // consume 'for'
                        break // Exit - block ended
                    } else {
                        // Not our closing tag - this is closing parent block
                        endMarker.rollbackTo() // Rollback, don't consume 'end'
                        break // Exit - parent block is ending
                    }
                }
                text in listOf("endfor", "next") -> {
                    builder.advanceLexer()
                    break // Exit - block ended
                }
                text in listOf("endfunction", "endsub", "endif", "endwhile", "endclass", "endnamespace", "endtry") -> {
                    // Parent block is ending, exit without consuming
                    break
                }
                else -> builder.advanceLexer()
            }
        }

        marker.done(BrightScriptElementTypes.FOR_BLOCK)
    }

    private fun parseWhileBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'while'

        // Parse until "end while" or "endwhile"
        while (!builder.eof()) {
            val text = builder.tokenText?.lowercase()
            when {
                text == "while" -> parseWhileBlock(builder) // Recursively parse nested while
                text == "if" -> parseIfBlock(builder)
                text == "for" -> parseForBlock(builder)
                text == "function" -> parseFunctionBlock(builder)
                text == "sub" -> parseSubBlock(builder)
                text == "class" -> parseClassBlock(builder)
                text == "try" -> parseTryBlock(builder)
                text == "namespace" -> parseNamespaceBlock(builder)
                text == "end" -> {
                    val endMarker = builder.mark()
                    builder.advanceLexer() // consume 'end'
                    if (builder.tokenText?.lowercase() == "while") {
                        endMarker.drop() // Keep the consumed tokens
                        builder.advanceLexer() // consume 'while'
                        break // Exit - block ended
                    } else {
                        // Not our closing tag - this is closing parent block
                        endMarker.rollbackTo() // Rollback, don't consume 'end'
                        break // Exit - parent block is ending
                    }
                }
                text == "endwhile" -> {
                    builder.advanceLexer()
                    break // Exit - block ended
                }
                text in listOf("endfunction", "endsub", "endif", "endfor", "endclass", "endnamespace", "endtry", "next") -> {
                    // Parent block is ending, exit without consuming
                    break
                }
                else -> builder.advanceLexer()
            }
        }

        marker.done(BrightScriptElementTypes.WHILE_BLOCK)
    }

    private fun parseClassBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'class'

        // Parse until "end class" or "endclass"
        while (!builder.eof()) {
            val text = builder.tokenText?.lowercase()
            when {
                text == "class" -> parseClassBlock(builder) // Recursively parse nested class
                text == "function" -> parseFunctionBlock(builder)
                text == "sub" -> parseSubBlock(builder)
                text == "if" -> parseIfBlock(builder)
                text == "for" -> parseForBlock(builder)
                text == "while" -> parseWhileBlock(builder)
                text == "try" -> parseTryBlock(builder)
                text == "namespace" -> parseNamespaceBlock(builder)
                text == "end" -> {
                    builder.advanceLexer() // consume 'end'
                    if (builder.tokenText?.lowercase() == "class") {
                        builder.advanceLexer() // consume 'class'
                        break // Exit - block ended
                    } else {
                        // Not our closing tag, skip both 'end' and the next token
                        builder.advanceLexer()
                    }
                }
                text == "endclass" -> {
                    builder.advanceLexer()
                    break // Exit - block ended
                }
                else -> builder.advanceLexer()
            }
        }

        marker.done(BrightScriptElementTypes.CLASS_BLOCK)
    }

    private fun parseNamespaceBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'namespace'

        // Parse until "end namespace" or "endnamespace"
        while (!builder.eof()) {
            val text = builder.tokenText?.lowercase()
            when {
                text == "namespace" -> parseNamespaceBlock(builder) // Recursively parse nested namespace
                text == "function" -> parseFunctionBlock(builder)
                text == "sub" -> parseSubBlock(builder)
                text == "class" -> parseClassBlock(builder)
                text == "if" -> parseIfBlock(builder)
                text == "for" -> parseForBlock(builder)
                text == "while" -> parseWhileBlock(builder)
                text == "try" -> parseTryBlock(builder)
                text == "end" -> {
                    builder.advanceLexer() // consume 'end'
                    if (builder.tokenText?.lowercase() == "namespace") {
                        builder.advanceLexer() // consume 'namespace'
                        break // Exit - block ended
                    } else {
                        // Not our closing tag, skip both 'end' and the next token
                        builder.advanceLexer()
                    }
                }
                text == "endnamespace" -> {
                    builder.advanceLexer()
                    break // Exit - block ended
                }
                else -> builder.advanceLexer()
            }
        }

        marker.done(BrightScriptElementTypes.NAMESPACE_BLOCK)
    }

    private fun parseTryBlock(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume 'try'

        // Parse until "end try" or "endtry"
        while (!builder.eof()) {
            val text = builder.tokenText?.lowercase()
            when {
                text == "try" -> parseTryBlock(builder) // Recursively parse nested try
                text == "function" -> parseFunctionBlock(builder)
                text == "sub" -> parseSubBlock(builder)
                text == "class" -> parseClassBlock(builder)
                text == "if" -> parseIfBlock(builder)
                text == "for" -> parseForBlock(builder)
                text == "while" -> parseWhileBlock(builder)
                text == "namespace" -> parseNamespaceBlock(builder)
                text == "end" -> {
                    builder.advanceLexer() // consume 'end'
                    if (builder.tokenText?.lowercase() == "try") {
                        builder.advanceLexer() // consume 'try'
                        break // Exit - block ended
                    } else {
                        // Not our closing tag, skip both 'end' and the next token
                        builder.advanceLexer()
                    }
                }
                text == "endtry" -> {
                    builder.advanceLexer()
                    break // Exit - block ended
                }
                else -> builder.advanceLexer()
            }
        }

        marker.done(BrightScriptElementTypes.TRY_BLOCK)
    }
}
