package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.OperationType
import com.akazlou.dynoman.domain.QueryResult
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.scene.control.SelectionMode
import javafx.scene.control.TabPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.util.Callback
import tornadofx.*

class QueryTabFragment : Fragment("Query Tab") {
    private var queryArea: TextArea by singleAssign()
    private var resultTable: TableView<Map<String, Any?>> by singleAssign()
    override val root = squeezebox {
        fold("Query", expanded = true) {
            tabpane {
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                tab("SQL") {
                    queryArea = textarea("SELECT * FROM T") {
                        vboxConstraints {
                            prefHeight = 300.0
                        }
                        selectAll()
                    }
                }
                tab("Editor") {
                    find<QueryEditorFragment>().root
                }
            }
        }
        fold("Data", expanded = true) {
            resultTable = tableview {
                vboxConstraints {
                    prefHeight = 335.0
                    vGrow = Priority.ALWAYS
                }
            }
            with(resultTable.selectionModel) {
                isCellSelectionEnabled = true
                selectionMode = SelectionMode.MULTIPLE
            }
        }
    }

    fun getQuery(): String {
        return queryArea.text
    }

    fun setQueryResult(qr: QueryResult) {
        queryArea.text = if (qr.operationType == OperationType.SCAN) "SELECT * FROM ${qr.table}" else ""
        if (qr.result.isEmpty()) {
            return
        }
        val columns = qr.result.firstOrNull()?.keys?.map { attributeName ->
            val column = TableColumn<Map<String, Any?>, String>(attributeName)
            column.cellValueFactory = Callback<TableColumn.CellDataFeatures<Map<String, Any?>, String>, ObservableValue<String>> {
                SimpleStringProperty(it.value.getOrDefault(attributeName, "").toString())
            }
            column
        }
        resultTable.columns.setAll(columns)
        resultTable.items = FXCollections.observableList(qr.result)
    }
}
