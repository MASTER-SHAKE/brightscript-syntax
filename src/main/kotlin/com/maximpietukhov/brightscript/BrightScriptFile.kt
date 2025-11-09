package com.maximpietukhov.brightscript

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class BrightScriptFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, BrightScriptLanguage.INSTANCE) {
    override fun getFileType(): FileType {
        return BrightScriptFileType.INSTANCE
    }

    override fun toString(): String {
        return "BrightScript File"
    }
}
