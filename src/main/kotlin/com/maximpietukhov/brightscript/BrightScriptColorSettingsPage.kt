package com.maximpietukhov.brightscript

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class BrightScriptColorSettingsPage : ColorSettingsPage {
    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Keyword", BrightScriptSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Type Keyword", BrightScriptSyntaxHighlighter.TYPE_KEYWORD),
            AttributesDescriptor("Built-in Function", BrightScriptSyntaxHighlighter.BUILTIN_FUNCTION),
            AttributesDescriptor("Function/Class Declaration", BrightScriptSyntaxHighlighter.FUNCTION_DECLARATION),
            AttributesDescriptor("String", BrightScriptSyntaxHighlighter.STRING),
            AttributesDescriptor("Number", BrightScriptSyntaxHighlighter.NUMBER),
            AttributesDescriptor("Comment", BrightScriptSyntaxHighlighter.COMMENT),
            AttributesDescriptor("REM Comment", BrightScriptSyntaxHighlighter.REM_COMMENT),
            AttributesDescriptor("Operator", BrightScriptSyntaxHighlighter.OPERATOR),
            AttributesDescriptor("Identifier", BrightScriptSyntaxHighlighter.IDENTIFIER),
            AttributesDescriptor("Parentheses", BrightScriptSyntaxHighlighter.PARENTHESES),
            AttributesDescriptor("Braces", BrightScriptSyntaxHighlighter.BRACES),
            AttributesDescriptor("Brackets", BrightScriptSyntaxHighlighter.BRACKETS),
            AttributesDescriptor("Dot", BrightScriptSyntaxHighlighter.DOT),
            AttributesDescriptor("Comma", BrightScriptSyntaxHighlighter.COMMA)
        )

        private const val DEMO_TEXT = """
' Regular comment (gray)
'' Special comment (yellow)
REM This is a REM comment (yellow)
function main() as void
    ' Built-in functions
    print "Hello, World!"
    obj = CreateObject("roArray", 5, true)

    ' Type designators
    name$ = "John"
    count% = 42
    price# = 19.99

    ' Hex literals
    color = &hFF0000
    mask = &h00FF00

    ' Type keywords
    dim numbers as integer
    dim username as string

    ' Compound operators
    count% += 10
    price# -= 5.5
    count% *= 2

    ' Increment/decrement
    count%++
    price#--

    ' Built-in functions
    upperText = UCase("hello")
    length = Len(upperText)
    typeName = Type(count%)

    ' Object example
    person = {
        name: "Jane",
        age: 30
    }

    ' Math functions
    result = Abs(-10)
    sine = Sin(3.14)

    if count% > 10 then
        print "Count is greater than 10"
    else
        print "Count is 10 or less"
    end if

    for i = 0 to 10
        print i
    end for
end function
"""
    }

    override fun getIcon(): Icon? = BrightScriptIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = BrightScriptSyntaxHighlighter()

    override fun getDemoText(): String = DEMO_TEXT

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "BrightScript"
}
