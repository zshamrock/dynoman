package com.akazlou.dynoman.domain

data class QueryFilter(val name: String, val type: Type, val operator: Operator, val values: List<String?>)