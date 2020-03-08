package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.SessionSaverController
import com.akazlou.dynoman.domain.search.Search
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class SaveSessionFragment : Fragment("Save Session") {
    companion object {
        private const val UNDEFINED = ""
    }

    val searches: List<Search> by param()
    val name: String by param(UNDEFINED)
    private val controller: SessionSaverController by inject()

    private val sessionNameProperty = SimpleStringProperty(name)

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
                        controller.save(sessionNameProperty.value, searches)
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
