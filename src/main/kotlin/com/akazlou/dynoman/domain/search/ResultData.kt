package com.akazlou.dynoman.domain.search

import com.amazonaws.services.dynamodbv2.model.KeySchemaElement

data class ResultData(val data: Map<String, Any?>, val hashKey: KeySchemaElement, val sortKey: KeySchemaElement?) {
    enum class DataType {
        LIST,
        MAP,
        SET,
        SCALAR,
        NULL;

        fun isCollection(): Boolean {
            return this == LIST || this == MAP || this == SET;
        }

        fun isMap(): Boolean {
            return this == MAP
        }
    }

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

    fun getRawValue(attributeName: String): Any? {
        return data[attributeName]
    }

    fun asMap(): Map<String, String> = data.keys.associateWith { getValue(it) }

    fun getDataType(attributeName: String): DataType {
        val value = data[attributeName] ?: return DataType.NULL
        return when (value) {
            is Map<*, *> -> DataType.MAP
            is Set<*> -> DataType.SET
            is List<*> -> DataType.LIST
            else -> DataType.SCALAR
        }
    }
}