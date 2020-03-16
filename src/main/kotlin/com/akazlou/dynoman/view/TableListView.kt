package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.ConnectionProperties
import com.akazlou.dynoman.domain.DynamoDBTable
import com.akazlou.dynoman.service.DynamoDBOperation
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Priority
import tornadofx.*

class TableListView : View() {
    private val operationProperty: SimpleObjectProperty<DynamoDBOperation> = SimpleObjectProperty()
    private val queryView: QueryView by inject()
    private val tableTree: TableTreeSelectFragment = find()

    override val root = vbox(5.0) {
        borderpaneConstraints {
            prefWidth = 220.0
            prefHeight = 725.0
            useMaxHeight = true
        }
        button("Connect") {
            action {
                find(ConnectionPropertiesFragment::class).openModal(block = true)
            }
        }
        val tableTree = tableTree.root
        tableTree.vgrow = Priority.ALWAYS
        add(tableTree)
        textarea {
            vboxConstraints {
                prefHeight = 200.0
            }
            isWrapText = true
            promptText = "You can use this area to put work in progress data, notes or thoughts"
        }
    }

    fun refresh(operation: DynamoDBOperation, properties: ConnectionProperties, tables: List<DynamoDBTable>) {
        operationProperty.value = operation
        queryView.setRegion(properties.region, properties.local)
        queryView.setOperation(operation)
        tableTree.refresh(operation, properties, tables)
    }
}
