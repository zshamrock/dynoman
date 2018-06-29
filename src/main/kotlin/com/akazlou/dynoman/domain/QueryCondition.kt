package com.akazlou.dynoman.domain

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
    BEGINS_WITH("Begins with")
}
