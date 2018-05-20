package com.akazlou.dynoman

import com.amazonaws.regions.Regions
import tornadofx.Controller

class MainController: Controller() {
    private val operation = DynamoDBOperation("http://localhost:9000", Regions.US_EAST_1.name)

    fun listTables(): List<DynamoDBTable> {
        //return operation.listTables().map { DynamoDBTable(it) }
        return listOf("Table1", "Table2", "Table3").map { DynamoDBTable(it) }
    }
}