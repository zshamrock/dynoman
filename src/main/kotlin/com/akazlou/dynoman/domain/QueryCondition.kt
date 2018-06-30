package com.akazlou.dynoman.domain

import com.amazonaws.services.dynamodbv2.document.QueryFilter

data class QueryCondition(val name: String, val type: Type, val operator: Operator, val value: String)

enum class Type {
    STRING,
    NUMBER
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

    fun <T> apply(attribute: String, type: Type, vararg values: T): QueryFilter {
        return when (this) {
            EQ -> QueryFilter(attribute).eq(values[0])
            else -> throw IllegalArgumentException("Unsupported operator $this")
        }
    }
}
