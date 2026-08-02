package com.maximpietukhov.brightscript

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

// Brace matcher for (), [], {}. None of these are structural in BrightScript -
// code blocks are bounded by keywords (function/end function), not braces.
class BrightScriptBraceMatcher : PairedBraceMatcher {

    override fun getPairs(): Array<BracePair> = PAIRS

    override fun isPairedBracesAllowedBeforeType(
        lbraceType: IElementType,
        contextType: IElementType?
    ): Boolean {
        return contextType == null
            || contextType == TokenType.WHITE_SPACE
            || contextType == BrightScriptTokenTypes.COMMA
            || contextType == BrightScriptTokenTypes.DOT
            || contextType == BrightScriptTokenTypes.COLON
            || contextType == BrightScriptTokenTypes.SEMICOLON
            || contextType == BrightScriptTokenTypes.RPAREN
            || contextType == BrightScriptTokenTypes.RBRACKET
            || contextType == BrightScriptTokenTypes.RBRACE
            || contextType == BrightScriptTokenTypes.LINE_COMMENT
            || contextType == BrightScriptTokenTypes.REM_COMMENT
    }

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }

    companion object {
        private val PAIRS = arrayOf(
            BracePair(BrightScriptTokenTypes.LPAREN, BrightScriptTokenTypes.RPAREN, false),
            BracePair(BrightScriptTokenTypes.LBRACKET, BrightScriptTokenTypes.RBRACKET, false),
            BracePair(BrightScriptTokenTypes.LBRACE, BrightScriptTokenTypes.RBRACE, false)
        )
    }
}
