package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.SessionSaverController
import com.akazlou.dynoman.domain.search.SearchCriteria
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class SaveQueryFragment : Fragment("Save Query") {

    val searches: List<SearchCriteria> by param()
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
                        val path = app.configBasePath.resolve("sessions")
                        controller.save(path, sessionNameProperty.value, searches, config)
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
