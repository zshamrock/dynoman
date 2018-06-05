package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.RunQueryController
import com.akazlou.dynoman.domain.OperationType
import com.akazlou.dynoman.domain.QueryResult
import com.amazonaws.regions.Regions
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import javafx.stage.StageStyle
import tornadofx.*

class QueryView : View("Query") {
    private val controller: RunQueryController by inject()
    private var queries: TabPane by singleAssign()
    private val region = SimpleStringProperty(app.config.string("region").orEmpty())

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
                action {
                    find(SaveQueryFragment::class).openModal(stageStyle = StageStyle.UTILITY)
                }
            }
            region {
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }
            }
            val namedQueries = listOf("X", "Y", "Z").observable()
            combobox<String> {
                setPrefSize(200.0, 40.0)
                items = namedQueries
            }
            button("Open") {
                setPrefSize(100.0, 40.0)
            }
        }
        queries = tabpane {
            vboxConstraints {
                prefHeight = 635.0
                vGrow = Priority.ALWAYS
            }
            tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
            tab("Unnamed", find(QueryTabFragment::class).root)
        }
        hbox {
            vboxConstraints {
                maxHeight = 45.0
            }
            alignment = Pos.CENTER
            button("Run") {
                setPrefSize(100.0, 40.0)
                shortcut("Ctrl+R")
                action {
                    //val result = controller.run(getQuery())
                    // setQueryResult(result)
                }
            }
        }
        hbox {
            vboxConstraints {
                maxHeight = 35.0
            }
            textflow {
                alignment = Pos.CENTER_RIGHT
                text("Region: ") {
                    font = Font.font("Verdana")
                }
                text(region) {
                    font = Font.font("Verdana")
                }
            }
        }
    }


    fun setQueryResult(operationType: OperationType, table: String, result: List<Map<String, Any?>>) {
        val tab = find(QueryTabFragment::class)
        tab.setQueryResult(QueryResult(operationType, table, result))
        queries.tab("$operationType $table", tab.root)
        queries.selectionModel.selectLast()
    }

    fun setRegion(region: Regions) {
        this.region.value = region.getName()
    }
}
