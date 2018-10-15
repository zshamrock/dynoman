package com.akazlou.dynoman.domain

import com.amazonaws.services.dynamodbv2.document.KeyAttribute
import io.kotlintest.matchers.beEmpty
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class QuerySearchSpec : StringSpec({
    "build query spec" {
        val search = QuerySearch(
                "TableA",
                null,
                listOf(QueryCondition("Id", Type.NUMBER, Operator.EQ, listOf("1"))),
                listOf(),
                Order.DESC)
        val spec = search.toQuerySpec(0)
        spec.hashKey shouldBe KeyAttribute("Id", 1L)
        spec.rangeKeyCondition shouldBe null
        spec.queryFilters should beEmpty()
        spec.isScanIndexForward.shouldBeFalse()
        spec.maxPageSize shouldBe null
    }
})