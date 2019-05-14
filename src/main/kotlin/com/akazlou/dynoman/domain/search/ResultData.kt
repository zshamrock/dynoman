package com.akazlou.dynoman.domain.search

import com.amazonaws.services.dynamodbv2.model.KeySchemaElement

data class ResultData(val data: Map<String, Any?>, val hashKey: KeySchemaElement, val sortKey: KeySchemaElement?) {
    fun getKeys(): List<String> {
        if (data.isEmpty()) {
            return emptyList()
        }
        val primaryKeys = listOfNotNull(hashKey.attributeName, sortKey?.attributeName)
        return primaryKeys + (data.keys.toList() - primaryKeys).sorted()
    }

    fun getValue(attributeName: String): String {
        return data.getOrDefault(attributeName, "").toString()
    }

    fun getValues(): List<String> {
        return getValues(getKeys().toSet())
    }

    fun getValues(keys: Set<String>): List<String> {
        return keys.map { getValue(it) }
    }
}