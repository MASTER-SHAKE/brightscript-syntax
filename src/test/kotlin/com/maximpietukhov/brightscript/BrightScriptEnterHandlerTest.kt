package com.maximpietukhov.brightscript

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class BrightScriptEnterHandlerTest : BasePlatformTestCase() {

    fun testAutoClosesFunction() {
        myFixture.configureByText("test.brs", "function foo()<caret>")
        myFixture.type('\n')
        myFixture.checkResult("function foo()\n    <caret>\nend function")
    }

    fun testDoesNotDuplicateExistingEnd() {
        myFixture.configureByText(
            "test.brs",
            "function foo()<caret>\nend function"
        )
        myFixture.type('\n')
        val text = myFixture.editor.document.text
        assertEquals(1, Regex("end function").findAll(text).count())
    }

    fun testSingleLineIfNotClosed() {
        myFixture.configureByText("test.brs", "if x = 1 then return<caret>")
        myFixture.type('\n')
        val text = myFixture.editor.document.text
        assertFalse(text.contains("end if"))
    }

    fun testCommentContinuation() {
        myFixture.configureByText("test.brs", "' some comment<caret>")
        myFixture.type('\n')
        myFixture.checkResult("' some comment\n' <caret>")
    }
}
