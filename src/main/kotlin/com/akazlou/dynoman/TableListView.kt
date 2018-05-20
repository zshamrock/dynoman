package com.akazlou.dynoman

import javafx.scene.control.TreeItem
import tornadofx.View
import tornadofx.cellFormat
import tornadofx.populate
import tornadofx.treeview

class TableListView : View() {
    val controller: MainController by inject()

    override val root = treeview<DynamoDBTable> {
        cellFormat { text = it.name }
        root = TreeItem(DynamoDBTable("Tables"))

        populate { parent ->
            if (parent == root) controller.listTables() else null
        }
    }
}
