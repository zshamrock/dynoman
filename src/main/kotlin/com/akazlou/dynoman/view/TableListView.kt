package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.MainController
import com.akazlou.dynoman.domain.DynamoDBTable
import com.akazlou.dynoman.domain.SearchType
import com.akazlou.dynoman.service.DynamoDBOperation
import com.amazonaws.regions.Regions
import javafx.collections.ObservableList
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.control.cell.TextFieldTreeCell
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
                val region = getRegion()
                queryView.setRegion(region)
                val tables = controller.listTables(region)
                val operation = controller.getClient(region)
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
                DynamoDBTextFieldTreeCell(controller.getClient(getRegion()), queryView, DynamoDBTableStringConverter())
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

    private fun getRegion(): Regions {
        return Regions.fromName(app.config.string("region", Regions.US_WEST_2.getName()))
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
        private val tableMenu: ContextMenu = ContextMenu()

        init {
            val scanMenuItem = MenuItem("Scan")
            scanMenuItem.action {
                val tableName = treeItem.value.name
                println("Scan $tableName")
                val description = (treeItem as DynamoDBTableTreeItem).description
                val queryType = QueryType(tableName, description.keySchema, false)
                val result = operation.scan(tableName)
                queryView.setQueryResult(
                        operation,
                        description,
                        SearchType.SCAN,
                        tableName,
                        queryType,
                        null,
                        null,
                        emptyList(),
                        null,
                        emptyList(),
                        result)
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
            tableMenu.items.addAll(scanMenuItem, scanWithOptionsMenuItem, queryMenuItem)
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
