package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.SessionSaverController
import com.akazlou.dynoman.domain.Config
import com.akazlou.dynoman.domain.search.Search
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class SaveSessionFragment : Fragment("Save Session") {

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
                    runAsyncWithProgress {
                        val base = Config.getSavedSessionsPath(app.configBasePath)
                        controller.save(base, sessionNameProperty.value, searches)
                    } ui {
                        close()
                    }
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
