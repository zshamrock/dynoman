package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.MainController
import com.akazlou.dynoman.domain.DynamoDBTable
import javafx.collections.FXCollections
import javafx.scene.control.TreeItem
import tornadofx.View
import tornadofx.cellFormat
import tornadofx.treeview

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
