package com.maximpietukhov.brightscript

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class BrightScriptSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        val KEYWORD = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
        )

        val STRING = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_STRING",
            DefaultLanguageHighlighterColors.STRING
        )

        val NUMBER = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER
        )

        val COMMENT = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )

        val REM_COMMENT = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_REM_COMMENT",
            DefaultLanguageHighlighterColors.METADATA
        )

        val OPERATOR = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_OPERATOR",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )

        val IDENTIFIER = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_IDENTIFIER",
            DefaultLanguageHighlighterColors.IDENTIFIER
        )

        val PARENTHESES = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_PARENTHESES",
            DefaultLanguageHighlighterColors.PARENTHESES
        )

        val BRACES = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_BRACES",
            DefaultLanguageHighlighterColors.BRACES
        )

        val BRACKETS = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_BRACKETS",
            DefaultLanguageHighlighterColors.BRACKETS
        )

        val DOT = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_DOT",
            DefaultLanguageHighlighterColors.DOT
        )

        val COMMA = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_COMMA",
            DefaultLanguageHighlighterColors.COMMA
        )

        val BUILTIN_FUNCTION = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_BUILTIN_FUNCTION",
            DefaultLanguageHighlighterColors.STATIC_METHOD
        )

        val TYPE_KEYWORD = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_TYPE_KEYWORD",
            DefaultLanguageHighlighterColors.CLASS_NAME
        )

        val FUNCTION_DECLARATION = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_FUNCTION_DECLARATION",
            DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
        )

        val FUNCTION_CALL = TextAttributesKey.createTextAttributesKey(
            "BRIGHTSCRIPT_FUNCTION_CALL",
            DefaultLanguageHighlighterColors.STATIC_METHOD
        )

        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val TYPE_KEYWORD_KEYS = arrayOf(TYPE_KEYWORD)
        private val STRING_KEYS = arrayOf(STRING)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val COMMENT_KEYS = arrayOf(COMMENT)
        private val REM_COMMENT_KEYS = arrayOf(REM_COMMENT)
        private val OPERATOR_KEYS = arrayOf(OPERATOR)
        private val IDENTIFIER_KEYS = arrayOf(IDENTIFIER)
        private val PARENTHESES_KEYS = arrayOf(PARENTHESES)
        private val BRACES_KEYS = arrayOf(BRACES)
        private val BRACKETS_KEYS = arrayOf(BRACKETS)
        private val DOT_KEYS = arrayOf(DOT)
        private val COMMA_KEYS = arrayOf(COMMA)
        private val BUILTIN_FUNCTION_KEYS = arrayOf(BUILTIN_FUNCTION)
        private val FUNCTION_DECLARATION_KEYS = arrayOf(FUNCTION_DECLARATION)
        private val FUNCTION_CALL_KEYS = arrayOf(FUNCTION_CALL)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }

    override fun getHighlightingLexer(): Lexer = BrightScriptLexer()

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        return when (tokenType) {
            BrightScriptTokenTypes.KEYWORD -> KEYWORD_KEYS
            BrightScriptTokenTypes.TYPE_KEYWORD -> TYPE_KEYWORD_KEYS
            BrightScriptTokenTypes.STRING_LITERAL -> STRING_KEYS
            BrightScriptTokenTypes.NUMBER_LITERAL -> NUMBER_KEYS
            BrightScriptTokenTypes.BOOLEAN_LITERAL -> NUMBER_KEYS
            BrightScriptTokenTypes.LINE_COMMENT -> COMMENT_KEYS
            BrightScriptTokenTypes.REM_COMMENT -> REM_COMMENT_KEYS
            BrightScriptTokenTypes.OPERATOR -> OPERATOR_KEYS
            BrightScriptTokenTypes.IDENTIFIER -> IDENTIFIER_KEYS
            BrightScriptTokenTypes.LPAREN, BrightScriptTokenTypes.RPAREN -> PARENTHESES_KEYS
            BrightScriptTokenTypes.LBRACE, BrightScriptTokenTypes.RBRACE -> BRACES_KEYS
            BrightScriptTokenTypes.LBRACKET, BrightScriptTokenTypes.RBRACKET -> BRACKETS_KEYS
            BrightScriptTokenTypes.DOT -> DOT_KEYS
            BrightScriptTokenTypes.COMMA -> COMMA_KEYS
            BrightScriptTokenTypes.BUILTIN_FUNCTION -> BUILTIN_FUNCTION_KEYS
            BrightScriptTokenTypes.FUNCTION_DECLARATION -> FUNCTION_DECLARATION_KEYS
            BrightScriptTokenTypes.FUNCTION_CALL -> FUNCTION_CALL_KEYS
            else -> EMPTY_KEYS
        }
    }
}
