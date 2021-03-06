package com.akazlou.dynoman.domain

import com.akazlou.dynoman.DynamoDBTestContainerListener
import com.akazlou.dynoman.IT
import com.akazlou.dynoman.domain.search.Condition
import com.akazlou.dynoman.domain.search.Operator
import com.akazlou.dynoman.domain.search.Order
import com.akazlou.dynoman.domain.search.QueryResult
import com.akazlou.dynoman.domain.search.QuerySearch
import com.akazlou.dynoman.domain.search.ScanSearch
import com.akazlou.dynoman.domain.search.SearchType
import com.akazlou.dynoman.domain.search.Type
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
            val table = operation.getTable("Table1")
            table.describe()
            val qr = QueryResult(SearchType.SCAN, table.description, result)
            qr.hasMoreData(1) shouldBe true
            qr.getData(1).size shouldBe 100

            qr.hasMoreData(2) shouldBe true
            qr.getData(2).size shouldBe 100

            qr.hasMoreData(3) shouldBe false

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
                            Condition("Id1", Type.STRING, Operator.EQ, listOf("Id1-50")),
                            null,
                            emptyList(),
                            Order.ASC))
            val table = operation.getTable("Table1")
            table.describe()
            val qr = QueryResult(SearchType.QUERY, table.description, result)

            qr.hasMoreData(1) shouldBe false
            qr.getData(1).size shouldBe 1

            result.hasNextPage() shouldBe false
            result.size() shouldBe 1
        }

        "verify navigate over pages for the query on index" {
            var result = operation.query(
                    QuerySearch(
                            "Table1",
                            "Table1Index2",
                            Condition("Num", Type.NUMBER, Operator.EQ, listOf("1")),
                            null,
                            emptyList(),
                            Order.ASC))

            val table = operation.getTable("Table1")
            table.describe()
            val qr = QueryResult(SearchType.QUERY, table.description, result)

            result.hasNextPage() shouldBe true
            result.size() shouldBe 100

            qr.getData(1).size shouldBe 100
            qr.hasMoreData(1) shouldBe true

            result = result.nextPage()
            result.hasNextPage() shouldBe false
            result.size() shouldBe 49

            qr.getData(2).size shouldBe 49
            qr.hasMoreData(2) shouldBe false
        }

        "verify navigate over pages for the query on index with filters" {
            var result = operation.query(
                    QuerySearch(
                            "Table1",
                            "Table1Index2",
                            Condition("Num", Type.NUMBER, Operator.EQ, listOf("1")),
                            null,
                            listOf(Condition("Timestamp2", Type.NUMBER, Operator.GE, listOf("150"))),
                            Order.ASC))

            val table = operation.getTable("Table1")
            table.describe()
            val qr = QueryResult(SearchType.QUERY, table.description, result)

            qr.getData(1).size shouldBe 0
            qr.hasMoreData(1) shouldBe false

            // TODO: Verify whether this is the expected behaviour on the real DynamoDB (doesn't sound like it is)
            result.hasNextPage() shouldBe true
            result.size() shouldBe 0

            result = result.nextPage()
            result.hasNextPage() shouldBe false
            result.size() shouldBe 0
        }
    }
}
