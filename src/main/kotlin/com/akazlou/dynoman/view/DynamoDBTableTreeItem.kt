package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.DynamoDBTable
import com.akazlou.dynoman.service.DynamoDBOperation
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TreeItem

class DynamoDBTableTreeItem(private val table: DynamoDBTable, private var operation: DynamoDBOperation)
    : TreeItem<DynamoDBTable>(table) {

    override fun getChildren(): ObservableList<TreeItem<DynamoDBTable>> {
        val children = super.getChildren()
        if (children.isNotEmpty()) {
            return children
        }
        children.setAll(fetchTableDescription(table))
        return children
    }

    private fun fetchTableDescription(table: DynamoDBTable): ObservableList<TreeItem<DynamoDBTable>> {
        val description = operation.getTable(table.name).describe()
        val children = FXCollections.observableArrayList<TreeItem<DynamoDBTable>>()
        val attributes = TreeItem(DynamoDBTable("Attributes"))
        val keySchema = description.keySchema
        val attributeDefinitionsByName = description.attributeDefinitions.associateBy { it.attributeName }
        keySchema.forEach {
            val attributeDefinition = attributeDefinitionsByName[it.attributeName]
            val attributeType = attributeDefinition?.attributeType
            attributes.children.add(TreeItem(DynamoDBTable("${it.attributeName} (${it.keyType}) $attributeType")))
        }
        children.add(attributes)
        val indexes = TreeItem(DynamoDBTable("Indexes"))
        val gsi = description.globalSecondaryIndexes
        gsi?.forEach {
            indexes.children.add(TreeItem(DynamoDBTable(it.indexName)))
        }
        children.add(indexes)
        return children
    }

    override fun isLeaf(): Boolean {
        return false
    }
}