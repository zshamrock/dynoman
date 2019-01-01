package com.akazlou.dynoman.domain

import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec

class ConnectionPropertiesSpec : StringSpec({
    "connection to the remote instance" {
        val properties = ConnectionProperties(Regions.US_WEST_2, "", "", "dynoman", false)
        val client = properties.buildAmazonDynamoDBClient() as AmazonDynamoDBClient
        client shouldNotBe null
    }

    "connection to the local instance" {
        val properties = ConnectionProperties(Regions.US_WEST_2, "", "", "default", true)
        val client = properties.buildAmazonDynamoDBClient() as AmazonDynamoDBClient
        client shouldNotBe null
    }
})