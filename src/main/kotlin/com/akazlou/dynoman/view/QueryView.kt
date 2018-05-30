package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.RunQueryController
import com.amazonaws.regions.Regions
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.TabPane
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import javafx.util.Callback
import tornadofx.*

class QueryView : View("Query") {
    private val region = Regions.US_WEST_2
    private val controller: RunQueryController by inject()

    private var queryArea: TextArea by singleAssign()
    private var resultTable: TableView<Map<String, Any?>> by singleAssign()
    private var queries: TabPane by singleAssign()


    override val root = vbox {
        borderpaneConstraints {
            prefHeight = 725.0
            useMaxWidth = true
        }
        val controlArea = hbox {
            vboxConstraints {
                maxHeight = 45.0
            }
            padding = tornadofx.insets(5, 0)
            alignment = Pos.CENTER_LEFT
            button("Save") {
                setPrefSize(100.0, 40.0)
                shortcut("Ctrl+S")
            }
        }
        queries = tabpane {
            vboxConstraints {
                prefHeight = 635.0
                vGrow = Priority.ALWAYS
            }
            tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
            tab("Unnamed") {
                vbox {
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
            }
        }
        hbox {
            vboxConstraints {
                maxHeight = 45.0
            }
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
        hbox {
            vboxConstraints {
                maxHeight = 35.0
            }
            text("Region: ${region.name}") {
                alignment = Pos.CENTER_RIGHT
                font = Font.font("Verdana")
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
