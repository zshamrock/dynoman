package com.akazlou.dynoman.domain

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient

/**
 * Connection properties used to build {@link AmazonDynamoDBClient}.
 */
data class ConnectionProperties(val region: Regions,
                                val key: String,
                                val secret: String,
                                val profile: String,
                                val local: Boolean) {
    companion object {
        private const val LOCAL_ENDPOINT = "http://localhost:8000"
    }

    fun buildAmazonDynamoDBClient(): AmazonDynamoDB {
        val builder = AmazonDynamoDBClient.builder().withCredentials(buildCredentials())
        if (local) {
            builder.withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration(LOCAL_ENDPOINT, region.getName()))
        } else {
            builder.withRegion(region)
        }
        return builder.build()
    }

    private fun buildCredentials(): AWSCredentialsProvider {
        return if (key.isNotBlank() && secret.isNotBlank()) {
            AWSStaticCredentialsProvider(BasicAWSCredentials(key, secret))
        } else if (profile.isNotBlank()) {
            ProfileCredentialsProvider(profile)
        } else {
            DefaultAWSCredentialsProviderChain()
        }
    }
}