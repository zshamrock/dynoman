package com.akazlou.dynoman.domain.search

import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec
import tornadofx.*
import javax.json.JsonObject

sealed class Search(val type: SearchType,
                    val table: String,
                    val index: String?,
                    val conditions: List<Condition>,
                    val order: Order) : JsonModel {
    fun isAscOrdered(): Boolean {
        return order == Order.ASC
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("type", type.name)
            doToJSON(json)
        }
    }

    override fun updateModel(json: JsonObject) {
        with(json) {
            doUpdateModel(json)
        }
    }

    protected abstract fun doToJSON(json: JsonBuilder)

    protected abstract fun doUpdateModel(json: JsonObject)
}

class QuerySearch(table: String,
                  index: String?,
                  val hashKey: Condition,
                  val rangeKey: Condition?,
                  filters: List<Condition>,
                  order: Order) : Search(SearchType.QUERY, table, index, filters, order) {

    public fun getHashKeyName(): String {
        return hashKey.name
    }

    public fun getHashKeyType(): Type {
        return hashKey.type
    }

    public fun getHashKeyValue(): Any {
        val hashKeyValue = hashKey.values[0]
        return when (getHashKeyType()) {
            Type.NUMBER -> hashKeyValue.toLong()
            else -> hashKeyValue
        }
    }

    public fun getRangeKeyName(): String? {
        return rangeKey?.name
    }

    public fun getRangeKeyValues(): List<String> {
        return rangeKey?.values.orEmpty()
    }

    public fun getRangeKeyType(): Type {
        return rangeKey!!.type
    }

    public fun getRangeKeyOperator(): Operator {
        return rangeKey?.operator ?: Operator.EQ
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
        spec.withQueryFilters(*(conditions.map { it.toQueryFilter() }.toTypedArray()))
        spec.withScanIndexForward(isAscOrdered())
        if (maxPageSize != 0) {
            spec.withMaxPageSize(maxPageSize)
        }
        return spec
    }

    override fun doToJSON(json: JsonBuilder) {
        TODO("not implemented")
    }

    override fun doUpdateModel(json: JsonObject) {
        TODO("not implemented")
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

    override fun doToJSON(json: JsonBuilder) {
        TODO("not implemented")
    }

    override fun doUpdateModel(json: JsonObject) {
        TODO("not implemented")
    }
}