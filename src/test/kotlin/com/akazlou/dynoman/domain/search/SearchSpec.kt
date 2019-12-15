package com.akazlou.dynoman.domain.search

import com.amazonaws.services.dynamodbv2.document.KeyAttribute
import io.kotlintest.data.forall
import io.kotlintest.matchers.beEmpty
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row


class SearchSpec : StringSpec({
    "serialize query search" {
        val search = QuerySearch(
                "Table A",
                "Index A",
                Condition("Id", Type.NUMBER, Operator.EQ, listOf("10")),
                null,
                listOf(Condition("Timestamp", Type.NUMBER, Operator.BETWEEN, listOf("100", "200")),
                        Condition("Name", Type.STRING, Operator.BEGINS_WITH, listOf("Test"))),
                Order.ASC)
    }

    "build query spec" {
        val search = QuerySearch(
                "TableA",
                null,
                Condition.hashKey("Id", Type.NUMBER, "1"),
                null,
                emptyList(),
                Order.DESC)
        val spec = search.toQuerySpec(0)
        spec.hashKey shouldBe KeyAttribute("Id", 1.0)
        spec.rangeKeyCondition shouldBe null
        spec.queryFilters should beEmpty()
        spec.isScanIndexForward shouldBe false
        spec.maxPageSize shouldBe null
    }

    "get all attribute values from query search" {
        forall(
                row(QuerySearch(
                        "TableA",
                        null,
                        Condition.hashKey("Id", Type.NUMBER, "1"),
                        null,
                        emptyList(),
                        Order.DESC), listOf("1")),
                row(QuerySearch(
                        "TableA",
                        null,
                        Condition.hashKey("Id", Type.NUMBER, "1"),
                        null,
                        listOf(Condition("X", Type.STRING, Operator.EQ, listOf("A"))),
                        Order.DESC), listOf("1", "A")),
                row(QuerySearch(
                        "TableA",
                        null,
                        Condition.hashKey("Id", Type.NUMBER, "1"),
                        null,
                        listOf(Condition("X", Type.STRING, Operator.EQ, listOf("A")),
                                Condition("Y", Type.BOOLEAN, Operator.EQ, listOf("true"))),
                        Order.DESC), listOf("1", "A", "true")),
                row(QuerySearch(
                        "TableA",
                        null,
                        Condition.hashKey("Id", Type.NUMBER, "1"),
                        Condition("Timestamp", Type.NUMBER, Operator.EQ, listOf("2")),
                        emptyList(),
                        Order.DESC), listOf("1", "2")),
                row(QuerySearch(
                        "TableA",
                        null,
                        Condition.hashKey("Id", Type.NUMBER, "1"),
                        Condition("Timestamp", Type.NUMBER, Operator.EQ, listOf("2")),
                        listOf(Condition("X", Type.STRING, Operator.EQ, listOf("A"))),
                        Order.DESC), listOf("1", "2", "A")),
                row(QuerySearch(
                        "TableA",
                        null,
                        Condition.hashKey("Id", Type.NUMBER, "1"),
                        Condition("Timestamp", Type.NUMBER, Operator.EQ, listOf("2")),
                        listOf(Condition("X", Type.STRING, Operator.EQ, listOf("A")),
                                Condition("Y", Type.BOOLEAN, Operator.EQ, listOf("true"))),
                        Order.DESC), listOf("1", "2", "A", "true"))
        ) { search, names ->
            search.getAllValues() shouldBe names
        }
    }

    "get all attribute names from scan search" {
        forall(
                row(ScanSearch(
                        "TableA",
                        null,
                        emptyList()), emptyList()),
                row(ScanSearch(
                        "TableA",
                        null,
                        listOf(Condition("X", Type.NUMBER, Operator.EQ, listOf("1")))), listOf("1")),
                row(ScanSearch(
                        "TableA",
                        null,
                        listOf(Condition("X", Type.NUMBER, Operator.EQ, listOf("1")),
                                Condition("Y", Type.STRING, Operator.BEGINS_WITH, listOf("A")))), listOf("1", "A"))
        ) { search, names ->
            search.getAllValues() shouldBe names
        }
    }
})