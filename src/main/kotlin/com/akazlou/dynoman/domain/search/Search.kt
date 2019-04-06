package com.akazlou.dynoman.domain.search

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import kotlinx.serialization.Serializable

@Serializable
sealed class Search(private var searchType: SearchType,
                    open val table: String,
                    open val index: String?,
                    open val filters: List<QueryCondition>,
                    open val order: Order) {
    fun isAscOrdered(): Boolean {
        return order == Order.ASC
    }
}

@Serializable
class QuerySearch(override val table: String,
                  override val index: String?,
                  val keys: List<QueryCondition>,
                  override val filters: List<QueryCondition>,
                  override val order: Order) : Search(SearchType.QUERY, table, index, filters, order) {
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

    fun toQuerySpec(maxPageSize: Int = 0): QuerySpec {
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
        if (maxPageSize != 0) {
            spec.withMaxPageSize(maxPageSize)
        }
        return spec
    }
}

@Serializable
class ScanSearch(override val table: String,
                 override val index: String?,
                 override val filters: List<QueryCondition>) :
        Search(SearchType.SCAN, table, index, filters, Order.ASC) {
    fun toScanSpec(maxPageSize: Int = 0): ScanSpec {
        val spec = ScanSpec()
        spec.withScanFilters(*(filters.map { it.toScanFilter() }.toTypedArray()))
        if (maxPageSize != 0) {
            spec.withMaxPageSize(maxPageSize)
        }
        return spec
    }
}