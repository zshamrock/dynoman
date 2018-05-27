package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.RunQueryController
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.util.Callback
import tornadofx.*

class QueryView : View("Query") {
    private val controller: RunQueryController by inject()

    private var queryArea: TextArea by singleAssign()
    private var resultTable: TableView<Map<String, Any?>> by singleAssign()

    override val root = vbox {
        queryArea = textarea("SELECT * FROM T") {
            selectAll()
        }
        resultTable = tableview {
        }
        hbox {
            alignment = Pos.CENTER
            button("Run") {
                setPrefSize(100.0, 40.0)
                action {
                    val result = controller.run(getQuery())
                    // setQueryResult(result)
                }
                shortcut("Ctrl+R")
            }
        }
    }

    private fun getQuery(): String {
        return queryArea.text
    }

    fun setQueryResult(result: List<Map<String, Any?>>) {
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
