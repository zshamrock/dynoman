package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.QueriesSaverController
import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.service.QueriesSaverService
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class SaveSessionFragment : Fragment("Save Session") {

    val searches: List<Search> by param()
    private val controller: QueriesSaverController by inject()

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
                        controller.save(QueriesSaverService.Type.SESSION, base, sessionNameProperty.value, searches)
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
