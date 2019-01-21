package com.akazlou.dynoman.domain

import com.akazlou.dynoman.DynamoDBTestContainerListener
import com.akazlou.dynoman.IT
import com.akazlou.dynoman.service.DynamoDBOperation
import com.amazonaws.regions.Regions
import io.kotlintest.TestCaseConfig
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class QueryResultITSpec : StringSpec() {

    override fun listeners(): List<TestListener> = listOf(DynamoDBTestContainerListener)

    private val operation = DynamoDBOperation(ConnectionProperties(Regions.US_WEST_2, "", "", "default", "", true), false)

    override val defaultTestCaseConfig: TestCaseConfig = TestCaseConfig(tags = setOf(IT))

    init {
        "verify navigate over pages for the scan" {
            var result = operation.scan(ScanSearch("Table1", null, emptyList()))

            result.hasNextPage() shouldBe true
            result.size() shouldBe 100

            result = result.nextPage()
            result.hasNextPage() shouldBe true
            result.size() shouldBe 100

            result = result.nextPage()
            result.hasNextPage() shouldBe false
            result.size() shouldBe 0
        }

        "verify navigate over pages for the query" {
            var result = operation.query(
                    QuerySearch(
                            "Table1",
                            null,
                            listOf(QueryCondition("Id1", Type.STRING, Operator.EQ, listOf("Id1-50"))),
                            emptyList(),
                            Order.ASC))

            result.hasNextPage() shouldBe false
            result.size() shouldBe 1
        }
    }
}
