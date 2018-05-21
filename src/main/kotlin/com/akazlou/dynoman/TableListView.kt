package com.akazlou.dynoman

import javafx.scene.control.TreeItem
import tornadofx.*

class TableListView : View() {
    private val controller: MainController by inject()

    override val root = treeview<DynamoDBTable> {
        cellFormat { text = it.name }
        root = TreeItem(DynamoDBTable("Tables"))
        val tables = controller.listTables()
        val tablesMap = tables.associateBy({ it.name }, { listOf(it) })
        val tableDescriptionItems = listOf(DynamoDBTable("Attributes"), DynamoDBTable("Indexes"))

        populate { parent ->
            if (parent == root) tables else (if (tablesMap.containsKey(parent.value.name)) tableDescriptionItems else null)
        }

        onUserSelect {
            selectedValue?.let { table ->
                if (tablesMap.containsKey(table.name)) {
                    val description = controller.getTableDescription(selectedValue)
                    find<TableDescriptionView>(mapOf(TableDescriptionView::description to description)).openWindow()
                }
            }
        }
    }
}
