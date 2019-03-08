package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.search.SearchCriteria
import javafx.geometry.Pos
import tornadofx.*

class SaveQueryFragment : Fragment("Save Query") {
    val criterias: List<SearchCriteria> by param()

    // TODO: Use TornadoFX form layour to organize the fields
    override val root = vbox(5.0) {
        hbox(5.0) {
            label("Name: ")
            textfield()
            alignment = Pos.CENTER
        }
        hbox(5.0) {
            button("Save") {
                action {
                    println("Saving $criterias")
                    close()
                }
            }
            button("Cancel") {
                action {
                    close()
                }
            }
            alignment = Pos.CENTER
        }
    }
}
