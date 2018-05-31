package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.OperationType
import com.akazlou.dynoman.domain.QueryResult
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.util.Callback
import tornadofx.*

class QueryTab : Fragment("Query Tab") {
    val result: QueryResult by param()
    private var queryArea: TextArea by singleAssign()
    private var resultTable: TableView<Map<String, Any?>> by singleAssign()
    override val root = vbox {
        queryArea = textarea("SELECT * FROM T") {
            vboxConstraints {
                prefHeight = 300.0
            }
            selectAll()
        }
        resultTable = tableview {
            vboxConstraints {
                prefHeight = 335.0
                vGrow = Priority.ALWAYS
            }
        }
    }

    fun getQuery(): String {
        return queryArea.text
    }

    fun setQueryResult(operationType: OperationType, table: String, result: List<Map<String, Any?>>) {
        val columns = result.firstOrNull()?.keys?.map { attributeName ->
            val column = TableColumn<Map<String, Any?>, String>(attributeName)
            column.cellValueFactory = Callback<TableColumn.CellDataFeatures<Map<String, Any?>, String>, ObservableValue<String>> {
                SimpleStringProperty(it.value.getOrDefault(attributeName, "").toString())
            }
            column
        }
        resultTable.columns.setAll(columns)
        resultTable.items = FXCollections.observableList(result)
    }
}
