package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.ManagedEnvironmentsController
import com.akazlou.dynoman.domain.EnvironmentValue
import javafx.geometry.Pos
import tornadofx.*
import java.util.UUID

class ManageEnvironmentFragment : Fragment("Manage Environments") {
    private val controller: ManagedEnvironmentsController by inject()
    private val items = controller.getGlobals().values.toMutableList().asObservable()

    override val root = vbox {
        prefWidth = 810.0
        prefHeight = 430.0
        alignment = Pos.CENTER
        buttonbar {
            button("+") {
                addClass("button-x")
                action {
                    items.add(EnvironmentValue(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                }
            }
            button("-") {
                addClass("button-x")
            }
        }
        tableview(items) {
            isEditable = true
            column("Variable", EnvironmentValue::nameProperty).makeEditable()
            column("Value", EnvironmentValue::valueProperty).makeEditable()
        }
    }
}
