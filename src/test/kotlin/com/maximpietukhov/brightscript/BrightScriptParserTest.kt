package com.maximpietukhov.brightscript

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.maximpietukhov.brightscript.psi.BrightScriptFunctionDefinition

class BrightScriptParserTest : BasePlatformTestCase() {

    private fun blocksOfType(text: String, type: com.intellij.psi.tree.IElementType): List<PsiElement> {
        val file = myFixture.configureByText("test.brs", text)
        return PsiTreeUtil.collectElements(file) { it.elementType == type }.toList()
    }

    fun testFunctionBlockParsed() {
        val blocks = blocksOfType(
            """
            function foo()
                print 1
            end function
            """.trimIndent(),
            BrightScriptElementTypes.FUNCTION_BLOCK
        )
        assertEquals(1, blocks.size)
    }

    fun testNestedIfInsideFunction() {
        val text = """
            function foo()
                if x = 1 then
                    print 1
                end if
            end function
        """.trimIndent()
        assertEquals(1, blocksOfType(text, BrightScriptElementTypes.FUNCTION_BLOCK).size)
        assertEquals(1, blocksOfType(text, BrightScriptElementTypes.IF_BLOCK).size)
    }

    fun testIfBlockBoundsDoNotEatEndFunction() {
        val text = """
            sub foo()
                if x then
                    print 1
                end if
                print 2
            end sub
        """.trimIndent()
        val ifBlocks = blocksOfType(text, BrightScriptElementTypes.IF_BLOCK)
        assertEquals(1, ifBlocks.size)
        // if block must end at "end if", not consume the rest of the sub
        assertFalse(ifBlocks[0].text.contains("print 2"))
    }

    fun testElseIfStaysInSameBlock() {
        val text = """
            function foo()
                if a then
                    print 1
                else if b then
                    print 2
                else
                    print 3
                end if
            end function
        """.trimIndent()
        assertEquals(1, blocksOfType(text, BrightScriptElementTypes.IF_BLOCK).size)
    }

    fun testDeepNesting() {
        val text = """
            function foo()
                for i = 0 to 10
                    if i > 5 then
                        while x
                            print i
                        end while
                    end if
                end for
            end function
        """.trimIndent()
        assertEquals(1, blocksOfType(text, BrightScriptElementTypes.FOR_BLOCK).size)
        assertEquals(1, blocksOfType(text, BrightScriptElementTypes.IF_BLOCK).size)
        assertEquals(1, blocksOfType(text, BrightScriptElementTypes.WHILE_BLOCK).size)
    }

    fun testMissingEndIfDoesNotBreakParent() {
        // if block without end if must close when the function closes
        val text = """
            function foo()
                if x then
                    print 1
            end function

            function bar()
                print 2
            end function
        """.trimIndent()
        val functions = blocksOfType(text, BrightScriptElementTypes.FUNCTION_BLOCK)
        assertEquals(2, functions.size)
    }

    fun testFunctionDefinitionName() {
        val file = myFixture.configureByText(
            "test.brs",
            """
            function doStuff(a, b)
                print a
            end function
            """.trimIndent()
        )
        val def = PsiTreeUtil.findChildOfType(file, BrightScriptFunctionDefinition::class.java)
        assertNotNull(def)
        assertEquals("doStuff", def!!.name)
    }

    fun testBsFileParsedToo() {
        val file = myFixture.configureByText(
            "test.bs",
            """
            @inject
            class Foo
                sub bar()
                    x = `template`
                end sub
            end class
            """.trimIndent()
        )
        assertEquals(BrightScriptLanguage.INSTANCE, file.language)
        assertEquals(1, PsiTreeUtil.collectElements(file) {
            it.elementType == BrightScriptElementTypes.CLASS_BLOCK
        }.size)
    }
}
