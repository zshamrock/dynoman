package com.akazlou.dynoman.domain

data class QueryResult(val operationType: OperationType,
                       val table: String,
                       val result: List<Map<String, Any?>>)