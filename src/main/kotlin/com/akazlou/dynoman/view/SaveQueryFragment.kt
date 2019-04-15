package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.SessionSaverController
import com.akazlou.dynoman.domain.search.Search
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class SaveQueryFragment : Fragment("Save Query") {

    val searches: List<Search> by param()
    private val controller: SessionSaverController by inject()

    private val sessionNameProperty = SimpleStringProperty()

    override val root = form {
        fieldset {
            field("Name:") {
                textfield(sessionNameProperty)
            }
        }
        buttonbar {
            button("Save") {
                enableWhen { sessionNameProperty.isNotBlank() }
                action {
                    // TODO: Wrap in async
                    val path = app.configBasePath
                            .resolve("session")
                            .resolve("${sessionNameProperty.value}.session")
                    controller.save(path, searches, config)
                    println("Saving searches at $path")
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
