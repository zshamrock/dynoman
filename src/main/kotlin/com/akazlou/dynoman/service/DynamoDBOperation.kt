package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.QuerySearch
import com.akazlou.dynoman.domain.ScanSearch
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Page
import com.amazonaws.services.dynamodbv2.document.QueryOutcome
import com.amazonaws.services.dynamodbv2.document.ScanOutcome
import com.amazonaws.services.dynamodbv2.document.Table
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class DynamoDBOperation(region: Regions, private val offline: Boolean) {
    companion object {
        const val MAX_PAGE_RESULT_SIZE = 100
    }

    private val dynamodb: DynamoDB?

    init {
        dynamodb = if (offline) {
            null
        } else {
            val amazonDynamoDB = AmazonDynamoDBClient.builder()
                    .withCredentials(DefaultAWSCredentialsProviderChain())
                    .withRegion(region)
                    .build()
            DynamoDB(amazonDynamoDB)
        }
    }

    fun listTables(): List<String> {
        return if (offline) emptyList() else dynamodb!!.listTables().map { it.tableName }
    }

    fun scan(search: ScanSearch): Page<Item, ScanOutcome> {
        val spec = search.toScanSpec(MAX_PAGE_RESULT_SIZE)
        val table = getTable(search.table)
        val result = if (search.index != null) {
            table.getIndex(search.index).scan(spec)
        } else {
            table.scan(spec)
        }
        val page = result.firstPage()
        println("Size of the page is ${page.size()}")
        println("has next page ${page.hasNextPage()}")
        return page
    }

    fun getTable(name: String): Table {
        return dynamodb!!.getTable(name)
    }

    fun query(search: QuerySearch): Page<Item, QueryOutcome> {
        val spec = search.toQuerySpec(MAX_PAGE_RESULT_SIZE)
        println("Run query")
        var page: Page<Item, QueryOutcome>? = null
        val runTime = measureTimeMillis {
            val table = getTable(search.table)
            val result = if (search.index != null) {
                table.getIndex(search.index).query(spec)
            } else {
                table.query(spec)
            }
            page = result.firstPage()
        }
        println("Size of the page is ${page?.size()}")
        println("Query run $runTime ms, and ${TimeUnit.MILLISECONDS.toSeconds(runTime)} secs")
        return page!!
    }
}