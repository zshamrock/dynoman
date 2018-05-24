package com.akazlou.dynoman

import javafx.collections.FXCollections
import javafx.scene.control.TreeItem
import tornadofx.*

class TableListView : View() {
    private val controller: MainController by inject()

    override val root = treeview<DynamoDBTable> {
        cellFormat { text = it.name }
        root = TreeItem(DynamoDBTable("Tables"))
        root.isExpanded = true
        isShowRoot = false
        val tables = controller.listTables()
        root.children.setAll(
                FXCollections.observableArrayList(tables.map { DynamoDBTableTreeItem(it, controller.operation) }))
    }
}
