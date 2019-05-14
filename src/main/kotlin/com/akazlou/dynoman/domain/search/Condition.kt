package com.akazlou.dynoman.domain.search

import com.amazonaws.services.dynamodbv2.document.QueryFilter
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import com.amazonaws.services.dynamodbv2.document.ScanFilter
import com.amazonaws.services.dynamodbv2.document.internal.Filter
import java.util.Locale

data class Condition(val name: String, val type: Type, val operator: Operator, val values: List<String>) {
    fun toQueryFilter(): QueryFilter {
        return operator.toQueryFilter(name, type, *values.toTypedArray())
    }

    fun toScanFilter(): ScanFilter {
        return operator.toScanFilter(name, type, *values.toTypedArray())
    }

    companion object {
        fun hashKey(name: String, type: Type, value: String): Condition {
            return Condition(name, type, Operator.EQ, listOf(value))
        }
    }
}

enum class Type {
    STRING,
    NUMBER,
    BINARY;

    companion object {
        fun fromString(s: String): Type {
            return when (s) {
                "S" -> STRING
                "N" -> NUMBER
                else -> throw UnsupportedOperationException("Unsupported $s type")
            }
        }
    }

    override fun toString(): String {
        return name.toLowerCase(Locale.ROOT).capitalize()
    }
}

enum class Order {
    ASC,
    DESC;
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

    fun isBetween(): Boolean {
        return this == BETWEEN
    }

    fun isNoArg(): Boolean {
        return this == EXISTS || this == NOT_EXISTS
    }

    fun toQueryFilter(attribute: String, type: Type, vararg values: String): QueryFilter {
        val filter = QueryFilter(attribute)
        return toFilter(filter, type, values) as QueryFilter
    }

    fun toScanFilter(attribute: String, type: Type, vararg values: String): ScanFilter {
        val filter = ScanFilter(attribute)
        return toFilter(filter, type, values) as ScanFilter
    }

    // TODO: Filter is the internal class switch to use Condition-s instead
    private fun <T : Filter<T>> toFilter(filter: Filter<T>,
                                         type: Type,
                                         values: Array<out String>): Filter<T> {
        val value1: Any? = cast(0, type, values)
        val value2: Any? = cast(1, type, values)
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

    fun apply(range: RangeKeyCondition, vararg values: Any) {
        when (this) {
            EQ -> range.eq(values[0])
            GT -> range.gt(values[0])
            GE -> range.ge(values[0])
            LT -> range.lt(values[0])
            LE -> range.le(values[0])
            BETWEEN -> range.between(values[0], values[1])
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
