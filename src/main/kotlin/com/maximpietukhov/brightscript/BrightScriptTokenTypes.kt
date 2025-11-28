package com.maximpietukhov.brightscript

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class BrightScriptTokenType(debugName: String) : IElementType(debugName, BrightScriptLanguage.INSTANCE) {
    override fun toString(): String = "BrightScriptTokenType." + super.toString()
}

object BrightScriptTokenTypes {
    // Keywords
    @JvmField val KEYWORD = BrightScriptTokenType("KEYWORD")
    @JvmField val TYPE_KEYWORD = BrightScriptTokenType("TYPE_KEYWORD")

    // Literals
    @JvmField val STRING_LITERAL = BrightScriptTokenType("STRING_LITERAL")
    @JvmField val NUMBER_LITERAL = BrightScriptTokenType("NUMBER_LITERAL")
    @JvmField val BOOLEAN_LITERAL = BrightScriptTokenType("BOOLEAN_LITERAL")

    // Identifiers
    @JvmField val IDENTIFIER = BrightScriptTokenType("IDENTIFIER")
    @JvmField val BUILTIN_FUNCTION = BrightScriptTokenType("BUILTIN_FUNCTION")
    @JvmField val FUNCTION_DECLARATION = BrightScriptTokenType("FUNCTION_DECLARATION")
    @JvmField val FUNCTION_CALL = BrightScriptTokenType("FUNCTION_CALL")

    // Operators
    @JvmField val OPERATOR = BrightScriptTokenType("OPERATOR")

    // Comments
    @JvmField val LINE_COMMENT = BrightScriptTokenType("LINE_COMMENT")
    @JvmField val REM_COMMENT = BrightScriptTokenType("REM_COMMENT")

    // Brackets
    @JvmField val LPAREN = BrightScriptTokenType("LPAREN")
    @JvmField val RPAREN = BrightScriptTokenType("RPAREN")
    @JvmField val LBRACE = BrightScriptTokenType("LBRACE")
    @JvmField val RBRACE = BrightScriptTokenType("RBRACE")
    @JvmField val LBRACKET = BrightScriptTokenType("LBRACKET")
    @JvmField val RBRACKET = BrightScriptTokenType("RBRACKET")

    // Other
    @JvmField val DOT = BrightScriptTokenType("DOT")
    @JvmField val COMMA = BrightScriptTokenType("COMMA")
    @JvmField val COLON = BrightScriptTokenType("COLON")
    @JvmField val BAD_CHARACTER = BrightScriptTokenType("BAD_CHARACTER")

    @JvmField
    val COMMENTS = TokenSet.create(LINE_COMMENT, REM_COMMENT)

    @JvmField
    val STRINGS = TokenSet.create(STRING_LITERAL)
}
