package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.RunQueryController
import com.akazlou.dynoman.domain.Operator
import com.akazlou.dynoman.domain.Order
import com.akazlou.dynoman.domain.QueryFilter
import com.akazlou.dynoman.domain.QueryResult
import com.akazlou.dynoman.domain.SearchType
import com.akazlou.dynoman.service.DynamoDBOperation
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Page
import com.amazonaws.services.dynamodbv2.model.TableDescription
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
    private val region = SimpleStringProperty(Config.getRegion(app.config))
    private val local = SimpleStringProperty(buildLocalText(Config.isLocal(app.config)))

    override val root = vbox(5.0) {
        borderpaneConstraints {
            prefHeight = 725.0
            useMaxWidth = true
        }
        val controlArea = hbox(5.0) {
            vboxConstraints {
                maxHeight = 45.0
            }
            padding = tornadofx.insets(5, 0)
            alignment = Pos.CENTER_LEFT
            button("Save") {
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
                //setPrefSize(200.0, 40.0)
                prefWidth = 200.0
                items = namedQueries
            }
            button("Open") {
            }
        }
        queries = tabpane {
            vboxConstraints {
                prefHeight = 635.0
                vGrow = Priority.ALWAYS
            }
            tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
            // TODO: Correctly handle Unnamed tab
            //tab("Unnamed", find(QueryTabFragment::class).root)
        }
        hbox(5.0) {
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
                text(local) {
                    font = Font.font("Verdana")
                }
            }
        }
    }

    fun setQueryResult(operation: DynamoDBOperation,
                       description: TableDescription,
                       searchType: SearchType,
                       table: String,
                       searchSource: SearchSource,
                       hashKeyValue: String?,
                       sortKeyOperator: Operator?,
                       sortKeyValues: List<String>,
                       order: Order?,
                       queryFilters: List<QueryFilter>,
                       page: Page<Item, out Any>) {
        val tab = find<QueryTabFragment>(
                params = mapOf(
                        QueryTabFragment::searchType to searchType,
                        "description" to description,
                        "operation" to operation,
                        "searchSource" to searchSource,
                        "hashKeyValue" to hashKeyValue,
                        "sortKeyOperator" to sortKeyOperator,
                        "sortKeyValues" to sortKeyValues,
                        "order" to order,
                        "queryFilters" to queryFilters))
        tab.setQueryResult(QueryResult(searchType, description, page))
        queries.tab("$searchType $table", tab.root)
        queries.selectionModel.selectLast()
    }

    fun setRegion(region: Regions, local: Boolean) {
        this.region.value = region.getName()
        this.local.value = buildLocalText(local)
    }

    private fun buildLocalText(local: Boolean) = if (local) "(local)" else ""
}
