package com.akazlou.dynoman.view

import com.akazlou.dynoman.service.DynamoDBOperation
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.TableDescription
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TreeItem

class DynamoDBTableTreeItem(private val value: DynamoDBTableTreeItemValue,
                            private var operation: SimpleObjectProperty<DynamoDBOperation>)
    : TreeItem<DynamoDBTableTreeItemValue>(value) {
    val description: TableDescription by lazy {
        println("Fetch ${value.getText()} description")
        operation.value.describeTable(value.getText())
    }

    override fun getChildren(): ObservableList<TreeItem<DynamoDBTableTreeItemValue>> {
        val children = super.getChildren()
        if (children.isNotEmpty()) {
            return children
        }
        children.setAll(fetchTableDescription())
        return children
    }

    private fun fetchTableDescription(): ObservableList<TreeItem<DynamoDBTableTreeItemValue>> {
        val children = FXCollections.observableArrayList<TreeItem<DynamoDBTableTreeItemValue>>()
        val provisionedThroughput = description.provisionedThroughput
        children.add(TreeItem(
                DynamoDBTableTreeItemValue.textValue("RCU: ${provisionedThroughput.readCapacityUnits}")))
        children.add(TreeItem(
                DynamoDBTableTreeItemValue.textValue("WCU: ${provisionedThroughput.writeCapacityUnits}")))
        val attributes = TreeItem(DynamoDBTableTreeItemValue.textValue("Attributes"))
        val keySchema = description.keySchema
        val attributeDefinitionsByName = description.attributeDefinitions.associateBy { it.attributeName }
        processKeySchema(keySchema, attributeDefinitionsByName, attributes)
        children.add(attributes)
        val indexes = TreeItem(DynamoDBTableTreeItemValue.textValue("Indexes"))
        val gsi = description.globalSecondaryIndexes
        gsi?.forEach {
            val indexItem = TreeItem(DynamoDBTableTreeItemValue.indexValue(description.tableName, it.indexName))
            indexes.children.add(indexItem)
            processKeySchema(it.keySchema, attributeDefinitionsByName, indexItem)
        }
        children.add(indexes)
        return children
    }

    private fun processKeySchema(keySchema: MutableList<KeySchemaElement>,
                                 attributeDefinitionsByName: Map<String, AttributeDefinition>,
                                 attributes: TreeItem<DynamoDBTableTreeItemValue>) {
        keySchema.forEach {
            val attributeDefinition = attributeDefinitionsByName[it.attributeName]
            val attributeType = attributeDefinition?.attributeType
            attributes.children.add(TreeItem(
                    DynamoDBTableTreeItemValue.textValue("${it.attributeName} (${it.keyType}) $attributeType")))
        }
    }

    override fun isLeaf(): Boolean {
        return false
    }
}