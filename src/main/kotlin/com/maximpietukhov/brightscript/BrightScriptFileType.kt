package com.maximpietukhov.brightscript

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class BrightScriptFileType private constructor() : LanguageFileType(BrightScriptLanguage.INSTANCE) {
    companion object {
        @JvmStatic
        val INSTANCE = BrightScriptFileType()
    }

    override fun getName(): String = "BrightScript"

    override fun getDescription(): String = "BrightScript file"

    override fun getDefaultExtension(): String = "brs"

    override fun getIcon(): Icon? = BrightScriptIcons.FILE
}
