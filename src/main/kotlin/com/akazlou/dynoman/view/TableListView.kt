package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.MainController
import com.akazlou.dynoman.domain.DynamoDBTable
import com.akazlou.dynoman.domain.Operator
import com.akazlou.dynoman.domain.Order
import com.akazlou.dynoman.domain.QueryCondition
import com.akazlou.dynoman.domain.QuerySearch
import com.akazlou.dynoman.domain.ScanSearch
import com.akazlou.dynoman.domain.SearchType
import com.akazlou.dynoman.domain.Type
import com.akazlou.dynoman.service.DynamoDBOperation
import javafx.collections.ObservableList
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

class TableListView : View() {
    private val controller: MainController by inject()
    private val queryView: QueryView by inject()
    private var tablesList: ObservableList<TreeItem<DynamoDBTable>> by singleAssign()

    override val root = vbox(5.0) {
        borderpaneConstraints {
            prefWidth = 220.0
            prefHeight = 725.0
            useMaxHeight = true
        }
        button("Connect") {
            //setPrefSize(100.0, 40.0)
            prefWidth = 100.0
            action {
                find(ConnectRegionFragment::class).openModal(block = true)
                val properties = Config.getConnectionProperties(app.config)
                queryView.setRegion(properties.region, properties.local)
                val tables = controller.listTables(properties)
                val operation = controller.getClient(properties)
                tablesList.setAll(tables.map { DynamoDBTableTreeItem(it, operation) })
            }
        }
        treeview<DynamoDBTable> {
            vboxConstraints {
                prefHeight = 480.0
                vGrow = Priority.ALWAYS
            }
            root = TreeItem(DynamoDBTable("Tables"))
            root.isExpanded = true
            isShowRoot = false
            cellFactory = Callback<TreeView<DynamoDBTable>, TreeCell<DynamoDBTable>> {
                DynamoDBTextFieldTreeCell(
                        controller.getClient(Config.getConnectionProperties(app.config)),
                        queryView,
                        DynamoDBTableStringConverter())
            }
            tablesList = root.children
        }
        textarea {
            vboxConstraints {
                prefHeight = 200.0
            }
            isEditable = false
        }
    }

    class DynamoDBTableStringConverter : StringConverter<DynamoDBTable>() {
        override fun toString(table: DynamoDBTable): String {
            return table.name
        }

        override fun fromString(string: String): DynamoDBTable? {
            return null
        }

    }

    class DynamoDBTextFieldTreeCell(operation: DynamoDBOperation,
                                    queryView: QueryView,
                                    converter: DynamoDBTableStringConverter) :
            TextFieldTreeCell<DynamoDBTable>(converter) {
        // TODO: #163 Does it mean that context menu is created for each of the tree cell, would it be possible to use one
        // instance for all the tree cells?
        private val tableMenu: ContextMenu = ContextMenu()

        init {
            val scanMenuItem = MenuItem("Scan")
            scanMenuItem.action {
                val tableName = treeItem.value.name
                println("Scan $tableName")
                val description = (treeItem as DynamoDBTableTreeItem).description
                val searchSource = SearchSource(tableName, description.keySchema, false)
                runAsyncWithProgress {
                    operation.scan(ScanSearch(tableName, null, emptyList()))
                } ui { result ->
                    queryView.setQueryResult(
                            operation,
                            description,
                            SearchType.SCAN,
                            tableName,
                            searchSource,
                            null,
                            null,
                            emptyList(),
                            null,
                            emptyList(),
                            result)
                }
            }
            val scanWithOptionsMenuItem = MenuItem("Scan...")
            scanWithOptionsMenuItem.action {
                println("Scan ${treeItem.value.name}")
                val description = (treeItem as DynamoDBTableTreeItem).description
                find<QueryWindowFragment>(
                        params = mapOf(
                                QueryWindowFragment::mode to QueryWindowFragment.Mode.MODAL,
                                QueryWindowFragment::searchType to SearchType.SCAN,
                                QueryWindowFragment::description to description,
                                QueryWindowFragment::operation to operation)).openModal()
            }
            val queryMenuItem = MenuItem("Query...")
            queryMenuItem.action {
                println("Query ${treeItem.value.name}")
                val description = (treeItem as DynamoDBTableTreeItem).description
                find<QueryWindowFragment>(
                        params = mapOf(
                                QueryWindowFragment::mode to QueryWindowFragment.Mode.MODAL,
                                QueryWindowFragment::searchType to SearchType.QUERY,
                                QueryWindowFragment::description to description,
                                QueryWindowFragment::operation to operation)).openModal()
            }
            // TODO: Check whether it is possible to enable this item only when there is the data in the clipboard, and
            // it can represented as String
            val queryMenuItemClipboard = createCustomMenuItem(
                    "Query (clipboard)",
                    "Query the data using the value from the clipboard as the value for the primary key")
            queryMenuItemClipboard.action {
                println("Query (clipboard) ${treeItem.value.name}")
                val tableName = treeItem.value.name
                val description = (treeItem as DynamoDBTableTreeItem).description
                val primaryKey = description.keySchema[0]
                val hashKeyValue = Clipboard.getSystemClipboard().string
                // TODO: Optimize/refactor
                val attributeDefinitionTypes = description.attributeDefinitions.associateBy(
                        { it.attributeName }, { Type.fromString(it.attributeType) })
                val primaryKeyCondition = QueryCondition(
                        primaryKey.attributeName,
                        attributeDefinitionTypes[primaryKey.attributeName]!!,
                        Operator.EQ,
                        listOf(hashKeyValue))
                val querySearch = QuerySearch(
                        tableName,
                        null,
                        listOf(primaryKeyCondition),
                        emptyList(),
                        Order.ASC)
                runAsyncWithProgress {
                    operation.query(querySearch)
                } ui { result ->
                    val searchSource = SearchSource(tableName, description.keySchema, false)
                    queryView.setQueryResult(
                            operation,
                            description,
                            SearchType.QUERY,
                            tableName,
                            searchSource,
                            hashKeyValue,
                            Operator.EQ,
                            emptyList(),
                            Order.ASC,
                            emptyList(),
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

        override fun updateItem(item: DynamoDBTable?, empty: Boolean) {
            super.updateItem(item, empty)
            if (empty || item == null) {
                text = null
                graphic = null
                contextMenu = null
            } else {
                text = item.name
                if (treeItem.parent != null && treeItem.parent == treeView.root) {
                    contextMenu = tableMenu
                }
            }
        }
    }
}
