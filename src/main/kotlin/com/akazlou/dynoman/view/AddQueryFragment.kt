package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.MainController
import com.akazlou.dynoman.domain.search.SearchType
import com.akazlou.dynoman.service.DynamoDBOperation
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.ScrollPane
import javafx.scene.layout.Background
import javafx.scene.layout.Priority
import javafx.stage.StageStyle
import tornadofx.*

class AddQueryFragment : Fragment("Add Query") {
    private val controller: MainController by inject()
    private val foreignTableProperty = SimpleStringProperty()
    val operation: DynamoDBOperation by param()
    val attributes: List<String> by param()
    private var pane: ScrollPane by singleAssign()

    override val root = form {
        prefWidth = 850.0
        prefHeight = 350.0
        fieldset("New Query") {
            field("Foreign table:") {
                // TODO: Add listener to listen on the change and when the text matches the table name dynamically
                //  replace the pane content (will although require to fetch the tables list to be able to do so)
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
                                    "description" to operation.describeTable(table.tableName),
                                    "attributes" to attributes
                            ))
                            pane.content = searchCriteriaFragment.root
                        }
                    }
                }
            }
            field("Columns:", Orientation.VERTICAL) {
                // TODO: Define read-only placeholder to avoid empty space
                pane = scrollpane {
                    background = Background.EMPTY
                    vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
                    hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                }
            }
        }
        hbox {
            vgrow = Priority.ALWAYS
            alignment = Pos.BOTTOM_RIGHT
            isFillHeight = false
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
}
