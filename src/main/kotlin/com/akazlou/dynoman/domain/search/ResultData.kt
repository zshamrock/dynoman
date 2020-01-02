package com.akazlou.dynoman.domain.search

import com.amazonaws.services.dynamodbv2.model.KeySchemaElement

data class ResultData(val data: Map<String, Any?>, val hashKey: KeySchemaElement, val sortKey: KeySchemaElement?) {
    companion object {
        private const val PATHS_SEPARATOR = "."
    }

    enum class DataType {
        LIST,
        MAP,
        SET,
        SCALAR,
        NULL;

        fun isCollection(): Boolean {
            return this == LIST || this == SET;
        }

        fun isMap(): Boolean {
            return this == MAP
        }

        fun isComposite(): Boolean {
            return isCollection() || isMap()
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
        return getDataType(value)
    }

    private fun getDataType(value: Any?): DataType {
        return when (value) {
            null -> DataType.NULL
            is Map<*, *> -> DataType.MAP
            is Set<*> -> DataType.SET
            is List<*> -> DataType.LIST
            else -> DataType.SCALAR
        }
    }

    fun getValues(path: String): List<String> {
        val paths = path.split(PATHS_SEPARATOR)
        return getValues(null, paths).distinct()
    }

    private fun getValues(parent: Any?, paths: List<String>): List<String> {
        val dataType = getDataType(parent)
        if (paths.isEmpty()) {
            return when (dataType) {
                DataType.NULL, DataType.SCALAR -> listOf(parent)
                DataType.LIST, DataType.SET -> parent as Collection<*>
                else -> throw UnsupportedOperationException("Leaf value of type $dataType is not supported")
            }.filterNotNull().map { it.toString() }
        }
        val head = paths[0]
        val tail = paths.drop(1)
        @Suppress("UNCHECKED_CAST")
        return when (dataType) {
            DataType.NULL -> getValues(getRawValue(head), tail)
            DataType.MAP -> getValues((parent as Map<String, *>)[head], tail)
            DataType.LIST -> (parent as Collection<*>)
                    .map { getValues((it as Map<String, *>)[head], tail) }
                    .flatten()
            else -> throw UnsupportedOperationException("Intermediate node value of type $dataType is not supported")
        }
    }
}