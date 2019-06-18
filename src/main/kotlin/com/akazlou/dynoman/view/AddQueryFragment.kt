package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.MainController
import com.akazlou.dynoman.domain.search.Operator
import com.akazlou.dynoman.service.DynamoDBOperation
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.stage.StageStyle
import tornadofx.*

// TODO: Can it be a view?
class AddQueryFragment : Fragment("Add Query") {
    private val controller: MainController by inject()
    private val foreignTableProperty = SimpleStringProperty()
    private val sourceTableAttributes = mutableListOf<String>().observable()
    private val hashKeyNameProperty = SimpleStringProperty("")
    private val sortKeyNameProperty = SimpleStringProperty("")
    private val hashKeyProperty = SimpleStringProperty("")
    private val sortKeyProperty = SimpleStringProperty("")
    private val sortKeyOperatorProperty = SimpleObjectProperty(Operator.EQ)

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
                        if (selector.isOk()) {
                            foreignTableProperty.value = selector.getTableName()
                        }
                    }
                }
            }
            field("Columns:", Orientation.VERTICAL) {
                hbox(5.0) {
                    label("Partition Key")
                    label(hashKeyNameProperty)
                    label("=")
                    combobox(values = sourceTableAttributes, property = hashKeyProperty)
                }
                hbox(5.0) {
                    label("Sort Key")
                    label(sortKeyNameProperty)
                    combobox(values = SearchCriteriaFragment.SORT_KEY_AVAILABLE_OPERATORS,
                            property = sortKeyOperatorProperty)
                    combobox(values = sourceTableAttributes, property = sortKeyProperty)
                }
            }
        }
    }
}
