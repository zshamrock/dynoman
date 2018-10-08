package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.Operator
import com.akazlou.dynoman.domain.QueryCondition
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Page
import com.amazonaws.services.dynamodbv2.document.QueryOutcome
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import com.amazonaws.services.dynamodbv2.document.ScanOutcome
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
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

    fun scan(table: String): Page<Item, ScanOutcome> {
        val spec = ScanSpec()
                .withMaxPageSize(MAX_PAGE_RESULT_SIZE)
        val result = getTable(table).scan(spec)
        return result.firstPage()
    }

    fun getTable(name: String): Table {
        return dynamodb!!.getTable(name)
    }

    fun query(tableName: String,
              indexName: String?,
              hashKeyName: String,
              hashKeyType: String,
              hashKeyValue: String,
              sortKeyName: String?,
              sortKeyType: String?,
              sortKeyOperation: Operator,
              sortKeyValues: List<String>,
              sortOrder: String,
              conditions: List<QueryCondition>): Page<Item, QueryOutcome> {
        val spec = QuerySpec()
        spec.withHashKey(hashKeyName, when (hashKeyType) {
            "N" -> hashKeyValue.toLong()
            else -> hashKeyValue
        })
        if (!sortKeyName.isNullOrEmpty() && sortKeyValues.isNotEmpty()) {
            val range = RangeKeyCondition(sortKeyName)
            val values: List<Any> = when (sortKeyType) {
                "N" -> sortKeyValues.map { it.toLong() }
                else -> sortKeyValues
            }
            sortKeyOperation.apply(range, *values.toTypedArray())
            spec.withRangeKeyCondition(range)
        }
        spec.withQueryFilters(*(conditions.map { it.toQueryFilter() }.toTypedArray()))
        spec.withScanIndexForward(sortOrder == "asc")
        spec.withMaxPageSize(MAX_PAGE_RESULT_SIZE)
        println("Run query")
        var page: Page<Item, QueryOutcome>? = null
        val runTime = measureTimeMillis {
            val table = getTable(tableName)
            val result = if (indexName != null) {
                table.getIndex(indexName).query(spec)
            } else {
                table.query(spec)
            }
            page = result.firstPage()
        }
        println("Query run $runTime ms, and ${TimeUnit.MILLISECONDS.toSeconds(runTime)} secs")
        return page!!
    }
}