package com.akazlou.dynoman.view

import com.amazonaws.services.dynamodbv2.model.KeySchemaElement

data class QueryType(val name: String, val keySchema: List<KeySchemaElement>, val isIndex: Boolean) {
    val hashKey: KeySchemaElement = keySchema[0]
    val sortKey: KeySchemaElement? = keySchema.getOrNull(1)

    override fun toString(): String {
        return (if (isIndex) "[Index]" else "[Table]") + " $name: ${joinKeySchema(keySchema)}"
    }

    private fun joinKeySchema(keySchema: List<KeySchemaElement>): String {
        return keySchema.joinToString { it.attributeName }
    }
}