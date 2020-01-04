package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.ManagedEnvironmentsController
import com.akazlou.dynoman.domain.EnvironmentValue
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.control.TableView
import tornadofx.*

class ManageEnvironmentFragment : Fragment("Manage Environments") {
    private val controller: ManagedEnvironmentsController by inject()
    private val items = controller.getGlobals().values.toMutableList().asObservable()
    private var valuesView: TableView<EnvironmentValue> by singleAssign()
    private val removeButtonEnabled: SimpleBooleanProperty = SimpleBooleanProperty(false)

    override val root = vbox {
        prefWidth = 810.0
        prefHeight = 430.0
        alignment = Pos.CENTER
        buttonbar {
            button("+") {
                addClass("button-x")
                action {
                    items.add(EnvironmentValue("", ""))
                    // TODO: Find the way to enable text edit by default and also would be nice tabs to move
                    // Or maybe better configure the cells to be text inputs, so it is also visible, and also tab works
                    // then accordingly
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
        valuesView = tableview(items) {
            isEditable = true
            column("Variable", EnvironmentValue::nameProperty).makeEditable()
            column("Value", EnvironmentValue::valueProperty).makeEditable()
        }
        removeButtonEnabled.bind(valuesView.selectionModel.selectedItemProperty().isNotNull)
    }
}
