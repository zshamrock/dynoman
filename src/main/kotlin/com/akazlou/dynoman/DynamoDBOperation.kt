package com.akazlou.dynoman

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB

class DynamoDBOperation(endpoint: String, region: String) {
    private val dynamodb: DynamoDB

    init {
        val amazonDynamoDB = AmazonDynamoDBClient.builder()
                .withCredentials(DefaultAWSCredentialsProviderChain())
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .build()
        dynamodb = DynamoDB(amazonDynamoDB)
    }

    fun listTables(): List<String> {
        return dynamodb.listTables().map { it.tableName }
    }
}