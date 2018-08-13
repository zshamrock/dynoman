package com.akazlou.dynoman.domain

import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.TableDescription

data class QueryResult(val operationType: OperationType,
                       val description: TableDescription,
                       val result: List<Map<String, Any?>>) {
    fun getTable(): String {
        return description.tableName
    }

    fun getTableHashKey(): KeySchemaElement {
        return description.keySchema[0]
    }

    fun getTableSortKey(): KeySchemaElement? {
        return description.keySchema.getOrNull(1)
    }
}