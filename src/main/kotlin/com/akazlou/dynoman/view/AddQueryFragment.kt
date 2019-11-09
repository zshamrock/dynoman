package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.AddQuerySaverController
import com.akazlou.dynoman.controller.MainController
import com.akazlou.dynoman.domain.Config
import com.akazlou.dynoman.domain.Environment
import com.akazlou.dynoman.domain.ForeignSearchName
import com.akazlou.dynoman.domain.search.SearchType
import com.akazlou.dynoman.service.DynamoDBOperation
import javafx.beans.binding.Bindings
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
    private val addQuerySaverController: AddQuerySaverController by inject()
    private val queryNameProperty = SimpleStringProperty()
    private val foreignTableProperty = SimpleStringProperty()
    val operation: DynamoDBOperation by param()
    val attributes: List<String> by param()
    val sourceTable: String by param()
    private var pane: ScrollPane by singleAssign()
    private var searchCriteriaFragment: SearchCriteriaFragment? = null

    companion object {
        private const val QUERY_NAME_STANDARD_PREFIX = "Get"
    }

    enum class Response {
        CREATE,
        CREATE_AND_RUN,
        CANCEL
    }

    var response: Response = Response.CANCEL
        private set

    var foreignSearchName: ForeignSearchName? = null
        private set

    private val createButtonEnabled = Bindings.and(queryNameProperty.isNotEmpty, foreignTableProperty.isNotEmpty)

    override val root = form {
        prefWidth = 850.0
        prefHeight = 380.0
        fieldset("New Query") {
            text("You can use \"?\" as the placeholder for the values where when search is applied you will be asked " +
                    "to manually enter the values.\nThis allows to create parametrized search.") {
                addClass("hint")
            }
            field("Name:") {
                textfield(queryNameProperty)
            }
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
                            if (queryNameProperty.value.isNullOrEmpty()) {
                                queryNameProperty.value = QUERY_NAME_STANDARD_PREFIX +
                                        Environment(table.name).value.capitalize()
                            }
                            // TODO: If it was the index, select it accordingly instead of table
                            searchCriteriaFragment = find<SearchCriteriaFragment>(params = mapOf(
                                    "searchType" to SearchType.QUERY,
                                    "description" to operation.describeTable(table.tableName),
                                    "attributes" to attributes
                            ))
                            pane.content = searchCriteriaFragment!!.root
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
                    enableWhen { createButtonEnabled }
                    action {
                        runAsyncWithProgress {
                            createQuery()
                        } ui {
                            response = Response.CREATE
                            close()
                        }
                    }
                }
                button("Create and Run") {
                    enableWhen { createButtonEnabled }
                    action {
                        runAsyncWithProgress {
                            createQuery()
                        } ui {
                            response = Response.CREATE_AND_RUN
                            close()
                        }
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

    private fun createQuery() {
        val base = Config.getSavedQueriesPath(app.configBasePath)
        foreignSearchName = addQuerySaverController.save(
                sourceTable,
                base,
                queryNameProperty.value,
                searchCriteriaFragment!!.getSearch())
    }
}
