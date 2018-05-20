package com.akazlou.dynoman

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table

class DynamoDBOperation(endpoint: String, region: String) {
    private val dynamodb: DynamoDB

    init {
        val amazonDynamoDB = AmazonDynamoDBClient.builder()
                .withCredentials(DefaultAWSCredentialsProviderChain())
                .withRegion(Regions.US_WEST_2)
                .build()
        dynamodb = DynamoDB(amazonDynamoDB)
    }

    fun listTables(): List<String> {
        return dynamodb.listTables().map { it.tableName }
    }

    fun getTable(name: String): Table {
        return dynamodb.getTable(name)
    }
}