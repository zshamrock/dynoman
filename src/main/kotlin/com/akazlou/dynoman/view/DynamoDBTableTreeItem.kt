package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.DynamoDBTable
import com.akazlou.dynoman.service.DynamoDBOperation
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.TableDescription
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TreeItem

class DynamoDBTableTreeItem(private val table: DynamoDBTable, private var operation: SimpleObjectProperty<DynamoDBOperation>)
    : TreeItem<DynamoDBTable>(table) {
    val description: TableDescription by lazy {
        println("Fetch ${table.name} description")
        operation.value.describeTable(table.name)
    }

    override fun getChildren(): ObservableList<TreeItem<DynamoDBTable>> {
        val children = super.getChildren()
        if (children.isNotEmpty()) {
            return children
        }
        children.setAll(fetchTableDescription())
        return children
    }

    private fun fetchTableDescription(): ObservableList<TreeItem<DynamoDBTable>> {
        val children = FXCollections.observableArrayList<TreeItem<DynamoDBTable>>()
        val provisionedThroughput = description.provisionedThroughput
        children.add(TreeItem(DynamoDBTable("RCU: ${provisionedThroughput.readCapacityUnits}")))
        children.add(TreeItem(DynamoDBTable("WCU: ${provisionedThroughput.writeCapacityUnits}")))
        val attributes = TreeItem(DynamoDBTable("Attributes"))
        val keySchema = description.keySchema
        val attributeDefinitionsByName = description.attributeDefinitions.associateBy { it.attributeName }
        processKeySchema(keySchema, attributeDefinitionsByName, attributes)
        children.add(attributes)
        val indexes = TreeItem(DynamoDBTable("Indexes"))
        val gsi = description.globalSecondaryIndexes
        gsi?.forEach {
            val indexItem = TreeItem(DynamoDBTable(it.indexName))
            indexes.children.add(indexItem)
            processKeySchema(it.keySchema, attributeDefinitionsByName, indexItem)
        }
        children.add(indexes)
        return children
    }

    private fun processKeySchema(keySchema: MutableList<KeySchemaElement>,
                                 attributeDefinitionsByName: Map<String, AttributeDefinition>,
                                 attributes: TreeItem<DynamoDBTable>) {
        keySchema.forEach {
            val attributeDefinition = attributeDefinitionsByName[it.attributeName]
            val attributeType = attributeDefinition?.attributeType
            attributes.children.add(TreeItem(DynamoDBTable("${it.attributeName} (${it.keyType}) $attributeType")))
        }
    }

    override fun isLeaf(): Boolean {
        return false
    }
}