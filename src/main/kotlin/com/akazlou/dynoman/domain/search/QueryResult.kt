package com.akazlou.dynoman.domain.search

import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Page
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.TableDescription

data class QueryResult(val searchType: SearchType,
                       private val description: TableDescription,
                       private var page: Page<Item, out Any>,
                       private val maxPageSize: Int = QUERY_MAX_PAGE_RESULT_SIZE) {
    companion object {
        const val QUERY_MAX_PAGE_RESULT_SIZE = 100
        const val SCAN_MAX_PAGE_RESULT_SIZE = 100
    }

    private val data = mutableListOf<List<ResultData>>()
    /**
     * When doing search with filter the accumulated data could contain more than [maxPageSize], so here we keep
     * the reminder of this data to be used when fetching the data for the next [maxPageSize] page.
     */
    private var rem = listOf<ResultData>()

    fun getTable(): String {
        return description.tableName
    }

    // TODO: Unit test this new way of acc pages
    fun getData(pageNum: Int): List<ResultData> {
        if (pageNum > data.size) {
            if (pageNum != 1) {
                page = page.nextPage()
            }
            val acc = mutableListOf<ResultData>()
            acc.addAll(rem)
            if (acc.size < maxPageSize) {
                acc.addAll(fetchData(page))
            }
            while (acc.size < maxPageSize) {
                if (page.hasNextPage()) {
                    page = page.nextPage()
                } else {
                    break
                }
                acc.addAll(fetchData(page))
            }
            val chunked = if (acc.size > maxPageSize) {
                acc.chunked(maxPageSize)
            } else {
                listOf(acc)
            }
            data.add(chunked[0])
            rem = chunked.getOrNull(1).orEmpty()
        }
        return data[pageNum - 1]
    }

    private fun fetchData(page: Page<Item, out Any>): List<ResultData> {
        return page.map { ResultData(it.asMap(), getTableHashKey(), getTableSortKey(), getIndexes()) }
    }

    private fun getTableHashKey(): KeySchemaElement {
        return description.keySchema[0]
    }

    private fun getTableSortKey(): KeySchemaElement? {
        return description.keySchema.getOrNull(1)
    }

    private fun getIndexes(): List<Pair<KeySchemaElement, KeySchemaElement?>> {
        return description.globalSecondaryIndexes.orEmpty()
                .sortedBy { it.indexName }
                .map { it.keySchema[0] to it.keySchema.getOrNull(1) }
    }

    fun hasMoreData(pageNum: Int): Boolean {
        if (pageNum < data.size) {
            return true
        }
        return page.hasNextPage()
    }

    fun getCurrentDataRange(pageNum: Int): Pair<Int, Int> {
        val from = if (pageNum == 1 && data[0].isEmpty()) {
            0
        } else {
            (pageNum - 1) * maxPageSize + 1
        }
        val to = if (pageNum == data.size) {
            if (page.hasNextPage()) {
                pageNum * maxPageSize
            } else {
                (pageNum - 1) * maxPageSize + data[pageNum - 1].size
            }
        } else {
            pageNum * maxPageSize
        }
        return Pair(from, to)
    }
}