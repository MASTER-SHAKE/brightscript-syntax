package com.maximpietukhov.brightscript

import com.intellij.lang.Language

class BrightScriptLanguage private constructor() : Language("BrightScript") {
    companion object {
        @JvmStatic
        val INSTANCE = BrightScriptLanguage()
    }

    override fun getDisplayName(): String = "BrightScript"
}
