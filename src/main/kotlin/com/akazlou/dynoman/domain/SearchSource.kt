package com.akazlou.dynoman.domain

import com.amazonaws.services.dynamodbv2.model.KeySchemaElement

data class SearchSource(val name: String, val keySchema: List<KeySchemaElement>, val isIndex: Boolean) :
        Comparable<SearchSource> {

    val hashKey: KeySchemaElement = keySchema[0]
    val sortKey: KeySchemaElement? = keySchema.getOrNull(1)

    override fun toString(): String {
        return (if (isIndex) "[Index]" else "[Table]") + " $name: ${joinKeySchema(keySchema)}"
    }

    private fun joinKeySchema(keySchema: List<KeySchemaElement>): String {
        return keySchema.joinToString { it.attributeName }
    }

    override fun compareTo(other: SearchSource): Int {
        if (isIndex && other.isIndex) {
            return name.compareTo(other.name)
        }
        return if (!isIndex) -1 else 1
    }
}