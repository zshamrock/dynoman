package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.SessionSaverController
import com.akazlou.dynoman.domain.search.SearchCriteria
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import tornadofx.*

// TODO: Define the structure/format of the data stored, and where
class SaveQueryFragment : Fragment("Save Query") {

    val criterias: List<SearchCriteria> by param()
    val controller: SessionSaverController by inject()

    private val sessionNameProperty = SimpleStringProperty()

    // TODO: Use TornadoFX form layout to organize the fields
    override val root = vbox(5.0) {
        hbox(5.0) {
            label("Name: ")
            textfield(sessionNameProperty)
            alignment = Pos.CENTER
        }
        hbox(5.0) {
            button("Save") {
                action {
                    val path = app.configBasePath
                            .resolve("session")
                            .resolve("${sessionNameProperty.value}.session")
                    controller.save(criterias, config)
                    println("Saving $criterias at $path")
                    //Files.write(path, )
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
