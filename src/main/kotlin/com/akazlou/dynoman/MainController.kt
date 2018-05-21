package com.akazlou.dynoman

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.model.TableDescription
import tornadofx.*

class MainController : Controller() {
    private val operation = DynamoDBOperation("https://dynamodb.us-west-2.amazonaws.com", Regions.US_WEST_2.name)
    private val tableDescriptions: MutableMap<String, TableDescription> = mutableMapOf()

    fun listTables(): List<DynamoDBTable> {
        return operation.listTables().map { DynamoDBTable(it) }
    }

    fun getTableDescription(table: DynamoDBTable?): TableDescription? {
        return table?.let {
            tableDescriptions.computeIfAbsent(it.name, { operation.getTable(it).describe() })
        }
    }
}