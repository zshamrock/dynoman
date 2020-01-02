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
        return getValues(null, paths)
    }

    private fun getValues(parent: Any?, paths: List<String>): List<String> {
        val head = paths[0]
        val tail = paths.drop(1)
        val parentDataType = getDataType(parent)
        if (paths.size == 1) {
            @Suppress("UNCHECKED_CAST")
            val value = when (parentDataType) {
                DataType.NULL -> getRawValue(head)
                DataType.MAP -> (parent as Map<String, *>)[head]
                DataType.LIST, DataType.SET -> (parent as Collection<*>).map {
                    val v = (it as Map<String, *>)[head]
                    if (getDataType(v).isComposite()) {
                        v as Collection<*>
                    } else {
                        listOf(v)
                    }
                }.flatten()
                else -> throw IllegalStateException("Failed to process the path $paths, ended with illegal parent "
                        + "data type $parentDataType")
            }
            val dataType = getDataType(value)
            return when (dataType) {
                DataType.NULL, DataType.SCALAR -> listOf(value)
                DataType.SET, DataType.LIST -> value as Collection<*>
                else -> throw UnsupportedOperationException("Leaf value of type $dataType is not supported")
            }.filterNotNull().map { it.toString() }
        }

        @Suppress("UNCHECKED_CAST")
        return when (parentDataType) {
            DataType.NULL -> getValues(getRawValue(head), tail)
            DataType.LIST, DataType.SET -> (parent as Collection<*>)
                    .map { getValues((it as Map<String, *>)[head], tail) }
                    .flatten()
            DataType.MAP -> getValues((parent as Map<String, *>)[head], tail)
            else -> throw UnsupportedOperationException(
                    "Intermediate parent data type $parentDataType is not supported")
        }
    }
}