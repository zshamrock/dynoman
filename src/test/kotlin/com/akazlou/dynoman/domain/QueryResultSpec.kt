package com.akazlou.dynoman.domain

import com.akazlou.dynoman.domain.search.QueryResult
import com.akazlou.dynoman.domain.search.SearchType
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Page
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import com.amazonaws.services.dynamodbv2.model.TableDescription
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row

class QueryResultSpec : StringSpec({
    "get current data range for empty page" {
        val qr = QueryResult(SearchType.QUERY, TableDescription(), EmptyPage())
        qr.getData(1)
        val (from, to) = qr.getCurrentDataRange(1)
        from shouldBe 0
        to shouldBe 0
    }

    "get current data range for non empty page" {
        val description = TableDescription()
        description.setKeySchema(listOf(KeySchemaElement("Id", KeyType.HASH)))
        val qr = QueryResult(
                SearchType.QUERY,
                description,
                PageList(listOf(
                        listOf(Item()),
                        listOf(Item()),
                        generateSequence { Item() }.take(52).toList()
                )))
        forall(
                row(1, 1, 100),
                row(2, 101, 200),
                row(3, 201, 252)
        ) { page, from, to ->
            qr.getData(page)
            qr.getCurrentDataRange(page) shouldBe Pair(from, to)
        }
    }
})

class EmptyPage : PageList(listOf(emptyList()))

open class PageList(private val pages: List<List<Item>>, private var page: Int = 1)
    : Page<Item, Any>(pages[page - 1], "") {
    override fun hasNextPage(): Boolean {
        return page < pages.size
    }

    override fun nextPage(): Page<Item, Any> {
        if (hasNextPage()) {
            return PageList(pages, page + 1)
        }
        throw NoSuchElementException()
    }
}


