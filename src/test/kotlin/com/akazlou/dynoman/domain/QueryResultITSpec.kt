package com.akazlou.dynoman.domain

import com.akazlou.dynoman.DynamoDBTestContainerListener
import com.akazlou.dynoman.IT
import com.akazlou.dynoman.service.DynamoDBOperation
import com.amazonaws.regions.Regions
import io.kotlintest.TestCaseConfig
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

// TODO: Might better use QueryResult to actually test the implementation
class QueryResultITSpec : StringSpec() {

    override fun listeners(): List<TestListener> = listOf(DynamoDBTestContainerListener)

    private val operation = DynamoDBOperation(ConnectionProperties(Regions.US_WEST_2, "", "", "default", "", true), false)

    override val defaultTestCaseConfig: TestCaseConfig = TestCaseConfig(tags = setOf(IT))

    init {
        "verify navigate over pages for the scan" {
            var result = operation.scan(ScanSearch("Table1", null, emptyList()))
            val table = operation.getTable("Table1")
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
                            listOf(QueryCondition("Id1", Type.STRING, Operator.EQ, listOf("Id1-50"))),
                            emptyList(),
                            Order.ASC))
            val table = operation.getTable("Table1")
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
                            listOf(QueryCondition("Num", Type.NUMBER, Operator.EQ, listOf("1"))),
                            emptyList(),
                            Order.ASC))

            val table = operation.getTable("Table1")
            val qr = QueryResult(SearchType.QUERY, table.description, result)

            result.hasNextPage() shouldBe true
            result.size() shouldBe 100

            qr.hasMoreData(1) shouldBe true
            qr.getData(1).size shouldBe 100

            result = result.nextPage()
            result.hasNextPage() shouldBe false
            result.size() shouldBe 49

            qr.hasMoreData(2) shouldBe false
            qr.getData(2).size shouldBe 49
        }

        "verify navigate over pages for the query on index with filters" {
            var result = operation.query(
                    QuerySearch(
                            "Table1",
                            "Table1Index2",
                            listOf(QueryCondition("Num", Type.NUMBER, Operator.EQ, listOf("1"))),
                            listOf(QueryCondition("Timestamp2", Type.NUMBER, Operator.GE, listOf("150"))),
                            Order.ASC))

            val table = operation.getTable("Table1")
            val qr = QueryResult(SearchType.QUERY, table.description, result)

            qr.hasMoreData(1) shouldBe true
            qr.getData(1).size shouldBe 0

            // TODO: Verify whether this is the expected behaviour on the real DynamoDB (doesn't sound like it is)
            result.hasNextPage() shouldBe true
            result.size() shouldBe 0


            result = result.nextPage()
            result.hasNextPage() shouldBe false
            result.size() shouldBe 0
        }
    }
}
