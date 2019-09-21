package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.UpdateAnnouncement
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import tornadofx.*

class UpdateAnnouncementFragment : Fragment("Announcement") {
    val update: UpdateAnnouncement by param()
    val confirm: Boolean by param(false)

    override val root = vbox(5.0, Pos.CENTER) {
        if (update.shouldAnnounce()) {
            prefWidth = 570.0
            prefHeight = 300.0
            text(update.announcement) {
                paddingTop = 20.0
                textAlignment = TextAlignment.CENTER
                alignment = Pos.TOP_CENTER
            }
            separator()
            form {
                fieldset("Changelog", labelPosition = Orientation.VERTICAL) {
                    field("", Orientation.VERTICAL) {
                        textarea(update.changelog) {
                            prefRowCount = 8
                            vgrow = Priority.ALWAYS
                        }
                    }
                }
                buttonbar {
                    button("Close") {
                        action {
                            close()
                        }
                    }
                    button("Download") {
                        action {
                            hostServices.showDocument(update.url)
                        }
                    }
                }
            }
        } else {
            prefWidth = 300.0
            prefHeight = 80.0
            text("You are using the latest version!")
            button("Close") {
                action {
                    close()
                }
            }
        }
    }
}
