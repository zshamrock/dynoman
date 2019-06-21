package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.MainController
import com.akazlou.dynoman.domain.search.SearchType
import com.akazlou.dynoman.service.DynamoDBOperation
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.control.ScrollPane
import javafx.scene.layout.Background
import javafx.stage.StageStyle
import tornadofx.*

class AddQueryFragment : Fragment("Add Query") {
    private val controller: MainController by inject()
    private val foreignTableProperty = SimpleStringProperty()
    val operation: DynamoDBOperation by param()
    private var pane: ScrollPane by singleAssign()

    override val root = form {
        prefWidth = 850.0
        prefHeight = 350.0
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
                            // TODO: If it was the index, select it accordingly instead of table
                            val searchCriteriaFragment = find<SearchCriteriaFragment>(params = mapOf(
                                    "searchType" to SearchType.QUERY,
                                    "description" to operation.describeTable(table.tableName)
                            ))
                            pane.content = searchCriteriaFragment.root
                        }
                    }
                }
            }
            field("Columns:", Orientation.VERTICAL) {
                pane = scrollpane {
                    background = Background.EMPTY
                    vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
                    hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                }
            }
        }
        // TODO: How always to put the buttonbar on the bottom no matter the size of the pane content
        buttonbar {
            button("Create") {
                action {
                    close()
                }
            }
            button("Cancel") {
                action {
                    close()
                }
            }
        }
    }
}
