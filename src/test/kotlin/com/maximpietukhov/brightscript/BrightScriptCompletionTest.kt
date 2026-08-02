package com.maximpietukhov.brightscript

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class BrightScriptCompletionTest : BasePlatformTestCase() {

    fun testBuiltinFunctionCompletion() {
        myFixture.configureByText("test.brs", "sub main()\n    x = Crea<caret>\nend sub")
        myFixture.completeBasic()
        val lookups = myFixture.lookupElementStrings ?: emptyList()
        assertTrue("expected CreateObject in $lookups", lookups.contains("CreateObject"))
    }

    fun testTypeCompletionAfterAs() {
        myFixture.configureByText("test.brs", "function foo(a as Int<caret>)\nend function")
        myFixture.completeBasic()
        val lookups = myFixture.lookupElementStrings ?: emptyList()
        assertTrue("expected Integer in $lookups", lookups.contains("Integer"))
    }

    fun testIdentifierFromFileCompletion() {
        myFixture.configureByText(
            "test.brs",
            "sub main()\n    myLongVariable = 1\n    print myLon<caret>\nend sub"
        )
        myFixture.completeBasic()
        val lookups = myFixture.lookupElementStrings
        // single exact match may be auto-inserted, then lookups is null
        if (lookups != null) {
            assertTrue("expected myLongVariable in $lookups", lookups.contains("myLongVariable"))
        } else {
            assertTrue(myFixture.editor.document.text.contains("print myLongVariable"))
        }
    }
}
