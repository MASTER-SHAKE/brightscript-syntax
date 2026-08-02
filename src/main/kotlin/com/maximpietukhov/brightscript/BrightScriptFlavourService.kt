package com.maximpietukhov.brightscript

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.wm.WindowManager
import java.io.File

// Manages Roku project flavours: folders inside <projectRoot>/flavours,
// applying one copies its contents over the project root (images, assets, manifest)
@Service(Service.Level.PROJECT)
@State(name = "BrightScriptFlavour", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class BrightScriptFlavourService(private val project: Project) : PersistentStateComponent<BrightScriptFlavourService.FlavourState> {

    class FlavourState {
        var selectedFlavour: String? = null
    }

    private var flavourState = FlavourState()

    override fun getState(): FlavourState = flavourState

    override fun loadState(state: FlavourState) {
        flavourState = state
    }

    val selectedFlavour: String?
        get() = flavourState.selectedFlavour

    fun flavoursDir(): File? {
        val base = project.basePath ?: return null
        val dir = File(base, "flavours")
        return if (dir.isDirectory) dir else null
    }

    fun listFlavours(): List<String> {
        val dirs = flavoursDir()?.listFiles { file -> file.isDirectory } ?: return emptyList()
        return dirs.map { it.name }.sorted()
    }

    fun applyFlavour(name: String) {
        val base = project.basePath ?: return
        val sourceDir = File(File(base, "flavours"), name)
        if (!sourceDir.isDirectory) {
            notify("Flavour folder not found: $sourceDir", NotificationType.ERROR)
            return
        }

        // applying a flavour overwrites files in the project root - ask first
        val answer = Messages.showYesNoDialog(
            project,
            "Apply flavour \"$name\"? This will overwrite matching files in the project root (manifest, images, assets).",
            "Apply Roku Flavour",
            Messages.getQuestionIcon()
        )
        if (answer != Messages.YES) return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Applying flavour: $name", false) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    sourceDir.copyRecursively(File(base), overwrite = true)
                } catch (e: Exception) {
                    notify("Failed to apply flavour $name: ${e.message}", NotificationType.ERROR)
                    return
                }

                flavourState.selectedFlavour = name

                // Refresh VFS so open editors pick up the new manifest/assets
                val baseVFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(File(base))
                if (baseVFile != null) {
                    VfsUtil.markDirtyAndRefresh(true, true, true, baseVFile)
                }

                notify("Flavour applied: $name", NotificationType.INFORMATION)

                ApplicationManager.getApplication().invokeLater {
                    WindowManager.getInstance().getStatusBar(project)
                        ?.updateWidget(BrightScriptFlavourWidget.WIDGET_ID)
                }
            }
        })
    }

    private fun notify(message: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("BrightScript Flavours")
            .createNotification(message, type)
            .notify(project)
    }
}
