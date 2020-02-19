package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.ManagedEnvironmentsController
import com.akazlou.dynoman.domain.EnvironmentValue
import com.akazlou.dynoman.domain.ManagedEnvironment
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import tornadofx.*

class ManageEnvironmentFragment : Fragment("Manage Environments") {
    private val controller: ManagedEnvironmentsController by inject()
    val environmentName: String by param()
    private val items = FXCollections.observableList(controller.get(environmentName)
            .values.toMutableList()) { value -> arrayOf(value.nameProperty, value.valueProperty) }
    private var valuesView: TableView<EnvironmentValue> by singleAssign()
    private val removeButtonEnabled: SimpleBooleanProperty = SimpleBooleanProperty(false)
    private val valuesChanged = SimpleBooleanProperty(false)
    private val environmentNameProperty = SimpleStringProperty(environmentName)
    private val environments = controller.list().asObservable()

    init {
        items.addListener(ListChangeListener {
            run {
                valuesChanged.set(true)
            }
        })
        environmentNameProperty.onChange {
            items.setAll(controller.get(it!!).values)
            valuesChanged.set(false)
        }
    }

    override val root = vbox(5.0) {
        prefWidth = 810.0
        prefHeight = 430.0
        alignment = Pos.CENTER
        padding = tornadofx.insets(5.0, 0, 5.0, 0)
        hbox(5.0) {
            hbox(5.0) {
                paddingLeft = 5.0
                combobox(environmentNameProperty) {
                    prefWidth = 200.0
                    items = environments
                }
                button("New") {
                    action {
                        val fragment = find<CreateEnvironmentFragment>()
                        fragment.openModal(block = true)
                        val environment = fragment.getNewManagedEnvironment()
                        if (environment != null) {
                            environments.add(environment.name)
                            environments.sortWith(ManagedEnvironment.COMPARATOR)
                            environmentNameProperty.set(environment.name)
                            items.clear()
                            valuesChanged.set(false)
                        }
                    }
                }
                button("Delete") {
                    enableWhen { environmentNameProperty.isNotEqualTo(ManagedEnvironment.GLOBALS) }
                    action {
                        runAsyncWithProgress {
                            val name = environmentNameProperty.value
                            controller.remove(name)
                            name
                        } ui { name ->
                            environmentNameProperty.set(ManagedEnvironment.GLOBALS)
                            environments.remove(name)
                            valuesChanged.set(false)
                        }
                    }
                }
            }
            region {
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }
            }
            buttonbar {
                paddingRight = 5.0
                button("+") {
                    addClass("button-x")
                    action {
                        items.add(EnvironmentValue("", ""))
                        valuesView.selectionModel.selectLast()
                    }
                }
                button("-") {
                    addClass("button-x")
                    enableWhen { removeButtonEnabled }
                    action {
                        items.remove(valuesView.selectedItem)
                    }
                }
            }
        }
        hbox(5.0) {
            padding = tornadofx.insets(5.0, 5.0, 5.0, 5.0)
            textflow {
                text("You can use defined env vars in the query fields (partition, sort and filters) with the " +
                        "following syntax (auto completion is also available): {{ <variable name> }}.") {
                    addClass("hint")
                }
            }
        }
        valuesView = tableview(items) {
            vgrow = Priority.ALWAYS
            isEditable = true
            column("Variable", EnvironmentValue::nameProperty) {
                prefWidth = 255.0
            }.makeEditable()
            column("Value", EnvironmentValue::valueProperty) {
                prefWidth = 300.0
            }.makeEditable()
        }
        removeButtonEnabled.bind(valuesView.selectionModel.selectedItemProperty().isNotNull)
        buttonbar {
            paddingRight = 5.0
            button("Save") {
                enableWhen { valuesChanged }
                action {
                    controller.save(ManagedEnvironment(environmentNameProperty.value, items))
                    valuesChanged.set(false)
                }
            }
            button("Close") {
                action {
                    close()
                }
            }
        }
    }

    fun getEnvironments(): List<String> {
        return environments
    }

    fun getSelectedEnvironmentName(): String {
        return environmentNameProperty.value
    }
}
