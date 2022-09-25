package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.AddQuerySaverController
import com.akazlou.dynoman.controller.MainController
import com.akazlou.dynoman.domain.Environment
import com.akazlou.dynoman.domain.ForeignSearchName
import com.akazlou.dynoman.domain.search.ResultData
import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.domain.search.SearchType
import com.akazlou.dynoman.service.DynamoDBOperation
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.ScrollPane
import javafx.scene.layout.Background
import javafx.scene.layout.Priority
import javafx.stage.StageStyle
import tornadofx.*
import java.util.*

class AddManageQueryFragment : Fragment("Add Query") {
    private val controller: MainController by inject()
    private val addQuerySaverController: AddQuerySaverController by inject()
    private val queryNameProperty = SimpleStringProperty()
    private val foreignTableProperty = SimpleStringProperty()
    val operation: DynamoDBOperation by param()
    val attributes: List<String> by param()
    val sourceTable: String by param()
    val data: List<ResultData> by param()
    val mode: Mode by param(Mode.ADD)
    val names: List<ForeignSearchName> by param(emptyList())
    private var observableNames = names.toMutableList().asObservable()
    private var pane: ScrollPane by singleAssign()
    private var searchCriteriaFragment: SearchCriteriaFragment? = null
    private val foreignQueryNameProperty = SimpleObjectProperty<ForeignSearchName>()

    companion object {
        private const val QUERY_NAME_STANDARD_PREFIX = "Get"
    }

    enum class Response {
        CREATE,
        CREATE_AND_RUN,
        CANCEL
    }

    enum class Mode {
        ADD,
        MANAGE;

        fun isAdd(): Boolean {
            return this == ADD
        }

        fun isManage(): Boolean {
            return this == MANAGE
        }
    }

    var response: Response = Response.CANCEL
        private set

    var foreignSearchName: ForeignSearchName? = null
        private set

    private val createButtonEnabled = Bindings.and(queryNameProperty.isNotEmpty, foreignTableProperty.isNotEmpty)

    init {
        title = if (mode.isManage()) {
            "Manage Queries"
        } else {
            "Add Query"
        }
    }

    override val root = form {
        prefWidth = 850.0
        prefHeight = if (mode.isManage()) {
            420.0
        } else {
            380.0
        }
        fieldset(if (mode.isManage()) {
            "Manage Queries"
        } else {
            "New Query"
        }) {
            if (mode.isManage()) {
                field("Query:") {
                    combobox(property = foreignQueryNameProperty) {
                        items = observableNames
                        useMaxWidth = true
                        valueProperty().onChange { name ->
                            if (name == null) {
                                queryNameProperty.value = null
                                foreignTableProperty.value = null
                                pane.content = null
                                pane.vbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                            } else {
                                queryNameProperty.value = name.name
                                var search = addQuerySaverController.restore(sourceTable, name)
                                foreignTableProperty.value = search.table
                                searchCriteriaFragment = find(params = mapOf(
                                        "searchType" to search.type,
                                        "description" to operation.describeTable(search.table),
                                        "attributes" to attributes
                                ))
                                // Expand ?1, ?2, etc. into ?
                                val values = search.getAllValues()
                                val mapping = values.filter { Search.requiresUserInput(it) }
                                        .associateWith { Search.USER_INPUT_MARK }
                                if (mapping.isNotEmpty()) {
                                    search = search.expand(mapping)
                                }
                                searchCriteriaFragment!!.init(search)
                                pane.content = searchCriteriaFragment!!.root
                                pane.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
                            }
                        }
                    }
                }
            }
            if (mode.isManage()) {
                separator()
            }
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
                                        Environment(table.name).value.replaceFirstChar { it.uppercase(Locale.ROOT) }
                            }
                            // TODO: If it was the index, select it accordingly instead of table
                            searchCriteriaFragment = find(params = mapOf(
                                    "searchType" to SearchType.QUERY,
                                    "description" to operation.describeTable(table.tableName),
                                    "attributes" to attributes
                            ))
                            pane.content = searchCriteriaFragment!!.root
                            pane.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
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
                if (mode.isAdd()) {
                    button("Create") {
                        addClass("button-large")
                        enableWhen { createButtonEnabled }
                        action {
                            runAsyncWithProgress {
                                createQuery()
                            } ui {
                                response = Response.CREATE
                                close()
                            } fail { ex ->
                                find<ErrorMessageFragment>(params = mapOf(ErrorMessageFragment::message to ex.message))
                                        .openModal(block = true)
                            }
                        }
                    }
                    button("Create and Run") {
                        addClass("button-large")
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
                }
                if (mode.isManage()) {
                    button("Update") {
                        addClass("button-large")
                        enableWhen { foreignQueryNameProperty.isNotNull }
                        action {
                            runAsyncWithProgress {
                                createQuery()
                            } ui {
                            }
                        }
                    }
                    button("Delete") {
                        addClass("button-large")
                        enableWhen { foreignQueryNameProperty.isNotNull }
                        action {
                            val confirmation = find<DeleteConfirmationFragment>(params = mapOf(
                                    DeleteConfirmationFragment::type to DeleteConfirmationFragment.Type.FOREIGN_QUERY,
                                    DeleteConfirmationFragment::name to foreignQueryNameProperty.value.getNameWithFlags()))
                            confirmation.openModal(block = true)
                            if (confirmation.isConfirmed()) {
                                runAsyncWithProgress {
                                    deleteQuery()
                                } ui {
                                    observableNames.remove(foreignQueryNameProperty.value)
                                    foreignQueryNameProperty.value = null
                                }
                            }
                        }
                    }
                }
                button(if (mode.isManage()) {
                    "Close"
                } else {
                    "Cancel"
                }) {
                    addClass("button-large")
                    action {
                        close()
                    }
                }
            }
        }
    }

    private fun createQuery() {
        foreignSearchName = addQuerySaverController.save(
                sourceTable,
                queryNameProperty.value,
                searchCriteriaFragment!!.getSearch(),
                data)
    }

    private fun deleteQuery() {
        addQuerySaverController.remove(foreignQueryNameProperty.value)
    }
}
