package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.MainController
import com.akazlou.dynoman.service.DynamoDBOperation
import javafx.beans.property.SimpleStringProperty
import javafx.stage.StageStyle
import tornadofx.*

// TODO: Can it be a view?
class AddQueryFragment : Fragment("Add Query") {
    private val controller: MainController by inject()
    private val foreignTableProperty = SimpleStringProperty()

    val operation: DynamoDBOperation by param()

    override val root = form {
        fieldset("New Query") {
            field("Foreign table:") {
                textfield(foreignTableProperty)
                button("...") {
                    addClass("button-select")
                    action {
                        val selector = find<TableTreeSelectModalFragment>(
                                params = mapOf(
                                        TableTreeSelectModalFragment::operation to operation,
                                        TableTreeSelectModalFragment::connectionProperties to operation.properties,
                                        TableTreeSelectModalFragment::tables to controller.listTables(operation.properties))
                        )
                        selector.openModal(stageStyle = StageStyle.UTILITY, block = true)
                        foreignTableProperty.value = selector.getTableName()
                    }
                }
            }
        }
    }
}
