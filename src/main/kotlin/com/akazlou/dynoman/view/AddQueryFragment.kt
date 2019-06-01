package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.MainController
import javafx.stage.StageStyle
import tornadofx.*

class AddQueryFragment : Fragment("Add Query") {
    private val controller: MainController by inject()

    override val root = form {
        fieldset("New Query") {
            field("Target:") {
                textfield()
                button("...") {
                    addClass("button-select")
                    action {
                        find<TableTreeSelectFragment>().openModal(stageStyle = StageStyle.UTILITY, block = true)
                    }
                }
            }
        }
    }
}
