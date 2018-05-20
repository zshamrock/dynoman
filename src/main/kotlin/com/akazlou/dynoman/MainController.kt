package com.akazlou.dynoman

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.model.TableDescription
import tornadofx.Controller

class MainController : Controller() {
    private val operation = DynamoDBOperation("https://dynamodb.us-west-2.amazonaws.com", Regions.US_WEST_2.name)
    private val tableDescriptions: MutableMap<String, TableDescription> = mutableMapOf()

    fun listTables(): List<DynamoDBTable> {
        return operation.listTables().map { DynamoDBTable(it) }
    }

    fun getTableDescription(table: DynamoDBTable?): TableDescription {
        return tableDescriptions.computeIfAbsent(table?.name.orEmpty(), { operation.getTable(it).describe() })
    }
}