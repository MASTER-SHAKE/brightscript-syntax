package com.maximpietukhov.brightscript

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrightScriptLexerTest {

    private fun tokens(text: String): List<Pair<IElementType, String>> {
        val lexer = BrightScriptLexer()
        lexer.start(text, 0, text.length, 0)
        val result = mutableListOf<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            result.add(lexer.tokenType!! to text.substring(lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
        return result
    }

    private fun significantTokens(text: String) =
        tokens(text).filter { it.first != TokenType.WHITE_SPACE }

    @Test
    fun keywordsAreCaseInsensitive() {
        val result = significantTokens("IF x THEN")
        assertEquals(BrightScriptTokenTypes.KEYWORD, result[0].first)
        assertEquals("IF", result[0].second)
        assertEquals(BrightScriptTokenTypes.KEYWORD, result[2].first)
        assertEquals("THEN", result[2].second)
    }

    @Test
    fun commentVariants() {
        assertEquals(BrightScriptTokenTypes.LINE_COMMENT, tokens("' plain comment")[0].first)
        assertEquals(BrightScriptTokenTypes.REM_COMMENT, tokens("'' doc comment")[0].first)
        assertEquals(BrightScriptTokenTypes.REM_COMMENT, tokens("REM old style")[0].first)
        assertEquals(BrightScriptTokenTypes.REM_COMMENT, tokens("rem lowercase")[0].first)
    }

    @Test
    fun remPrefixWordIsNotComment() {
        // "remove" starts with rem but is an identifier
        val result = significantTokens("remove()")
        assertEquals(BrightScriptTokenTypes.FUNCTION_CALL, result[0].first)
        assertEquals("remove", result[0].second)
    }

    @Test
    fun stringLiteralStopsAtNewline() {
        val result = tokens("\"unclosed\nprint")
        assertEquals(BrightScriptTokenTypes.STRING_LITERAL, result[0].first)
        assertEquals("\"unclosed", result[0].second)
        assertTrue(result.any { it.first == BrightScriptTokenTypes.KEYWORD && it.second == "print" })
    }

    @Test
    fun numbersAndHexLiterals() {
        assertEquals(BrightScriptTokenTypes.NUMBER_LITERAL, tokens("123")[0].first)
        assertEquals(BrightScriptTokenTypes.NUMBER_LITERAL, tokens("1.5e10")[0].first)
        val hex = tokens("&hFF00AA")
        assertEquals(BrightScriptTokenTypes.NUMBER_LITERAL, hex[0].first)
        assertEquals("&hFF00AA", hex[0].second)
    }

    @Test
    fun annotationToken() {
        val result = significantTokens("@inject")
        assertEquals(BrightScriptTokenTypes.ANNOTATION, result[0].first)
        assertEquals("@inject", result[0].second)
    }

    @Test
    fun templateStringSingleToken() {
        val text = "`hello \${name} world`"
        val result = tokens(text)
        assertEquals(BrightScriptTokenTypes.TEMPLATE_STRING, result[0].first)
        assertEquals(text, result[0].second)
    }

    @Test
    fun templateStringSpansLines() {
        val text = "`line one\nline two`"
        val result = tokens(text)
        assertEquals(BrightScriptTokenTypes.TEMPLATE_STRING, result[0].first)
        assertEquals(text, result[0].second)
    }

    @Test
    fun functionDeclarationAndCall() {
        val decl = significantTokens("function doStuff()")
        assertEquals(BrightScriptTokenTypes.KEYWORD, decl[0].first)
        assertEquals(BrightScriptTokenTypes.FUNCTION_DECLARATION, decl[1].first)

        val call = significantTokens("doStuff()")
        assertEquals(BrightScriptTokenTypes.FUNCTION_CALL, call[0].first)
    }

    @Test
    fun builtinFunctionsRecognized() {
        val result = significantTokens("CreateObject(\"roSGNode\")")
        assertEquals(BrightScriptTokenTypes.BUILTIN_FUNCTION, result[0].first)
    }

    @Test
    fun compoundOperators() {
        val result = significantTokens("x += 1")
        assertEquals(BrightScriptTokenTypes.OPERATOR, result[1].first)
        assertEquals("+=", result[1].second)
    }

    @Test
    fun typeDesignatorSuffix() {
        val result = significantTokens("count% = 1")
        assertEquals(BrightScriptTokenTypes.IDENTIFIER, result[0].first)
        assertEquals("count%", result[0].second)
    }

    @Test
    fun badCharacterStillReported() {
        val result = significantTokens("x = ±")
        assertTrue(result.any { it.first == BrightScriptTokenTypes.BAD_CHARACTER })
    }

    @Test
    fun lonelyAtIsNotAnnotation() {
        // xml attribute access designator after identifier keeps old behavior
        val result = significantTokens("node@ attr")
        assertEquals("node@", result[0].second)
    }
}
