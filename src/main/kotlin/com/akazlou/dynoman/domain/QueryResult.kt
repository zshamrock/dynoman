package com.akazlou.dynoman.domain

data class QueryResult(private val operationType: OperationType,
                       private val table: String,
                       private val result: List<Map<String, Any?>>)