package com.akazlou.dynoman.domain

import com.amazonaws.services.dynamodbv2.document.QueryFilter
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import java.util.Locale

data class QueryCondition(val name: String, val type: Type, val operator: Operator, val value: String)

enum class Type {
    STRING,
    NUMBER;

    override fun toString(): String {
        return name.toLowerCase(Locale.ROOT).capitalize()
    }
}

enum class Operator(val text: String) {
    EQ("="),
    LT("<"),
    LE("<="),
    GT(">"),
    GE(">="),
    NE("!="),
    BETWEEN("Between"),
    EXISTS("Exists"),
    NOT_EXISTS("Not exists"),
    CONTAINS("Contains"),
    NOT_CONTAINS("Not Contains"),
    BEGINS_WITH("Begins with");

    fun apply(attribute: String, type: Type, vararg values: String): QueryFilter {
        val value1: Any? = cast(0, type, values)
        val value2: Any? = cast(1, type, values)
        val filter = QueryFilter(attribute)
        return when (this) {
            EQ -> filter.eq(value1)
            LT -> filter.lt(value1)
            LE -> filter.le(value1)
            GT -> filter.gt(value1)
            GE -> filter.ge(value1)
            NE -> filter.ne(value1)
            BETWEEN -> filter.between(value1, value2)
            EXISTS -> filter.exists()
            NOT_EXISTS -> filter.notExist()
            CONTAINS -> filter.contains(value1)
            NOT_CONTAINS -> filter.notContains(value1)
            BEGINS_WITH -> filter.beginsWith(value1.toString())
        }
    }

    fun apply(range: RangeKeyCondition, value: Any) {
        when (this) {
            EQ -> range.eq(value)
            GT -> range.gt(value)
            GE -> range.ge(value)
            LT -> range.lt(value)
            LE -> range.le(value)
            else -> throw IllegalArgumentException("Unsupported operator for the range key condition: $this")
        }
    }

    private fun cast(index: Int, type: Type, values: Array<out String>): Any? {
        return if (values.size > index) {
            if (type == Type.STRING) values[index] else values[index].toLong()
        } else null
    }

    override fun toString(): String {
        return text
    }
}
