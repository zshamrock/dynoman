package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.ManagedEnvironmentsController
import com.akazlou.dynoman.domain.EnvironmentValue
import com.akazlou.dynoman.domain.ManagedEnvironment
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import tornadofx.*

class ManageEnvironmentFragment : Fragment("Manage Environments") {
    private val controller: ManagedEnvironmentsController by inject()
    private val items = FXCollections.observableList(controller.restore(
            ManagedEnvironment.GLOBALS).values.toMutableList()) { value -> arrayOf(value.nameProperty, value.valueProperty) }
    private var valuesView: TableView<EnvironmentValue> by singleAssign()
    private val removeButtonEnabled: SimpleBooleanProperty = SimpleBooleanProperty(false)
    private val valuesChanged = SimpleBooleanProperty(false)

    init {
        items.addListener(ListChangeListener {
            run {
                valuesChanged.set(true)
            }
        })
    }

    override val root = vbox(5.0) {
        prefWidth = 810.0
        prefHeight = 430.0
        alignment = Pos.CENTER
        padding = tornadofx.insets(5.0, 0, 5.0, 0)
        hbox(5.0) {
            hbox(5.0) {
                paddingLeft = 5.0
                combobox(values = listOf("No Environment")) {
                    prefWidth = 200.0
                }
                button("New") {
                }
                button("Delete") {
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
                    controller.save(ManagedEnvironment(ManagedEnvironment.GLOBALS, items))
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
}
