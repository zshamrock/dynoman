package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.MainController
import com.akazlou.dynoman.domain.DynamoDBTable
import javafx.collections.FXCollections
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.control.cell.TextFieldTreeCell
import javafx.util.Callback
import javafx.util.StringConverter
import tornadofx.*

class TableListView : View() {
    private val controller: MainController by inject()
    private val queryView: QueryView by inject()

    override val root = vbox {
        borderpaneConstraints {
            maxHeight = 50.0
        }
        treeview<DynamoDBTable> {
            vboxConstraints {
                prefWidth = 500.0
            }
            root = TreeItem(DynamoDBTable("Tables"))
            root.isExpanded = true
            isShowRoot = false
            cellFactory = Callback<TreeView<DynamoDBTable>, TreeCell<DynamoDBTable>> {
                DynamoDBTextFieldTreeCell(controller, queryView, DynamoDBTableStringConverter())
            }
            val tables = controller.listTables()
            root.children.setAll(
                    FXCollections.observableArrayList(tables.map { DynamoDBTableTreeItem(it, controller.operation) }))
        }
        textarea {
            vbox {
                prefWidth = 50.0
            }
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

    class DynamoDBTextFieldTreeCell(controller: MainController,
                                    queryView: QueryView,
                                    converter: DynamoDBTableStringConverter) :
            TextFieldTreeCell<DynamoDBTable>(converter) {
        private val tableMenu: ContextMenu = ContextMenu()

        init {
            val scanMenuItem = MenuItem("Scan...")
            scanMenuItem.action {
                val tableName = treeItem.value.name
                println("Scan $tableName")
                val result = controller.scan(tableName)
                queryView.setQueryResult(result)
            }
            val queryMenuItem = MenuItem("Query...")
            queryMenuItem.action {
                println("Query ${treeItem.value.name}")
            }
            tableMenu.items.addAll(scanMenuItem, queryMenuItem)
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
