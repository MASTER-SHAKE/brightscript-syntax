package com.maximpietukhov.brightscript

import com.intellij.formatting.*
import com.intellij.psi.codeStyle.CodeStyleSettings

class BrightScriptFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val settings = formattingContext.codeStyleSettings
        return FormattingModelProvider.createFormattingModelForPsiFile(
            formattingContext.containingFile,
            BrightScriptBlock(
                formattingContext.node,
                Wrap.createWrap(WrapType.NONE, false),
                Alignment.createAlignment(),
                settings
            ),
            settings
        )
    }
}
