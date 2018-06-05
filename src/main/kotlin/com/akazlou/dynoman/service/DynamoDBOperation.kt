package com.akazlou.dynoman.service

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec

class DynamoDBOperation(region: Regions, private val offline: Boolean) {
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

    fun scan(table: String): List<Map<String, Any?>> {
        val spec = ScanSpec()
                .withMaxResultSize(50)
        val result = getTable(table).scan(spec)
        return result.map { it.asMap() }
    }

    fun getTable(name: String): Table {
        return dynamodb!!.getTable(name)
    }
}