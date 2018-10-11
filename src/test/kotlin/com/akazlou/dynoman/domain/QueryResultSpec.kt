package com.akazlou.dynoman.domain

import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Page
import com.amazonaws.services.dynamodbv2.model.TableDescription
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class QueryResultSpec : StringSpec({
    "get current data range for empty page" {
        val qr = QueryResult(OperationType.QUERY, TableDescription(), EmptyPage())
        qr.getData(1)
        val (from, to) = qr.getCurrentDataRange(1)
        from shouldBe 0
        to shouldBe 0
    }
})

class EmptyPage : Page<Item, Any>(emptyList(), "") {
    override fun hasNextPage(): Boolean {
        return false
    }

    override fun nextPage(): Page<Item, Any> {
        throw NoSuchElementException()
    }

}


