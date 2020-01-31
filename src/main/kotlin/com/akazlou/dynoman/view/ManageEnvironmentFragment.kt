package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.ManagedEnvironmentsController
import com.akazlou.dynoman.domain.Config
import com.akazlou.dynoman.domain.EnvironmentValue
import com.akazlou.dynoman.domain.ManagedEnvironment
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import tornadofx.*

class ManageEnvironmentFragment : Fragment("Manage Environments") {
    private val controller: ManagedEnvironmentsController by inject()
    private val items = controller.getGlobals().values.toMutableList().asObservable()
    private var valuesView: TableView<EnvironmentValue> by singleAssign()
    private val removeButtonEnabled: SimpleBooleanProperty = SimpleBooleanProperty(false)

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
        }
        valuesView = tableview(items) {
            vgrow = Priority.ALWAYS
            isEditable = true
            column("Variable", EnvironmentValue::nameProperty).makeEditable()
            column("Value", EnvironmentValue::valueProperty).makeEditable()
        }
        removeButtonEnabled.bind(valuesView.selectionModel.selectedItemProperty().isNotNull)
        buttonbar {
            paddingRight = 5.0
            button("Save") {
                // TODO: Configure the button be enabled on every change and flush when saved
                //enableWhen
                action {
                    // TODO: Redefine the Globals name and build the corresponding single constant
                    val base = Config.getSavedEnvironmentsPath(Config.getProfile(app.config), app.configBasePath)
                    controller.save(base, ManagedEnvironment("Globals", items))
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
