package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.RunQueryController
import com.akazlou.dynoman.controller.SessionSaverController
import com.akazlou.dynoman.domain.Config
import com.akazlou.dynoman.domain.Version
import com.akazlou.dynoman.domain.search.QueryResult
import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.ext.tab
import com.akazlou.dynoman.service.DynamoDBOperation
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Page
import com.amazonaws.services.dynamodbv2.model.TableDescription
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.stage.StageStyle
import tornadofx.*

class QueryView : View("Query") {
    companion object {
        private const val QUERY_TAB_FRAGMENT_KEY: String = "queryTabFragment"
    }

    private val runQueryController: RunQueryController by inject()
    private val sessionSaverController: SessionSaverController by inject()
    private var queries: TabPane by singleAssign()
    private val region = SimpleStringProperty(Config.getRegion(app.config))
    private val local = SimpleStringProperty(buildLocalText(Config.isLocal(app.config)))
    private val tabContextMenu: ContextMenu
    private val namedQueries = mutableListOf<String>().asObservable()
    private val openSessionNameProperty = SimpleStringProperty()
    private var operation: DynamoDBOperation? = null

    init {
        val duplicate = MenuItem("Duplicate")
        tabContextMenu = ContextMenu(duplicate)

        duplicate.setOnAction {
            val tabs = queries.tabs
            val currentTab = queries.selectionModel.selectedItem
            val currentFragment = currentTab.properties[QUERY_TAB_FRAGMENT_KEY] as QueryTabFragment
            val qr = currentFragment.getQueryResult()!!
            val fragment = currentFragment.duplicate()
            val index = tabs.indexOf(currentTab)
            val tab = queries.tab(index + 1, "${qr.searchType} ${qr.getTable()}", fragment.root)
            tab.properties[QUERY_TAB_FRAGMENT_KEY] = fragment
            tab.contextMenu = tabContextMenu
            queries.selectionModel.select(tab)
        }
        updateNamedQueries()
    }

    override val root = vbox(5.0) {
        borderpaneConstraints {
            prefHeight = 725.0
            useMaxWidth = true
        }
        var saveButton: Button by singleAssign()
        val controlArea = hbox(5.0) {
            vboxConstraints {
                maxHeight = 45.0
            }
            padding = tornadofx.insets(5, 0)
            alignment = Pos.CENTER_LEFT
            // XXX: Move Save and Open outside of the QueryView, either separate view or part of the TableListView
            saveButton = button("Save") {
                shortcut("Ctrl+S")
                action {
                    val criterias = queries.tabs
                            .map { it.properties[QUERY_TAB_FRAGMENT_KEY] as QueryTabFragment }
                            .map { it.getSearch() }
                    find<SaveSessionFragment>(params = mapOf(
                            SaveSessionFragment::searches to criterias
                    )).openModal(stageStyle = StageStyle.UTILITY, block = true)
                    updateNamedQueries()
                }
            }
            combobox<String>(openSessionNameProperty) {
                //setPrefSize(200.0, 40.0)
                prefWidth = 200.0
                items = namedQueries
            }
            button("Open") {
                enableWhen { Bindings.isNotEmpty(openSessionNameProperty) }
                action {
                    runAsyncWithProgress {
                        sessionSaverController.restore(
                                Config.getSavedSessionsPath(app.configBasePath),
                                openSessionNameProperty.value)
                    } ui { searches ->
                        searches.forEach { search ->
                            if (operation != null) {
                                setQueryResult(
                                        operation!!,
                                        operation!!.describeTable(search.table),
                                        search,
                                        // When restore the search from the stored one we don't want to do the network
                                        // (later could be configured over settings whether to do the network call or
                                        // not) calls to fetch the actual data, instead we populate the search view and
                                        // allow the user to do the actual network call when required
                                        null)
                            }
                        }
                    }
                }
            }
            region {
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }
            }
            combobox<String> {
                prefWidth = 200.0
                items = listOf("No Environment").asObservable()
            }
            button("Manage Environments") {
                addClass("button-xlarge")
                action {
                    find<ManageEnvironmentFragment>().openModal(block = true)
                }
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
        saveButton.enableWhen { Bindings.isNotEmpty(queries.tabs) }
        hbox(5.0) {
            vboxConstraints {
                maxHeight = 35.0
            }
            textflow {
                addClass("status")
                alignment = Pos.CENTER_RIGHT
                text("Region: ")
                text(region)
                text(local)
                text(" / v${Version.CURRENT}")
            }
        }
    }

    private fun updateNamedQueries() {
        namedQueries.setAll(sessionSaverController.listNames(Config.getSavedSessionsPath(app.configBasePath)))
    }

    fun setQueryResult(operation: DynamoDBOperation,
                       description: TableDescription,
                       search: Search,
                       page: Page<Item, out Any>?) {
        val fragment = find<QueryTabFragment>(
                params = mapOf(
                        "description" to description,
                        "operation" to operation,
                        "search" to search))
        if (page != null) {
            fragment.setQueryResult(QueryResult(search.type, description, page))
        }
        val tab = queries.tab("${search.type} ${search.table}", fragment.root)
        tab.properties[QUERY_TAB_FRAGMENT_KEY] = fragment
        tab.contextMenu = tabContextMenu
        queries.selectionModel.selectLast()
    }

    fun setRegion(region: Regions, local: Boolean) {
        this.region.value = region.getName()
        this.local.value = buildLocalText(local)
    }

    fun setOperation(operation: DynamoDBOperation) {
        this.operation = operation
    }

    private fun buildLocalText(local: Boolean) = if (local) "(local)" else ""
}
