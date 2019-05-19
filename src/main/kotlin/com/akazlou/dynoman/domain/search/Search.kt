package com.akazlou.dynoman.domain.search

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import tornadofx.*

sealed class Search(val type: SearchType,
                    val table: String,
                    val index: String?,
                    val conditions: List<Condition>,
                    val order: Order) : JsonModel {
    fun isAscOrdered(): Boolean {
        return order == Order.ASC
    }
}

class QuerySearch(table: String,
                  index: String?,
                  private val hashKey: Condition,
                  private val rangeKey: Condition?,
                  filters: List<Condition>,
                  order: Order) : Search(SearchType.QUERY, table, index, filters, order) {

    fun getHashKeyName(): String {
        return hashKey.name
    }

    fun getHashKeyType(): Type {
        return hashKey.type
    }

    fun getHashKeyValue(): String {
        return hashKey.values[0]
    }

    fun getRangeKeyName(): String? {
        return rangeKey?.name
    }

    fun getRangeKeyValues(): List<String> {
        return rangeKey?.values.orEmpty()
    }

    fun getRangeKeyType(): Type {
        return rangeKey!!.type
    }

    fun getRangeKeyOperator(): Operator {
        return rangeKey?.operator ?: Operator.EQ
    }

    fun toQuerySpec(maxPageSize: Int = 0): QuerySpec {
        val spec = QuerySpec()
        spec.withHashKey(getHashKeyName(), cast(getHashKeyValue(), getHashKeyType()))
        if (!getRangeKeyName().isNullOrEmpty() && getRangeKeyValues().isNotEmpty()) {
            val range = RangeKeyCondition(getRangeKeyName())
            val values: List<Any> = getRangeKeyValues().map { cast(it, getRangeKeyType()) }
            getRangeKeyOperator().apply(range, *values.toTypedArray())
            spec.withRangeKeyCondition(range)
        }
        spec.withQueryFilters(*(conditions.map { it.toQueryFilter() }.toTypedArray()))
        spec.withScanIndexForward(isAscOrdered())
        if (maxPageSize != 0) {
            spec.withMaxPageSize(maxPageSize)
        }
        return spec
    }

    private fun cast(value: String, type: Type): Any {
        return when (type) {
            Type.NUMBER -> value.toLong()
            else -> value
        }
    }
}

class ScanSearch(table: String,
                 index: String?,
                 filters: List<Condition>) :
        Search(SearchType.SCAN, table, index, filters, Order.ASC) {
    fun toScanSpec(maxPageSize: Int = 0): ScanSpec {
        val spec = ScanSpec()
        spec.withScanFilters(*(conditions.map { it.toScanFilter() }.toTypedArray()))
        if (maxPageSize != 0) {
            spec.withMaxPageSize(maxPageSize)
        }
        return spec
    }
}