package com.maximpietukhov.brightscript

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType

class BrightScriptTemplateContextType : TemplateContextType("BrightScript") {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        return templateActionContext.file.language == BrightScriptLanguage.INSTANCE
    }
}
