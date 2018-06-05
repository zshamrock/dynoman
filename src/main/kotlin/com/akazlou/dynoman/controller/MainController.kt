package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.DynamoDBTable
import com.akazlou.dynoman.service.DynamoDBOperation
import com.amazonaws.regions.Regions
import tornadofx.*

class MainController : Controller() {
    private val clients = with(mutableMapOf<Regions, DynamoDBOperation>()) {
        withDefault { region ->
            println("Using default value for the region $region")
            getOrPut(region, {
                DynamoDBOperation(
                        region,
                        System.getProperty("offline", "false")!!.toBoolean())
            })
        }
    }

    fun listTables(region: Regions): List<DynamoDBTable> {
        return getClient(region).listTables().map { DynamoDBTable(it) }
    }

    fun getClient(region: Regions): DynamoDBOperation {
        return clients.getValue(region)
    }
}
