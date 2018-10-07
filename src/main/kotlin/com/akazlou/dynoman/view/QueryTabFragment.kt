package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.OperationType
import com.akazlou.dynoman.domain.Operator
import com.akazlou.dynoman.domain.QueryFilter
import com.akazlou.dynoman.domain.QueryResult
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.SelectionMode
import javafx.scene.control.TabPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Priority
import javafx.util.Callback
import tornadofx.*

class QueryTabFragment : Fragment("Query Tab") {
    val operationType: OperationType by param()
    private var queryArea: TextArea by singleAssign()
    private var resultTable: TableView<ResultData> by singleAssign()
    private var pageNum = 1
    private var hasMorePages = true
    override val root = vbox {
        hbox(alignment = Pos.CENTER_RIGHT) {
            prefHeight = 150.0
            textflow {
                spacing = 10.0
                text("<") {
                    isVisible = pageNum > 1
                    setOnMouseClicked {
                        println("Clicked <")
                        pageNum--
                    }
                    cursor = Cursor.HAND
                }
                text("Viewing 101 to 200 items") {
                }
                text(">") {
                    isVisible = hasMorePages
                    setOnMouseClicked {
                        println("Clicked >")
                        pageNum++
                    }
                    cursor = Cursor.HAND
                }
            }
        }
        squeezebox {
            fold("Query", expanded = true) {
                tabpane {
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    tabMinWidth = 60.0
                    val sql = tab("SQL") {
                        queryArea = textarea("SELECT * FROM T") {
                            vboxConstraints {
                                prefHeight = 300.0
                            }
                            selectAll()
                        }
                    }
                    val qwf = find<QueryWindowFragment>(
                            params = mapOf(
                                    QueryWindowFragment::mode to QueryWindowFragment.Mode.INLINE,
                                    QueryWindowFragment::operationType to operationType,
                                    QueryWindowFragment::description to params["description"],
                                    QueryWindowFragment::operation to params["operation"]))
                    val editor = tab("EDITOR", qwf.root)
                    val queryFilters = params["queryFilters"]
                    qwf.init(operationType,
                            params["queryType"] as QueryType?,
                            params["hashKey"] as String?,
                            params["sortKeyOperation"] as Operator?,
                            (params["sortKeyValues"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            params["sort"] as String?,
                            (queryFilters as? List<*>)?.filterIsInstance<QueryFilter>() ?: emptyList(),
                            this@QueryTabFragment)
                    editor.select()
                }
            }
            fold("Data", expanded = true) {
                resultTable = tableview {
                    vboxConstraints {
                        prefHeight = 335.0
                        vGrow = Priority.ALWAYS
                    }
                    contextmenu {
                        item("Open Value in Viewer") {
                            setOnAction {
                                println("Open Value in Viewer")
                                println(selectedCell)
                            }
                        }
                        separator()
                        item("Copy Row") {
                            setOnAction {
                                if (selectedItem != null) {
                                    val content = (selectedItem as ResultData).getValues().joinToString {
                                        "'$it'"
                                    }
                                    clipboard.putString(content)
                                }
                                println("Copy Row")
                            }
                        }
                        item("Copy Row (with names)") {
                            setOnAction {
                                if (selectedItem != null) {
                                    val resultData = selectedItem as ResultData
                                    val content = (resultData.getKeys().joinToString { it }
                                            + "\n"
                                            + resultData.getValues().joinToString {
                                        "'$it'"
                                    })
                                    clipboard.putString(content)
                                }
                                println("Copy Row (with names)")
                            }
                        }
                        item("Copy Field", KeyCombination.keyCombination("Ctrl+C")) {
                            setOnAction {
                                println("Copy Field")
                                if (selectedValue != null) {
                                    clipboard.putString(selectedValue as String)
                                }
                                println(selectedCell)
                                println(selectedColumn)
                                println(selectedItem)
                                println(selectedValue)
                            }
                        }
                    }
                }
                with(resultTable.selectionModel) {
                    isCellSelectionEnabled = true
                    selectionMode = SelectionMode.SINGLE
                }
            }
        }
    }

    fun getQuery(): String {
        return queryArea.text
    }

    fun setQueryResult(qr: QueryResult) {
        queryArea.text = if (qr.operationType == OperationType.SCAN) "SELECT * FROM ${qr.getTable()}" else ""
        val results = qr.result.map { ResultData(it, qr.getTableHashKey(), qr.getTableSortKey()) }
        val columns = if (results.isEmpty()) {
            emptyList()
        } else {
            results.first().getKeys().map { attributeName ->
                val column = TableColumn<ResultData, String>(attributeName)
                column.cellValueFactory = Callback<TableColumn.CellDataFeatures<ResultData, String>, ObservableValue<String>> {
                    SimpleStringProperty(it.value.getValue(attributeName))
                }
                column
            }
        }
        resultTable.columns.setAll(columns)
        resultTable.items = FXCollections.observableList(results)
    }
}

data class ResultData(val data: Map<String, Any?>, val hashKey: KeySchemaElement, val sortKey: KeySchemaElement?) {
    fun getKeys(): List<String> {
        if (data.isEmpty()) {
            return emptyList()
        }
        val primaryKeys = listOfNotNull(hashKey.attributeName, sortKey?.attributeName)
        return primaryKeys + (data.keys.toList() - primaryKeys).sorted()
    }

    fun getValue(attributeName: String): String {
        return data.getOrDefault(attributeName, "").toString()
    }

    fun getValues(): List<String> {
        return getKeys().map { getValue(it) }
    }
}
