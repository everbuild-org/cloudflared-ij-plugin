package io.github.bloeckchengrafik.cloudflared

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.WidgetPresentation
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager
import com.intellij.util.Consumer
import com.intellij.util.concurrency.EdtExecutorService
import com.intellij.util.ui.EdtInvocationManager
import io.github.bloeckchengrafik.cloudflared.CloudflaredSingleton.Companion.getInstance
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import java.awt.event.MouseEvent
import javax.swing.Icon

class CloudflaredWidgetFactory : StatusBarWidgetFactory {
    private val shown = true

    init {
        EdtExecutorService.getScheduledExecutorInstance().scheduleWithFixedDelay({
            EdtInvocationManager.getInstance().invokeLater {
                for (project in ProjectManager.getInstance().openProjects) {
                    val statusBar: StatusBar? = WindowManager.getInstance().getStatusBar(project)
                    statusBar?.updateWidget(id)
                }
            }
        }, 0, 1, java.util.concurrent.TimeUnit.SECONDS)

        Runtime.getRuntime().addShutdownHook(Thread {
            // Kill all processes named cloudflared when the IDE shuts down
            ProcessBuilder("pkill", "cloudflared").start()
        })
    }

    override fun getId(): @NonNls String {
        return "cloudflaredWidged"
    }

    override fun getDisplayName(): @Nls String {
        return "Cloudflared"
    }

    override fun isAvailable(project: Project): Boolean {
        return shown
    }

    override fun isEnabledByDefault(): Boolean {
        return false
    }

    override fun isConfigurable(): Boolean {
        return true
    }

    override fun createWidget(project: Project): StatusBarWidget {
        return CloudflaredWidget(project.getService(StatusBarWidgetsManager::class.java), project)
    }

    override fun disposeWidget(widget: StatusBarWidget) {
    }
    override fun canBeEnabledOn(statusBar: StatusBar): Boolean {
        return true
    }

    private class CloudflaredWidget(private val manager: StatusBarWidgetsManager, private val project: Project) : StatusBarWidget, StatusBarWidget.IconPresentation {
        override fun ID(): String {
            return "cloudflaredWidged"
        }

        override fun install(statusBar: StatusBar) {
        }

        override fun getPresentation(): WidgetPresentation {
            return this
        }

        override fun getTooltipText(): String {
            return "Cloudflared is " + getInstance().getStatus()
        }

        override fun getClickConsumer(): Consumer<MouseEvent> {
            return Consumer { _: MouseEvent? ->
                getInstance().toggleRunning()
            }
        }

        override fun getIcon(): Icon {
            return if (getInstance().isRunning()) CloudflaredIcons.ENABLED else CloudflaredIcons.DISABLED
        }

        override fun dispose() {
        }
    }
}