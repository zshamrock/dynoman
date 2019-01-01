package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.ConnectionProperties
import com.akazlou.dynoman.domain.DynamoDBTable
import com.akazlou.dynoman.service.DynamoDBOperation
import tornadofx.*

class MainController : Controller() {
    private val clients = with(mutableMapOf<ConnectionProperties, DynamoDBOperation>()) {
        withDefault { properties ->
            println("Build DynamoDB connection using $properties")
            getOrPut(properties) {
                DynamoDBOperation(
                        properties,
                        System.getProperty("offline", "false")!!.toBoolean())
            }
        }
    }

    fun listTables(properties: ConnectionProperties): List<DynamoDBTable> {
        return getClient(properties).listTables().map { DynamoDBTable(it) }
    }

    fun getClient(properties: ConnectionProperties): DynamoDBOperation {
        return clients.getValue(properties)
    }
}
