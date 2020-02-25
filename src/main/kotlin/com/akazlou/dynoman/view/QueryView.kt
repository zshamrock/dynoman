package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.ManagedEnvironmentsController
import com.akazlou.dynoman.controller.RunQueryController
import com.akazlou.dynoman.controller.SessionSaverController
import com.akazlou.dynoman.domain.Config
import com.akazlou.dynoman.domain.ManagedEnvironment
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
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import tornadofx.*

class QueryView : View("Query") {
    companion object {
        private const val QUERY_TAB_FRAGMENT_KEY: String = "queryTabFragment"
    }

    private val runQueryController: RunQueryController by inject()
    private val sessionSaverController: SessionSaverController by inject()
    private val managedEnvironmentsController: ManagedEnvironmentsController by inject()
    private var queries: TabPane by singleAssign()
    private val region = SimpleStringProperty(Config.getRegion(app.config))
    private val local = SimpleStringProperty(buildLocalText(Config.isLocal(app.config)))
    private val tabContextMenu: ContextMenu
    private val namedQueries = mutableListOf<String>().asObservable()
    private val openSessionNameProperty = SimpleStringProperty()
    private val environmentNameProperty = SimpleStringProperty(ManagedEnvironment.GLOBALS)
    private var operation: DynamoDBOperation? = null
    private val environments = managedEnvironmentsController.list().asObservable()

    enum class TabPosition {
        LAST,
        AFTER_CURRENT
    }

    init {
        val rename = MenuItem("Rename")
        val duplicate = MenuItem("Duplicate")
        val closeAll = MenuItem("Close All")
        tabContextMenu = ContextMenu(rename, duplicate, closeAll)

        rename.setOnAction {
            val currentTab = queries.selectionModel.selectedItem
            val updateTabNameFragment = find<UpdateTabNameFragment>(params = mapOf(
                    UpdateTabNameFragment::name to currentTab.text))
            updateTabNameFragment.openModal(block = true)
            val tabName = updateTabNameFragment.getTabName()
            if (tabName.isNotBlank()) {
                currentTab.text = tabName
            }
        }
        duplicate.setOnAction {
            val tabs = queries.tabs
            val currentTab = queries.selectionModel.selectedItem
            val currentFragment = currentTab.properties[QUERY_TAB_FRAGMENT_KEY] as QueryTabFragment
            val qr = currentFragment.getQueryResult()!!
            val fragment = currentFragment.duplicate()
            val index = tabs.indexOf(currentTab)
            val tab = queries.tab(index + 1, currentTab.text, fragment.root)
            tab.properties[QUERY_TAB_FRAGMENT_KEY] = fragment
            tab.contextMenu = tabContextMenu
            queries.selectionModel.select(tab)
        }
        closeAll.setOnAction {
            queries.tabs.clear()
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
                                Config.getSavedSessionsPath(Config.getProfile(app.config), app.configBasePath),
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
            // XXX: Move Save and Open outside of the QueryView, either separate view or part of the TableListView
            saveButton = button("Save") {
                shortcut("Ctrl+S")
                action {
                    val searches = queries.tabs
                            .map { Pair(it, it.properties[QUERY_TAB_FRAGMENT_KEY] as QueryTabFragment) }
                            .map { it.second.getSearch(it.first.text) }
                    find<SaveSessionFragment>(params = mapOf(
                            SaveSessionFragment::searches to searches,
                            SaveSessionFragment::name to openSessionNameProperty.value.orEmpty()
                    )).openModal(block = true)
                    updateNamedQueries()
                }
            }
            region {
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }
            }
            combobox<String>(environmentNameProperty) {
                prefWidth = 200.0
                items = environments
            }
            button("Manage Environments") {
                addClass("button-xlarge")
                action {
                    val fragment = find<ManageEnvironmentFragment>(
                            params = mapOf(ManageEnvironmentFragment::environmentName to environmentNameProperty.value))
                    fragment.openModal(block = true)
                    environments.setAll(fragment.getEnvironments())
                    environmentNameProperty.set(fragment.getSelectedEnvironmentName())
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
        namedQueries.setAll(sessionSaverController.listNames(Config.getSavedSessionsPath(
                Config.getProfile(app.config), app.configBasePath)))
    }

    fun setQueryResult(operation: DynamoDBOperation,
                       description: TableDescription,
                       search: Search,
                       page: Page<Item, out Any>?,
                       position: TabPosition = TabPosition.LAST): Tab {
        val fragment = find<QueryTabFragment>(
                params = mapOf(
                        "description" to description,
                        "operation" to operation,
                        "search" to search))
        if (page != null) {
            fragment.setQueryResult(QueryResult(search.type, description, page))
        }
        val index = if (position == TabPosition.AFTER_CURRENT) {
            queries.selectionModel.selectedIndex + 1
        } else {
            queries.tabs.size
        }
        val tab = queries.tab(index, search.name.ifBlank { "${search.type} ${search.table}" }, fragment.root)
        tab.properties[QUERY_TAB_FRAGMENT_KEY] = fragment
        tab.contextMenu = tabContextMenu
        queries.selectionModel.select(index)
        return tab
    }

    fun select(tab: Tab) {
        queries.selectionModel.select(tab)
    }

    fun setRegion(region: Regions, local: Boolean) {
        this.region.value = region.getName()
        this.local.value = buildLocalText(local)
    }

    fun setOperation(operation: DynamoDBOperation) {
        this.operation = operation
    }

    fun getEnvironmentName(): String {
        return environmentNameProperty.value
    }

    private fun buildLocalText(local: Boolean) = if (local) "(local)" else ""
}
