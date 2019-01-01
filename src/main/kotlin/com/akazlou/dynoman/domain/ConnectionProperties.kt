package com.akazlou.dynoman.domain

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient

/**
 * Connection properties used to build {@link AmazonDynamoDBClient}.
 */
data class ConnectionProperties(val region: Regions, val profile: String, val local: Boolean) {
    companion object {
        private const val LOCAL_ENDPOINT = "http://localhost:8000"
    }

    fun buildAmazonDynamoDBClient(): AmazonDynamoDB {
        val builder = AmazonDynamoDBClient.builder()
                .withCredentials(if (profile.isBlank()) {
                    DefaultAWSCredentialsProviderChain()
                } else {
                    ProfileCredentialsProvider(profile)
                })
        if (local) {
            builder.withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration(LOCAL_ENDPOINT, region.getName()))
        } else {
            builder.withRegion(region)
        }
        return builder.build()
    }
}