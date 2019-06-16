package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.MainController
import com.akazlou.dynoman.domain.ConnectionProperties
import com.akazlou.dynoman.domain.DynamoDBTable
import com.akazlou.dynoman.domain.search.Condition
import com.akazlou.dynoman.domain.search.Operator
import com.akazlou.dynoman.domain.search.Order
import com.akazlou.dynoman.domain.search.QuerySearch
import com.akazlou.dynoman.domain.search.ScanSearch
import com.akazlou.dynoman.domain.search.SearchType
import com.akazlou.dynoman.domain.search.Type
import com.akazlou.dynoman.service.DynamoDBOperation
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.geometry.Pos
import javafx.scene.control.ContextMenu
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.control.Tooltip
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.control.cell.TextFieldTreeCell
import javafx.scene.input.Clipboard
import javafx.scene.layout.Priority
import javafx.util.Callback
import javafx.util.StringConverter
import tornadofx.*
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

class TableTreeSelectFragment : Fragment("Table Tree") {
    companion object {
        private val ROOT_TABLE_TEXT_VALUE = DynamoDBTableTreeItemValue.textValue("Tables")
    }

    private val controller: MainController by inject()
    private val operationProperty: SimpleObjectProperty<DynamoDBOperation> = SimpleObjectProperty()
    private val queryView: QueryView by inject()
    private val masterTablesList: ObservableList<DynamoDBTableTreeItemValue> = FXCollections.observableArrayList()
    private var tablesTree: TreeView<DynamoDBTableTreeItemValue> by singleAssign()
    private val filteredNameProperty = SimpleStringProperty("")
    private val tablesList: FilteredList<DynamoDBTableTreeItemValue> =
            masterTablesList.filtered(FilterTablePredicate.ACCEPT_ALL_PREDICATE)
    private val selectionProperty = SimpleObjectProperty<DynamoDBTableTreeItemValue>(ROOT_TABLE_TEXT_VALUE)
    // Keep the cellFactory cached to be reused when switching between the connection properties
    private val cellFactories = with(
            mutableMapOf<ConnectionProperties,
                    Callback<TreeView<DynamoDBTableTreeItemValue>, TreeCell<DynamoDBTableTreeItemValue>>>()) {
        withDefault { properties ->
            getOrPut(properties) {
                Callback {
                    DynamoDBTextFieldTreeCell(
                            controller.getClient(properties),
                            queryView,
                            DynamoDBTableStringConverter())
                }
            }
        }
    }

    private val predicates: LoadingCache<String, Predicate<DynamoDBTableTreeItemValue>> = Caffeine.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build { FilterTablePredicate(it) }

    init {
        filteredNameProperty.addListener { _, oldValue, newValue ->
            if (oldValue != newValue) {
                tablesList.predicate = if (newValue.isEmpty()) {
                    FilterTablePredicate.ACCEPT_ALL_PREDICATE
                } else {
                    predicates.get(newValue)
                }
            }
        }
    }

    override val root = vbox(5.0) {
        stackpane {
            alignment = Pos.CENTER_RIGHT
            textfield(filteredNameProperty) {
                promptText = "Filter by table name"
                useMaxWidth = true
            }
            button("x") {
                addClass("clear-x")
                action {
                    filteredNameProperty.value = ""
                    tablesList.predicate = FilterTablePredicate.ACCEPT_ALL_PREDICATE
                }
            }
        }
        tablesTree = treeview {
            vboxConstraints {
                prefHeight = 480.0
                vGrow = Priority.ALWAYS
            }
            root = TreeItem(ROOT_TABLE_TEXT_VALUE)
            root.isExpanded = true
            isShowRoot = false
            populate({ table -> DynamoDBTableTreeItem(table, operationProperty) },
                    { parent ->
                        if (parent == root) {
                            tablesList
                        } else {
                            null
                        }
                    })
        }
        tablesTree.bindSelected(selectionProperty)
    }

    fun refresh(operation: DynamoDBOperation, properties: ConnectionProperties, tables: List<DynamoDBTable>) {
        operationProperty.value = operation
        // Initialize the cellFactory for the tree here in order to get the correct operation reference
        tablesTree.cellFactory = cellFactories.getValue(properties)
        filteredNameProperty.value = ""
        masterTablesList.setAll(tables.map { DynamoDBTableTreeItemValue.tableValue(it.name) })
        tablesList.predicate = FilterTablePredicate.ACCEPT_ALL_PREDICATE
    }

    fun getSelection(): SimpleObjectProperty<DynamoDBTableTreeItemValue> {
        return selectionProperty
    }

    class DynamoDBTableStringConverter : StringConverter<DynamoDBTableTreeItemValue>() {
        override fun toString(value: DynamoDBTableTreeItemValue): String {
            return value.getText()
        }

        override fun fromString(string: String): DynamoDBTableTreeItemValue? {
            return null
        }
    }

    // TODO: Configure whether to enable menu or not (for the modal we don't menu enabled or at least different one)
    class DynamoDBTextFieldTreeCell(operation: DynamoDBOperation,
                                    queryView: QueryView,
                                    converter: DynamoDBTableStringConverter) :
            TextFieldTreeCell<DynamoDBTableTreeItemValue>(converter) {
        // TODO: #163 Does it mean that context menu is created for each of the tree cell, would it be possible to use one
        // instance for all the tree cells?
        private val tableMenu: ContextMenu = ContextMenu()

        init {
            val scanMenuItem = MenuItem("Scan")
            scanMenuItem.action {
                val tableName = treeItem.value.getText()
                println("Scan $tableName")
                val description = (treeItem as DynamoDBTableTreeItem).description
                runAsyncWithProgress {
                    val search = ScanSearch(tableName, null, emptyList())
                    Pair(search, operation.scan(search))
                } ui { (search, result) ->
                    queryView.setQueryResult(
                            operation,
                            description,
                            search,
                            result)
                }
            }
            val scanWithOptionsMenuItem = MenuItem("Scan...")
            scanWithOptionsMenuItem.action {
                println("Scan ${treeItem.value.getText()}")
                val description = (treeItem as DynamoDBTableTreeItem).description
                find<QueryWindowFragment>(
                        params = mapOf(
                                QueryWindowFragment::mode to QueryWindowFragment.Mode.MODAL,
                                QueryWindowFragment.SEARCH_TYPE_PARAM to SearchType.SCAN,
                                QueryWindowFragment::description to description,
                                QueryWindowFragment::operation to operation)).openModal()
            }
            val queryMenuItem = MenuItem("Query...")
            queryMenuItem.action {
                println("Query ${treeItem.value.getText()}")
                val description = (treeItem as DynamoDBTableTreeItem).description
                find<QueryWindowFragment>(
                        params = mapOf(
                                QueryWindowFragment::mode to QueryWindowFragment.Mode.MODAL,
                                QueryWindowFragment.SEARCH_TYPE_PARAM to SearchType.QUERY,
                                QueryWindowFragment::description to description,
                                QueryWindowFragment::operation to operation)).openModal()
            }
            // TODO: Check whether it is possible to enable this item only when there is the data in the clipboard, and
            // it can represented as String
            val queryMenuItemClipboard = createCustomMenuItem(
                    "Query (clipboard)",
                    "Query the data using the value from the clipboard as the value for the primary key")
            queryMenuItemClipboard.action {
                println("Query (clipboard) ${treeItem.value.getText()}")
                val tableName = treeItem.value.getText()
                val description = (treeItem as DynamoDBTableTreeItem).description
                val primaryKey = description.keySchema[0]
                val hashKeyValue = Clipboard.getSystemClipboard().string
                // TODO: Optimize/refactor
                val attributeDefinitionTypes = description.attributeDefinitions.associateBy(
                        { it.attributeName }, { Type.fromString(it.attributeType) })
                val hashKey = Condition(
                        primaryKey.attributeName,
                        attributeDefinitionTypes.getValue(primaryKey.attributeName),
                        Operator.EQ,
                        listOf(hashKeyValue))
                val search = QuerySearch(
                        tableName,
                        null,
                        hashKey,
                        null,
                        emptyList(),
                        Order.ASC)
                runAsyncWithProgress {
                    Pair(search, operation.query(search))
                } ui { (search, result) ->
                    queryView.setQueryResult(
                            operation,
                            description,
                            search,
                            result)
                }
            }
            tableMenu.items.addAll(scanMenuItem, scanWithOptionsMenuItem, queryMenuItem, queryMenuItemClipboard)
        }

        private fun createCustomMenuItem(text: String, tooltip: String = ""): CustomMenuItem {
            val node = Label(text)
            val menuItem = CustomMenuItem(node)
            if (tooltip.isNotEmpty()) {
                Tooltip.install(node, Tooltip(tooltip))
            }
            return menuItem
        }

        override fun updateItem(item: DynamoDBTableTreeItemValue?, empty: Boolean) {
            super.updateItem(item, empty)
            if (empty || item == null) {
                text = null
                graphic = null
                contextMenu = null
            } else {
                text = item.getText()
                if (treeItem.parent != null && treeItem.parent == treeView.root) {
                    contextMenu = tableMenu
                }
            }
        }
    }
}

class FilterTablePredicate(var text: String = "") : Predicate<DynamoDBTableTreeItemValue> {
    companion object {
        @JvmField
        val ACCEPT_ALL_PREDICATE = FilterTablePredicate()
    }

    override fun test(table: DynamoDBTableTreeItemValue): Boolean {
        return text.isBlank() || table.getText().contains(text, true)
    }
}
