package com.akazlou.dynoman.domain.search

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import tornadofx.*

// XXX: Might consider make the "mapping" part of the search instead of using "expand" approach
sealed class Search(val type: SearchType,
                    val table: String,
                    val index: String?,
                    val filters: List<Condition>,
                    val order: Order,
                    val name: String = "") : JsonModel {
    companion object {
        const val USER_INPUT_MARK = "?"

        fun requiresUserInput(value: String): Boolean {
            return value.startsWith(Search.USER_INPUT_MARK)
        }
    }

    fun isAscOrdered(): Boolean {
        return order == Order.ASC
    }

    /**
     * Search might contain values, which are not real values, but rather reference/alias/placeholder pointing to where
     * the actual value could be obtained.
     *
     * This method expands those references with the actual values, and return expanded search instance. *Current
     * instance is not modified*.
     *
     * Possible references are:
     * - environment variables
     * - input from user
     * - managed environment/context variable
     * - master table column's name (in the case of the foreign query)
     */
    fun expand(mapping: Map<String, String>): Search {
        return if (mapping.isEmpty()) {
            this
        } else {
            doExpand(mapping)
        }
    }

    protected abstract fun doExpand(mapping: Map<String, String>): Search

    protected fun expandFilters(mapping: Map<String, String>): List<Condition> {
        return filters.map { it.expand(mapping) }
    }

    fun getAllValues(): List<String> {
        return doGetAllValues() + getAllFilterValues()
    }

    protected abstract fun doGetAllValues(): List<String>

    private fun getAllFilterValues(): List<String> {
        return filters.flatMap { it.values }
    }
}

class QuerySearch(table: String,
                  index: String?,
                  val hashKey: Condition,
                  val rangeKey: Condition?,
                  filters: List<Condition>,
                  order: Order,
                  name: String = "")
    : Search(SearchType.QUERY, table, index, filters, order, name) {
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

    fun toQuerySpec(maxPageSize: Int = 0): QuerySpec {
        val spec = QuerySpec()
        spec.withHashKey(getHashKeyName(), cast(getHashKeyValue(), getHashKeyType()))
        if (!getRangeKeyName().isNullOrEmpty() && getRangeKeyValues().isNotEmpty()) {
            val range = RangeKeyCondition(getRangeKeyName())
            val values: List<Any> = getRangeKeyValues().map { cast(it, getRangeKeyType()) }
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

    override fun doExpand(mapping: Map<String, String>): Search {
        return QuerySearch(
                table, index, hashKey.expand(mapping), rangeKey?.expand(mapping), expandFilters(mapping), order)
    }

    override fun doGetAllValues(): List<String> {
        return if (rangeKey == null) {
            listOf(hashKey.values.first())
        } else {
            listOf(hashKey.values.first(), *rangeKey.values.toTypedArray())
        }
    }

    private fun cast(value: String, type: Type): Any {
        return when (type) {
            Type.BOOLEAN -> value.toBoolean()
            Type.NUMBER -> value.toDouble()
            else -> value
        }
    }
}

class ScanSearch(table: String,
                 index: String?,
                 filters: List<Condition>,
                 name: String = "") :
        Search(SearchType.SCAN, table, index, filters, Order.ASC, name) {
    fun toScanSpec(maxPageSize: Int = 0): ScanSpec {
        val spec = ScanSpec()
        spec.withScanFilters(*(filters.map { it.toScanFilter() }.toTypedArray()))
        if (maxPageSize != 0) {
            spec.withMaxPageSize(maxPageSize)
        }
        return spec
    }

    override fun doExpand(mapping: Map<String, String>): Search {
        return ScanSearch(table, index, expandFilters(mapping))
    }

    override fun doGetAllValues(): List<String> {
        return emptyList()
    }
}