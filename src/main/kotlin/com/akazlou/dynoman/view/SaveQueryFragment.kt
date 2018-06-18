package com.akazlou.dynoman.view

import javafx.geometry.Pos
import tornadofx.*

class SaveQueryFragment : Fragment("Save Query") {
    override val root = vbox(5.0) {
        hbox(5.0) {
            label("Name: ")
            textfield()
            alignment = Pos.CENTER
        }
        hbox(5.0) {
            button("Save") {
                setPrefSize(100.0, 40.0)
            }
            button("Cancel") {
                setPrefSize(100.0, 40.0)
                action {
                    close()
                }
            }
            alignment = Pos.CENTER
        }
    }
}
