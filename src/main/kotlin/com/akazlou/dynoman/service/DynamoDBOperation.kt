package com.akazlou.dynoman.service

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table

class DynamoDBOperation(endpoint: String, region: String, private val offline: Boolean) {
    private val dynamodb: DynamoDB?

    init {
        dynamodb = if (offline) {
            null
        } else {
            val amazonDynamoDB = AmazonDynamoDBClient.builder()
                    .withCredentials(DefaultAWSCredentialsProviderChain())
                    .withRegion(Regions.US_WEST_2)
                    .build()
            DynamoDB(amazonDynamoDB)
        }
    }

    fun listTables(): List<String> {
        return if (offline) emptyList() else dynamodb!!.listTables().map { it.tableName }
    }

    fun getTable(name: String): Table {
        return dynamodb!!.getTable(name)
    }
}