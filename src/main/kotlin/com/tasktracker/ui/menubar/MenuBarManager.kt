package com.tasktracker.ui.menubar

import com.tasktracker.viewmodel.TaskViewModel
import kotlinx.coroutines.CoroutineScope
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.JWindow

class MenuBarManager(
    private val viewModel: TaskViewModel,
    private val scope: CoroutineScope,
    private val onShowMainWindow: () -> Unit,
    private val onQuit: () -> Unit
) {
    private var trayIcon: TrayIcon? = null
    private var popupWindow: JWindow? = null

    fun initialize() {
        if (!SystemTray.isSupported()) {
            println("System tray is not supported")
            return
        }

        val tray = SystemTray.getSystemTray()
        val icon = createTrayIcon()

        trayIcon = TrayIcon(icon, "Task Tracker").apply {
            isImageAutoSize = true

            // Create popup menu
            val popup = PopupMenu()

            // Show All Tasks menu item
            val showAllItem = MenuItem("Show All Tasks").apply {
                addActionListener {
                    onShowMainWindow()
                }
            }
            popup.add(showAllItem)

            popup.addSeparator()

            // Quit menu item
            val quitItem = MenuItem("Quit").apply {
                addActionListener {
                    onQuit()
                }
            }
            popup.add(quitItem)

            popupMenu = popup

            // Double-click to show main window
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2) {
                        onShowMainWindow()
                    }
                }
            })
        }

        try {
            tray.add(trayIcon)
        } catch (e: AWTException) {
            println("TrayIcon could not be added: ${e.message}")
        }
    }

    fun remove() {
        trayIcon?.let { icon ->
            SystemTray.getSystemTray().remove(icon)
        }
        popupWindow?.dispose()
    }

    private fun createTrayIcon(): Image {
        // Create a simple icon with a checkmark
        val size = 16
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()

        // Enable anti-aliasing
        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )

        // Draw a rounded rectangle background
        g2d.color = Color(107, 78, 255) // AccentPrimary color
        g2d.fillRoundRect(2, 2, size - 4, size - 4, 4, 4)

        // Draw a simple checkmark
        g2d.color = Color.WHITE
        g2d.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g2d.drawLine(4, 8, 7, 11)
        g2d.drawLine(7, 11, 12, 5)

        g2d.dispose()

        return image
    }

    fun updateIcon(taskCount: Int) {
        // Optional: update icon with task count badge
        // For now, we keep it simple
    }
}
