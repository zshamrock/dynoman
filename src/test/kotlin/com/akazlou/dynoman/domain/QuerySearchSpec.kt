package com.akazlou.dynoman.domain

import io.kotlintest.specs.StringSpec

class QuerySearchSpec: StringSpec({
    "build query spec" {
        val search = QuerySearch(
                "TableA",
                null,
                listOf(QueryCondition("Id", Type.NUMBER, Operator.EQ, listOf(""))),
                listOf(),
                Order.DESC)
    }
})