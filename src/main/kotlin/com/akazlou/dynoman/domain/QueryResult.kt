package com.akazlou.dynoman.domain

import com.akazlou.dynoman.view.ResultData
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Page
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.TableDescription

data class QueryResult(val searchType: SearchType,
                       private val description: TableDescription,
                       private var page: Page<Item, out Any>,
                       private val maxPageSize: Int = MAX_PAGE_RESULT_SIZE) {
    companion object {
        const val MAX_PAGE_RESULT_SIZE = 100
    }

    private var data = mutableListOf<List<ResultData>>()

    fun getTable(): String {
        return description.tableName
    }

    fun getData(pageNum: Int): List<ResultData> {
        if (pageNum > data.size) {
            if (pageNum != 1) {
                page = page.nextPage()
            }
            data.add(fetchData(page))
        }
        return data[pageNum - 1]
    }

    private fun fetchData(page: Page<Item, out Any>): List<ResultData> {
        return page.map { ResultData(it.asMap(), getTableHashKey(), getTableSortKey()) }
    }

    private fun getTableHashKey(): KeySchemaElement {
        return description.keySchema[0]
    }

    private fun getTableSortKey(): KeySchemaElement? {
        return description.keySchema.getOrNull(1)
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