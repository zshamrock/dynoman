package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.SessionSaverController
import com.akazlou.dynoman.domain.search.SearchCriteria
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

// TODO: Define the structure/format of the data stored, and where
class SaveQueryFragment : Fragment("Save Query") {

    val criterias: List<SearchCriteria> by param()
    val controller: SessionSaverController by inject()

    private val sessionNameProperty = SimpleStringProperty()

    override val root = form {
        fieldset {
            field("Name:") {
                textfield(sessionNameProperty)
            }
        }
        buttonbar {
            button("Save") {
                action {
                    val path = app.configBasePath
                            .resolve("session")
                            .resolve("${sessionNameProperty.value}.session")
                    controller.save(path, criterias, config)
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
        }
    }
}
