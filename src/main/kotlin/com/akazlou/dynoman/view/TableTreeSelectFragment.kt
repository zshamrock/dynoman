package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.MainController
import com.akazlou.dynoman.domain.ConnectionProperties
import com.akazlou.dynoman.domain.DynamoDBTable
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeItem
import javafx.scene.layout.Priority
import tornadofx.*

class TableTreeSelectFragment : Fragment("Table Tree") {
    private val controller: MainController by inject()
    private val connectionProperties: ConnectionProperties = Config.getConnectionProperties(app.config)

    override val root = vbox {
        treeview<DynamoDBTable> {
            vboxConstraints {
                prefHeight = 480.0
                vGrow = Priority.ALWAYS
            }
            root = TreeItem(DynamoDBTable("Tables"))
            root.isExpanded = true
            isShowRoot = false
            populate({ table ->
                DynamoDBTableTreeItem(table,
                        SimpleObjectProperty(controller.getClient(connectionProperties)))
            },
                    { parent ->
                        if (parent == root) {
                            controller.listTables(connectionProperties)
                        } else {
                            null
                        }
                    })
        }
        button("Close") {
            action {
                this@TableTreeSelectFragment.close()
            }
        }
    }
}
