package com.akazlou.dynoman

import com.amazonaws.regions.Regions
import tornadofx.*

class MainController : Controller() {
    val operation = DynamoDBOperation("https://dynamodb.us-west-2.amazonaws.com", Regions.US_WEST_2.name)

    fun listTables(): List<DynamoDBTable> {
        return operation.listTables().map { DynamoDBTable(it) }
    }
}