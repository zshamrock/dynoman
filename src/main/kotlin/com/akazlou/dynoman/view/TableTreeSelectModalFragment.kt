package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.ConnectionProperties
import com.akazlou.dynoman.domain.DynamoDBTable
import com.akazlou.dynoman.domain.DynamoDBTableIndex
import com.akazlou.dynoman.service.DynamoDBOperation
import javafx.geometry.Pos
import tornadofx.*

// TODO: Reuse the tree model between main tree view and here, so to avoid network calls and cache if any made
class TableTreeSelectModalFragment : Fragment("Select Table or Index") {
    val operation: DynamoDBOperation by param()
    val connectionProperties: ConnectionProperties by param()
    val tables: List<DynamoDBTable> by param()

    private var applied = false
    private val tableTree: TableTreeSelectFragment = find()

    init {
        // TODO: Try actually to reuse the data from TableListView
        tableTree.refresh(operation, connectionProperties, tables)
    }

    override val root = vbox(5.0) {
        paddingBottom = 5.0
        alignment = Pos.CENTER
        add(tableTree.root)
        // Use hbox instead of buttonbar, as didn't find the proper way to align buttons in the buttonbar centered,
        // and buttonOrder didn't work at all (tried +AC+, but even +AC didn't work, i.e. no button was rendered,
        // although as per documentation that should work).
        hbox(5.0) {
            alignment = Pos.CENTER
            button("Apply") {
                enableWhen {
                    tableTree.getSelection().booleanBinding { value ->
                        value != null && (value.isTable() || value.isIndex())
                    }
                }
                action {
                    applied = true
                    this@TableTreeSelectModalFragment.close()
                }
            }
            button("Cancel") {
                action {
                    this@TableTreeSelectModalFragment.close()
                }
            }
        }

    }

    fun getTable(): DynamoDBTable? {
        val value: DynamoDBTableTreeItemValue = tableTree.getSelection().value
        return when {
            value.isTable() -> DynamoDBTable(value.getText())
            value.isIndex() -> DynamoDBTableIndex((value as DynamoDBTableTreeIndexItem).tableName, value.indexName)
            else -> null
        }
    }

    fun isOk(): Boolean {
        return applied
    }
}
