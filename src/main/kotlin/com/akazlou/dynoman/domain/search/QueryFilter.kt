package com.akazlou.dynoman.domain.search

// TODO: Could be QueryCondition used instead? I.e. more specifically merge QueryFilter and QueryCondition into one?
data class QueryFilter(val name: String, val type: Type, val operator: Operator, val values: List<String?>)