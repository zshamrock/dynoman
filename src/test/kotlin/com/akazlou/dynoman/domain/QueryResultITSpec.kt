package com.akazlou.dynoman.domain

import com.akazlou.dynoman.DynamoDBTestContainerListener
import com.akazlou.dynoman.IT
import com.akazlou.dynoman.service.DynamoDBOperation
import com.amazonaws.regions.Regions
import io.kotlintest.TestCaseConfig
import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.StringSpec

class QueryResultITSpec : StringSpec() {

    override fun listeners(): List<TestListener> = listOf(DynamoDBTestContainerListener)

    private val operation = DynamoDBOperation(ConnectionProperties(Regions.US_WEST_2, "", "", "default", "", true), false)

    override val defaultTestCaseConfig: TestCaseConfig = TestCaseConfig(tags = setOf(IT))

    init {
        "verify navigate over pages for the scan" {
        }
    }
}
