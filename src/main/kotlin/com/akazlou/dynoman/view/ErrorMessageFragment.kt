package com.akazlou.dynoman.view

import javafx.geometry.Pos
import tornadofx.*

class ErrorMessageFragment : Fragment("Error") {
    val message: String by param()

    override val root = vbox {
        prefWidth = 570.0
        prefHeight = 125.0
        alignment = Pos.CENTER
        spacing = 5.0
        textarea(message) {
            prefRowCount = 2
        }
        button("Close") {
            action {
                close()
            }
        }
    }
}
