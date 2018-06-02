package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.DynamoDBTable
import com.akazlou.dynoman.service.DynamoDBOperation
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import tornadofx.*

class MainController : Controller() {
    val operation =
            DynamoDBOperation(
                    "https://dynamodb.us-west-2.amazonaws.com",
                    Regions.US_WEST_2.name,
                    System.getProperty("offline", "false")!!.toBoolean())

    fun listTables(): List<DynamoDBTable> {
        return operation.listTables().map { DynamoDBTable(it) }
    }

    fun scan(table: String): List<Map<String, Any?>> {
        val spec = ScanSpec()
                .withMaxResultSize(50)
        val result = operation.getTable(table).scan(spec)
        return result.map { it.asMap() }
    }
}