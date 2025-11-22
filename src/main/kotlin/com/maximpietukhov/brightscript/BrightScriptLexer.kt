package com.maximpietukhov.brightscript

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

class BrightScriptLexer : LexerBase() {
    private var buffer: CharSequence? = null
    private var startOffset: Int = 0
    private var endOffset: Int = 0
    private var currentOffset: Int = 0
    private var tokenStart: Int = 0
    private var tokenEnd: Int = 0
    private var tokenType: IElementType? = null

    companion object {
        // Control flow and structure keywords
        private val KEYWORDS = setOf(
            "if", "then", "else", "elseif", "elsif", "endif",
            "for", "to", "step", "next", "each", "endfor", "exitfor",
            "while", "endwhile", "exitwhile",
            "function", "endfunction", "sub", "endsub",
            "return", "exit", "goto", "stop",
            "and", "or", "not", "mod",
            "as", "in", "dim", "let", "print", "run",
            "class", "endclass", "namespace", "endnamespace",
            "interface", "implements", "extends",
            "public", "private", "protected", "override",
            "new", "import", "library", "alias",
            "try", "catch", "endtry", "finally", "throw",
            "continue", "continuefor", "continuewhile",
            "enum", "endenum", "const",
            "m", "super", "me", "global"
        )

        // Type keywords
        private val TYPE_KEYWORDS = setOf(
            "boolean", "integer", "longinteger", "float", "double",
            "string", "object", "function", "dynamic", "brsub",
            "interface"
        )

        // Built-in constants
        private val CONSTANTS = setOf(
            "true", "false", "invalid"
        )

        // Global built-in functions
        private val BUILTIN_FUNCTIONS = setOf(
            // Math functions
            "abs", "atn", "cdbl", "cint", "cos", "csng", "exp", "fix", "int", "log", "rnd", "sgn", "sin", "sqr", "tan",
            // Runtime functions
            "createobject", "type", "getglobalaa", "box", "run", "eval", "getlastruncompileerror", "getlastrunruntimeerror",
            // Global utility functions
            "sleep", "wait", "getinterface", "findmemberfunction", "uptime", "rebootsystem", "listdir",
            "readasciifile", "writeasciifile", "copyfile", "movefile", "matchfiles", "deletefile",
            "deletedirectory", "createdirectory", "formatdrive", "strtoi", "rungarbagecollector",
            "parsejson", "formatjson", "tr",
            // Global string functions
            "ucase", "lcase", "asc", "chr", "instr", "left", "len", "mid", "right", "str", "stri",
            "string", "stringi", "val", "substitute"
        )

        private val OPERATORS = setOf(
            "+", "-", "*", "/", "\\", "^", "=", "<", ">", "<=", ">=", "<>", "<<", ">>", "&"
        )
    }

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        this.currentOffset = startOffset
        this.tokenStart = startOffset
        this.tokenEnd = startOffset
        this.tokenType = null
        advance()
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = tokenType

    override fun getTokenStart(): Int = tokenStart

    override fun getTokenEnd(): Int = tokenEnd

    override fun advance() {
        if (currentOffset >= endOffset) {
            tokenType = null
            return
        }

        tokenStart = currentOffset
        val char = buffer!![currentOffset]

        when {
            char == '\'' && currentOffset + 1 < endOffset && buffer!![currentOffset + 1] == '\'' -> {
                // Double-quote comment '' (yellow)
                while (currentOffset < endOffset && buffer!![currentOffset] != '\n') {
                    currentOffset++
                }
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.REM_COMMENT
            }
            char == '\'' -> {
                // Single-quote comment ' (gray)
                while (currentOffset < endOffset && buffer!![currentOffset] != '\n') {
                    currentOffset++
                }
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.LINE_COMMENT
            }
            (char == 'R' || char == 'r') && currentOffset + 2 < endOffset &&
                buffer!![currentOffset + 1].lowercaseChar() == 'e' &&
                buffer!![currentOffset + 2].lowercaseChar() == 'm' &&
                (currentOffset + 3 >= endOffset || !buffer!![currentOffset + 3].isLetterOrDigit()) -> {
                // REM comment (yellow)
                while (currentOffset < endOffset && buffer!![currentOffset] != '\n') {
                    currentOffset++
                }
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.REM_COMMENT
            }
            char == '"' -> {
                // String literal
                currentOffset++
                while (currentOffset < endOffset) {
                    val c = buffer!![currentOffset]
                    if (c == '"') {
                        currentOffset++
                        break
                    }
                    currentOffset++
                }
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.STRING_LITERAL
            }
            char.isDigit() -> {
                // Number literal
                while (currentOffset < endOffset && (buffer!![currentOffset].isDigit() || buffer!![currentOffset] == '.')) {
                    currentOffset++
                }
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.NUMBER_LITERAL
            }
            char == '&' && currentOffset + 1 < endOffset && buffer!![currentOffset + 1].lowercaseChar() == 'h' -> {
                // Hex literal: &hFF0000
                currentOffset += 2 // skip &h
                while (currentOffset < endOffset && buffer!![currentOffset].let {
                    it.isDigit() || it.lowercaseChar() in 'a'..'f'
                }) {
                    currentOffset++
                }
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.NUMBER_LITERAL
            }
            char.isLetter() || char == '_' || char == '#' -> {
                // Identifier, keyword, type, or preprocessor directive
                while (currentOffset < endOffset && (buffer!![currentOffset].isLetterOrDigit() || buffer!![currentOffset] == '_')) {
                    currentOffset++
                }
                // Check for type designator suffix ($, %, !, #, &, @)
                if (currentOffset < endOffset && buffer!![currentOffset] in setOf('$', '%', '!', '#', '&', '@')) {
                    currentOffset++
                }
                tokenEnd = currentOffset
                val text = buffer!!.subSequence(tokenStart, tokenEnd).toString()
                val lowerText = text.lowercase()

                tokenType = when {
                    KEYWORDS.contains(lowerText) -> BrightScriptTokenTypes.KEYWORD
                    TYPE_KEYWORDS.contains(lowerText) -> BrightScriptTokenTypes.TYPE_KEYWORD
                    CONSTANTS.contains(lowerText) -> BrightScriptTokenTypes.BOOLEAN_LITERAL
                    BUILTIN_FUNCTIONS.contains(lowerText) -> BrightScriptTokenTypes.BUILTIN_FUNCTION
                    else -> BrightScriptTokenTypes.IDENTIFIER
                }
            }
            char.isWhitespace() -> {
                // Skip whitespace
                while (currentOffset < endOffset && buffer!![currentOffset].isWhitespace()) {
                    currentOffset++
                }
                tokenEnd = currentOffset
                tokenType = com.intellij.psi.TokenType.WHITE_SPACE
            }
            char == '(' -> {
                currentOffset++
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.LPAREN
            }
            char == ')' -> {
                currentOffset++
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.RPAREN
            }
            char == '{' -> {
                currentOffset++
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.LBRACE
            }
            char == '}' -> {
                currentOffset++
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.RBRACE
            }
            char == '[' -> {
                currentOffset++
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.LBRACKET
            }
            char == ']' -> {
                currentOffset++
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.RBRACKET
            }
            char == '.' -> {
                currentOffset++
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.DOT
            }
            char == ',' -> {
                currentOffset++
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.COMMA
            }
            char == ':' -> {
                currentOffset++
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.COLON
            }
            char in listOf('+', '-', '*', '/', '\\', '^', '=', '<', '>', '&') -> {
                // Operator (including compound and increment/decrement)
                currentOffset++
                // Check for multi-character operators
                if (currentOffset < endOffset) {
                    val nextChar = buffer!![currentOffset]
                    // Compound assignment operators: +=, -=, *=, /=, \=, <<=, >>=
                    if (nextChar == '=' && char in listOf('+', '-', '*', '/', '\\', '<', '>')) {
                        currentOffset++
                    }
                    // Increment/decrement: ++, --
                    else if ((char == '+' && nextChar == '+') || (char == '-' && nextChar == '-')) {
                        currentOffset++
                    }
                    // Comparison and shift: <=, >=, <>, <<, >>
                    else if ((char == '<' && nextChar in listOf('=', '>', '<')) ||
                             (char == '>' && nextChar in listOf('=', '>'))) {
                        currentOffset++
                        // Handle <<= and >>=
                        if (currentOffset < endOffset && buffer!![currentOffset - 1] in listOf('<', '>') &&
                            buffer!![currentOffset] == '=') {
                            currentOffset++
                        }
                    }
                }
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.OPERATOR
            }
            else -> {
                currentOffset++
                tokenEnd = currentOffset
                tokenType = BrightScriptTokenTypes.BAD_CHARACTER
            }
        }
    }

    override fun getBufferSequence(): CharSequence = buffer!!

    override fun getBufferEnd(): Int = endOffset
}
