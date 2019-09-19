package com.akazlou.dynoman.view

import javafx.geometry.Pos
import javafx.scene.text.TextAlignment
import tornadofx.*

class UpdateAnnouncementFragment : Fragment("Announcement") {
    val version: String by param()
    val announcement: String by param()

    override val root = vbox(5.0, Pos.CENTER) {
        prefWidth = 380.0
        prefHeight = 120.0
        text("New version $version is available")
        separator()
        text(announcement) {
            textAlignment = TextAlignment.CENTER
        }
        button("Ok") {
            action {
                close()
            }
        }
    }
}
