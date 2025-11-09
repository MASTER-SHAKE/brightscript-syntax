package com.maximpietukhov.brightscript

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class BrightScriptParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer {
        return BrightScriptLexer()
    }

    override fun createParser(project: Project?): PsiParser {
        return BrightScriptParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun getCommentTokens(): TokenSet {
        return BrightScriptTokenTypes.COMMENTS
    }

    override fun getStringLiteralElements(): TokenSet {
        return BrightScriptTokenTypes.STRINGS
    }

    override fun createElement(node: ASTNode): PsiElement {
        return BrightScriptPsiElement(node)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return BrightScriptFile(viewProvider)
    }

    companion object {
        val FILE = IFileElementType(BrightScriptLanguage.INSTANCE)
    }
}
