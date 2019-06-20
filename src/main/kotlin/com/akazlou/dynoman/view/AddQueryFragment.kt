package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.MainController
import com.akazlou.dynoman.domain.search.SearchType
import com.akazlou.dynoman.service.DynamoDBOperation
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.stage.StageStyle
import tornadofx.*

// TODO: Can it be a view?
class AddQueryFragment : Fragment("Add Query") {
    private val controller: MainController by inject()
    private val foreignTableProperty = SimpleStringProperty()
    val operation: DynamoDBOperation by param()
    private var columnsField: Field by singleAssign()

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
                        if (selector.isOk()) {
                            val table = selector.getTable()!!
                            foreignTableProperty.value = table.name
                            val searchCriteriaFragment = find<SearchCriteriaFragment>(params = mapOf(
                                    "searchType" to SearchType.QUERY,
                                    "description" to operation.describeTable(table.tableName)
                            ))
                            // TODO: handle multiple adds
                            // TODO: handle correct sizing and scrolling
                            columnsField.add(searchCriteriaFragment.root)
                        }
                    }
                }
            }
            columnsField = field("Columns:", Orientation.VERTICAL) {
            }
        }
    }
}
