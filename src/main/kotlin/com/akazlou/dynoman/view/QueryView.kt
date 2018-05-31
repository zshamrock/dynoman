package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.RunQueryController
import com.akazlou.dynoman.domain.OperationType
import com.amazonaws.regions.Regions
import javafx.geometry.Pos
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import tornadofx.*

class QueryView : View("Query") {
    private val region = Regions.US_WEST_2
    private val controller: RunQueryController by inject()
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
                find(QueryTab::class)
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
                    //val result = controller.run(getQuery())
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


    fun setQueryResult(operationType: OperationType, table: String, result: List<Map<String, Any?>>) {

    }
}
