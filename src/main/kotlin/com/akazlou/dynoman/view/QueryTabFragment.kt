package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.Operator
import com.akazlou.dynoman.domain.Order
import com.akazlou.dynoman.domain.QueryFilter
import com.akazlou.dynoman.domain.QueryResult
import com.akazlou.dynoman.domain.SearchType
import com.akazlou.dynoman.function.Functions
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.SelectionMode
import javafx.scene.control.TabPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TablePosition
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Priority
import javafx.util.Callback
import tornadofx.*

class QueryTabFragment : Fragment("Query Tab") {
    val searchType: SearchType by param()
    private var queryArea: TextArea by singleAssign()
    private var resultTable: TableView<ResultData> by singleAssign()
    private var pageNum = 1
    private val prevPageVisibleProperty = SimpleBooleanProperty(false)
    private val nextPageVisibleProperty = SimpleBooleanProperty(false)
    private val paginationTextProperty = SimpleStringProperty("")
    private val data = FXCollections.observableArrayList<ResultData>()
    private var queryResult: QueryResult? = null
    private var copyAllByFieldMenu: Menu? = null
    private val allColumns: MutableSet<String> = mutableSetOf()
    private var qwf: QueryWindowFragment by singleAssign()

    override val root = vbox {
        hbox(alignment = Pos.CENTER_RIGHT) {
            prefHeight = 30.0
            maxHeight = 30.0
            minHeight = 30.0
            isFillHeight = false
            textflow {
                spacing = 10.0
                text("<") {
                    visibleWhen(prevPageVisibleProperty)
                    setOnMouseClicked {
                        println("Clicked <")
                        pageNum--
                        data.setAll(queryResult!!.getData(pageNum))
                        prevPageVisibleProperty.value = pageNum > 1
                        nextPageVisibleProperty.value = queryResult!!.hasMoreData(pageNum)
                        updatePaginationTextProperty(pageNum)
                    }
                    cursor = Cursor.HAND
                }
                text(" ")
                text(paginationTextProperty)
                text(" ")
                text(">") {
                    visibleWhen(nextPageVisibleProperty)
                    setOnMouseClicked {
                        println("Clicked >")
                        pageNum++
                        runAsyncWithProgress {
                            queryResult!!.getData(pageNum)
                        } ui { result ->
                            data.setAll(result)
                            prevPageVisibleProperty.value = pageNum > 1
                            nextPageVisibleProperty.value = queryResult!!.hasMoreData(pageNum)
                            updatePaginationTextProperty(pageNum)
                        }
                    }
                    cursor = Cursor.HAND
                }
            }
        }
        squeezebox {
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }
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
                    qwf = find<QueryWindowFragment>(
                            params = mapOf(
                                    QueryWindowFragment::mode to QueryWindowFragment.Mode.INLINE,
                                    QueryWindowFragment::searchType to searchType,
                                    QueryWindowFragment::description to params["description"],
                                    QueryWindowFragment::operation to params["operation"]))
                    val editor = tab("EDITOR", qwf.root)
                    val queryFilters = params["queryFilters"]
                    qwf.init(searchType,
                            params["searchSource"] as SearchSource?,
                            params["hashKeyValue"] as String?,
                            params["sortKeyOperator"] as Operator?,
                            (params["sortKeyValues"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            params["order"] as Order?,
                            (queryFilters as? List<*>)?.filterIsInstance<QueryFilter>() ?: emptyList(),
                            this@QueryTabFragment)
                    editor.select()
                }
            }
            fold("Data", expanded = true) {
                resultTable = tableview(data) {
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
                        separator()
                        item("Copy All") {
                            setOnAction {
                                val content = data.joinToString("\n") { it.getValues(allColumns).joinToString { "'$it'" } }
                                clipboard.putString(content)
                                println("Copy All")
                            }
                        }
                        item("Copy All (with names)") {
                            setOnAction {
                                if (selectedItem != null) {
                                    val content = (allColumns.joinToString { it }
                                            + "\n"
                                            + data.joinToString("\n") { it.getValues(allColumns).joinToString { "'$it'" } })
                                    clipboard.putString(content)
                                }
                                println("Copy All (with names)")
                            }
                        }
                        copyAllByFieldMenu = menu("Copy All by Field")
                        separator()
                        menu("Apply Function") {
                            Functions.getAvailableFunctions().sortedBy { it.name() }.forEach { function ->
                                item(function.name()) {
                                    setOnAction {
                                        if (selectedCell != null && selectedColumn != null && data.isNotEmpty()) {
                                            val attributeName = (selectedColumn as TableColumn<ResultData, *>).text
                                            val column = TableColumn<ResultData, String>("${function.name()}($attributeName)")
                                            column.cellValueFactory = Callback<TableColumn.CellDataFeatures<ResultData, String>, ObservableValue<String>> {
                                                SimpleStringProperty(function.run(it.value.getValue(attributeName)).toString())
                                            }
                                            resultTable.columns.add(
                                                    (selectedCell as TablePosition<ResultData, *>).column + 1, column)
                                        }
                                    }
                                }
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

    fun duplicate(): QueryTabFragment {
        return this
    }

    fun setQueryResult(qr: QueryResult) {
        this.queryResult = qr
        queryArea.text = if (qr.searchType == SearchType.SCAN) "SELECT * FROM ${qr.getTable()}" else ""
        this.pageNum = 1
        val results = qr.getData(pageNum)
        val columns = if (results.isEmpty()) {
            emptyList()
        } else {
            allColumns.clear()
            results.asSequence()
                    .flatMap { it.getKeys().asSequence() }
                    .map { attributeName ->
                        if (allColumns.contains(attributeName)) {
                            null
                        } else {
                            val column = TableColumn<ResultData, String>(attributeName)
                            column.cellValueFactory = Callback<TableColumn.CellDataFeatures<ResultData, String>, ObservableValue<String>> {
                                SimpleStringProperty(it.value.getValue(attributeName))
                            }
                            allColumns.add(attributeName)
                            column
                        }
                    }
                    .filterNotNull()
                    .toList()
        }
        prevPageVisibleProperty.value = false
        nextPageVisibleProperty.value = qr.hasMoreData(pageNum)
        updatePaginationTextProperty(pageNum)
        resultTable.columns.setAll(columns)
        data.setAll(results)
        if (copyAllByFieldMenu!!.items.isEmpty() && data.isNotEmpty()) {
            data[0].getKeys().forEach { key ->
                val item = MenuItem(key)
                item.setOnAction {
                    println("Copy All by Field $key")
                    val content = data.asSequence()
                            .map { it.data[key] }
                            .flatMap {
                                (it as? List<*> ?: listOf(it)).asSequence()
                            }
                            .filterNotNull()
                            .map { it.toString() }
                            .filter { it.isNotBlank() }
                            .distinct()
                            .joinToString("\n")
                    clipboard.putString(content)
                }
                copyAllByFieldMenu!!.items.add(item)
            }
        }
    }

    fun getQueryResult() = queryResult

    private fun updatePaginationTextProperty(pageNum: Int) {
        val (from, to) = queryResult!!.getCurrentDataRange(pageNum)
        paginationTextProperty.value = "Viewing $from to $to items"
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
        return getValues(getKeys().toSet())
    }

    fun getValues(keys: Set<String>): List<String> {
        return keys.map { getValue(it) }
    }
}
