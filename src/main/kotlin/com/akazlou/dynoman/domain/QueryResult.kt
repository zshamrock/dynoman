package com.akazlou.dynoman.domain

import com.amazonaws.services.dynamodbv2.model.KeySchemaElement

data class QueryResult(val operationType: OperationType,
                       val table: String,
                       val hashKey: KeySchemaElement,
                       val sortKey: KeySchemaElement?,
                       val result: List<Map<String, Any?>>)