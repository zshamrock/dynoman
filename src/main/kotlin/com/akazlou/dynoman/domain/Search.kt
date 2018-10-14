package com.akazlou.dynoman.domain

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec

abstract class Search(private var searchType: SearchType,
                      val table: String,
                      val index: String?,
                      protected val filters: List<QueryCondition>,
                      private val order: Order) {
    fun isAscOrdered(): Boolean {
        return order == Order.ASC
    }
}

class QuerySearch(table: String,
                  index: String?,
                  keys: List<QueryCondition>,
                  filters: List<QueryCondition>,
                  order: Order) : Search(SearchType.QUERY, table, index, filters, order) {
    private val hashKey = keys[0]
    private val rangeKey = keys.getOrNull(1)

    private fun getHashKeyName(): String {
        return hashKey.name
    }

    private fun getHashKeyType(): Type {
        return hashKey.type
    }

    private fun getHashKeyValue(): Any {
        val hashKeyValue = hashKey.values[0]
        return when (getHashKeyType()) {
            Type.NUMBER -> hashKeyValue.toLong()
            else -> hashKeyValue
        }
    }

    private fun getRangeKeyName(): String? {
        return rangeKey?.name
    }

    private fun getRangeKeyValues(): List<String> {
        return rangeKey?.values.orEmpty()
    }

    private fun getRangeKeyType(): Type {
        return rangeKey!!.type
    }

    private fun getRangeKeyOperator(): Operator {
        return rangeKey!!.operator
    }

    fun toQuerySpec(maxPageResultSize: Int = 0): QuerySpec {
        val spec = QuerySpec()
        spec.withHashKey(getHashKeyName(), getHashKeyValue())
        if (!getRangeKeyName().isNullOrEmpty() && getRangeKeyValues().isNotEmpty()) {
            val range = RangeKeyCondition(getRangeKeyName())
            val values: List<Any> = when (getRangeKeyType()) {
                Type.NUMBER -> getRangeKeyValues().map { it.toLong() }
                else -> getRangeKeyValues()
            }
            getRangeKeyOperator().apply(range, *values.toTypedArray())
            spec.withRangeKeyCondition(range)
        }
        spec.withQueryFilters(*(filters.map { it.toQueryFilter() }.toTypedArray()))
        spec.withScanIndexForward(isAscOrdered())
        if (maxPageResultSize != 0) {
            spec.withMaxPageSize(maxPageResultSize)
        }
        return spec
    }
}

class ScanSearch(table: String,
                 index: String?,
                 filters: List<QueryCondition>,
                 order: Order) :
        Search(SearchType.SCAN, table, index, filters, order)