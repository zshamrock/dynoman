package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.DynamoDBTable
import com.akazlou.dynoman.service.DynamoDBOperation
import com.amazonaws.regions.Regions
import tornadofx.*

class MainController : Controller() {
    private val clients = with(mutableMapOf<Pair<Regions, Boolean>, DynamoDBOperation>()) {
        withDefault { pair ->
            val region = pair.first
            val local = pair.second
            println("Using default value for the region $region (local: $local)")
            getOrPut(pair) {
                DynamoDBOperation(
                        region,
                        local,
                        System.getProperty("offline", "false")!!.toBoolean())
            }
        }
    }

    fun listTables(region: Regions, local: Boolean): List<DynamoDBTable> {
        return getClient(region, local).listTables().map { DynamoDBTable(it) }
    }

    fun getClient(region: Regions, local: Boolean): DynamoDBOperation {
        return clients.getValue(Pair(region, local))
    }
}
