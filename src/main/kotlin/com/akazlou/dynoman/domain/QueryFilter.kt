package com.akazlou.dynoman.domain

// TODO: Add support for the BETWEEN, so value will become value...
data class QueryFilter(val name: String, val type: Type, val operator: Operator, val value: String?)