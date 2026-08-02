package com.maximpietukhov.brightscript

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

// Status bar widget only shows up in projects that have a flavours/ folder
class BrightScriptFlavourWidgetFactory : StatusBarWidgetFactory {

    override fun getId(): String = BrightScriptFlavourWidget.WIDGET_ID

    override fun getDisplayName(): String = "BrightScript Flavour"

    override fun isAvailable(project: Project): Boolean =
        project.getService(BrightScriptFlavourService::class.java).flavoursDir() != null

    override fun createWidget(project: Project): StatusBarWidget = BrightScriptFlavourWidget(project)

    override fun disposeWidget(widget: StatusBarWidget) {}

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}

class BrightScriptFlavourWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.MultipleTextValuesPresentation {

    companion object {
        const val WIDGET_ID = "BrightScriptFlavourWidget"
    }

    override fun ID(): String = WIDGET_ID

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun install(statusBar: StatusBar) {}

    override fun dispose() {}

    override fun getTooltipText(): String = "Select Roku flavour"

    override fun getSelectedValue(): String {
        val service = project.getService(BrightScriptFlavourService::class.java)
        return "Flavour: " + (service.selectedFlavour ?: "none")
    }

    override fun getPopup(): JBPopup? {
        val service = project.getService(BrightScriptFlavourService::class.java)
        val flavours = service.listFlavours()
        if (flavours.isEmpty()) return null

        val step = object : BaseListPopupStep<String>("Roku Flavours", flavours) {
            override fun onChosen(selectedValue: String, finalChoice: Boolean): PopupStep<*>? {
                service.applyFlavour(selectedValue)
                return PopupStep.FINAL_CHOICE
            }
        }
        return JBPopupFactory.getInstance().createListPopup(step)
    }
}
