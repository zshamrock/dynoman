package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.ManagedEnvironmentsController
import com.akazlou.dynoman.domain.EnvironmentValue
import javafx.geometry.Pos
import tornadofx.*

class ManageEnvironmentFragment : Fragment("Manage Environments") {
    private val controller: ManagedEnvironmentsController by inject()

    override val root = vbox {
        prefWidth = 810.0
        prefHeight = 430.0
        alignment = Pos.CENTER
        tableview(controller.getGlobals().values.asObservable()) {
            readonlyColumn("Variable", EnvironmentValue::name)
            readonlyColumn("Value", EnvironmentValue::value)
        }
    }
}
