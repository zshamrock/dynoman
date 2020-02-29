package com.akazlou.dynoman.view

import javafx.geometry.Pos
import tornadofx.*

class CloseTabsConfirmationFragment : Fragment("Close Tabs Confirmation") {
    val tabs: Int by param()
    private var confirmed: Boolean = false

    override val root = vbox(5.0, Pos.CENTER) {
        prefWidth = 490.0
        prefHeight = 80.0
        text("You are about to close $tabs tab${if (tabs > 1) "s" else ""}. Are you sure you want to continue?")
        buttonbar {
            paddingRight = 5.0
            button("Cancel") {
                action {
                    close()
                }
            }
            button("Close Tabs") {
                action {
                    confirmed = true
                    close()
                }
            }
        }
    }

    fun isConfirmed(): Boolean {
        return confirmed
    }
}