package com.akazlou.dynoman.domain.search

import io.kotlintest.specs.StringSpec


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
})