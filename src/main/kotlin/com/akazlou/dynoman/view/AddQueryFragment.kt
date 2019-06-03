package com.akazlou.dynoman.view

import com.akazlou.dynoman.service.DynamoDBOperation
import javafx.stage.StageStyle
import tornadofx.*

// TODO: Can it be a view?
class AddQueryFragment : Fragment("Add Query") {
    val operation: DynamoDBOperation by param()

    override val root = form {
        fieldset("New Query") {
            field("Foreign table:") {
                textfield()
                button("...") {
                    addClass("button-select")
                    action {
                        find<TableTreeSelectModalFragment>(
                                params = mapOf(
                                        TableTreeSelectModalFragment::operation to operation,
                                        TableTreeSelectModalFragment::connectionProperties to operation.properties,
                                        TableTreeSelectModalFragment::tables to operation.listTables())
                        ).openModal(stageStyle = StageStyle.UTILITY, block = true)
                    }
                }
            }
        }
    }
}
