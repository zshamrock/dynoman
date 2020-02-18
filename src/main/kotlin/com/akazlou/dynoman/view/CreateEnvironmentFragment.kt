package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.ManagedEnvironmentsController
import com.akazlou.dynoman.domain.ManagedEnvironment
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class CreateEnvironmentFragment : Fragment("Create Environment") {
    private val controller: ManagedEnvironmentsController by inject()

    private val environmentNameProperty = SimpleStringProperty()
    private var managedEnvironment: ManagedEnvironment? = null

    override val root = form {
        fieldset {
            field("Name:") {
                textfield(environmentNameProperty)
            }
        }
        buttonbar {
            button("Create") {
                enableWhen { environmentNameProperty.isNotBlank() }
                action {
                    runAsyncWithProgress {
                        val environment = ManagedEnvironment(environmentNameProperty.value, emptyList())
                        controller.save(environment)
                        environment
                    } ui { environment ->
                        managedEnvironment = environment
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

    fun getNewManagedEnvironment(): ManagedEnvironment? {
        return managedEnvironment
    }
}
