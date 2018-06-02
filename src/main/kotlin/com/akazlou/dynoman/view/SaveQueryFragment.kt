package com.akazlou.dynoman.view

import javafx.geometry.Pos
import tornadofx.*

class SaveQueryFragment : Fragment("Save Query") {
    override val root = vbox {
        hbox {
            label("Name:")
            textfield()
            alignment = Pos.CENTER
        }
        hbox {
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
