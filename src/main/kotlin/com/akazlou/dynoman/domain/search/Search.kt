package com.akazlou.dynoman.domain.search

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import tornadofx.*

sealed class Search(val type: SearchType,
                    val table: String,
                    val index: String?,
                    val filters: List<Condition>,
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
                  order: Order)
    : Search(SearchType.QUERY, table, index, filters, order) {

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
        return rangeKey?.type ?: Type.STRING
    }

    fun getRangeKeyOperator(): Operator {
        return rangeKey?.operator ?: Operator.EQ
    }

    fun toQuerySpec(maxPageSize: Int = 0, mapping: Map<String, String> = mapOf()): QuerySpec {
        val spec = QuerySpec()
        spec.withHashKey(getHashKeyName(), cast(getValue(getHashKeyValue(), mapping), getHashKeyType()))
        if (!getRangeKeyName().isNullOrEmpty() && getRangeKeyValues().isNotEmpty()) {
            val range = RangeKeyCondition(getRangeKeyName())
            val values: List<Any> = getRangeKeyValues().map { cast(getValue(it, mapping), getRangeKeyType()) }
            getRangeKeyOperator().apply(range, *values.toTypedArray())
            spec.withRangeKeyCondition(range)
        }
        // TODO: Map values for the filters as well
        spec.withQueryFilters(*(filters.map { it.toQueryFilter() }.toTypedArray()))
        spec.withScanIndexForward(isAscOrdered())
        if (maxPageSize != 0) {
            spec.withMaxPageSize(maxPageSize)
        }
        return spec
    }

    /**
     * Gets the actual value for the corresponding "value" if there is the matching key in the mapping map, when then
     * value acts as the key of the mapping map, and also used as the default value if the key is missing.
     */
    private fun getValue(value: String, mapping: Map<String, String>) = mapping.getOrDefault(value, value)

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
    fun toScanSpec(maxPageSize: Int = 0, mapping: Map<String, String> = mapOf()): ScanSpec {
        val spec = ScanSpec()
        // TODO: Map values for the filters as well
        spec.withScanFilters(*(filters.map { it.toScanFilter() }.toTypedArray()))
        if (maxPageSize != 0) {
            spec.withMaxPageSize(maxPageSize)
        }
        return spec
    }
}