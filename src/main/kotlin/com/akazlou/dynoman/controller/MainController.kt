package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.Config
import com.akazlou.dynoman.domain.ConnectionProperties
import com.akazlou.dynoman.domain.DynamoDBTable
import com.akazlou.dynoman.service.DynamoDBOperation
import com.amazonaws.services.dynamodbv2.model.TableDescription
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

    private val connectionProperties: ConnectionProperties = Config.getConnectionProperties(app.config)

    // TODO: Pass the actual operation instead of the connection properties
    fun listTables(properties: ConnectionProperties): List<DynamoDBTable> {
        return getClient(properties).listTables().map { DynamoDBTable(it) }
    }

    // TODO: Pass the actual operation instead of the connection properties
    fun getClient(properties: ConnectionProperties): DynamoDBOperation {
        return clients.getValue(properties)
    }

    // TODO: Pass the actual operation instead of the connection properties
    fun describeTable(table: DynamoDBTable, properties: ConnectionProperties): TableDescription {
        return getClient(properties).describeTable(table.tableName)
    }
}
