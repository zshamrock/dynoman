package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.ConnectionProperties
import com.akazlou.dynoman.domain.DynamoDBTable
import com.akazlou.dynoman.service.DynamoDBOperation
import tornadofx.*

// TODO: Reuse the tree model between main tree view and here, so to avoid network calls and cache if any made
class TableTreeSelectModalFragment : Fragment("Table Tree Modal") {
    val operation: DynamoDBOperation by param()
    val connectionProperties: ConnectionProperties by param()
    val tables: List<DynamoDBTable> by param()

    private val tableTree: TableTreeSelectFragment = find()

    init {
        // TODO: Try actually to reuse the data from TableListView
        tableTree.refresh(operation, connectionProperties, tables)
    }

    override val root = vbox {
        add(tableTree.root)
        button("Apply") {
            action {
                this@TableTreeSelectModalFragment.close()
            }
        }
    }
}
