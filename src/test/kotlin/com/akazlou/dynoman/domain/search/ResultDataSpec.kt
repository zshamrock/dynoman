package com.akazlou.dynoman.domain.search

import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row

class ResultDataSpec : StringSpec({
    "get data type" {
        forall(
                row(emptyMap<String, Any?>(), "name", ResultData.DataType.NULL),
                row(mapOf("name" to "value"), "name", ResultData.DataType.SCALAR),
                row(mapOf("name" to true), "name", ResultData.DataType.SCALAR),
                row(mapOf("name" to 0), "name", ResultData.DataType.SCALAR),
                row(mapOf("name" to emptyList<String>()), "name", ResultData.DataType.LIST),
                row(mapOf("name" to emptyList<Int>()), "name", ResultData.DataType.LIST),
                row(mapOf("name" to listOf("value")), "name", ResultData.DataType.LIST),
                row(mapOf("name" to listOf(0)), "name", ResultData.DataType.LIST),
                row(mapOf("name" to emptyMap<String, String>()), "name", ResultData.DataType.MAP),
                row(mapOf("name" to emptyMap<String, Int>()), "name", ResultData.DataType.MAP),
                row(mapOf("name" to mapOf("x" to "y")), "name", ResultData.DataType.MAP),
                row(mapOf("name" to mapOf("x" to 0)), "name", ResultData.DataType.MAP),
                row(mapOf("name" to emptySet<String>()), "name", ResultData.DataType.SET),
                row(mapOf("name" to emptySet<Int>()), "name", ResultData.DataType.SET),
                row(mapOf("name" to setOf("value")), "name", ResultData.DataType.SET),
                row(mapOf("name" to setOf(0)), "name", ResultData.DataType.SET)
        ) { data, name, dt ->
            ResultData(data, KeySchemaElement("-", KeyType.HASH), null).getDataType(name) shouldBe dt
        }
    }

    "verify is collection type" {
        forall(
                row(ResultData.DataType.LIST, true),
                row(ResultData.DataType.SET, true),
                row(ResultData.DataType.MAP, true),
                row(ResultData.DataType.SCALAR, false),
                row(ResultData.DataType.NULL, false)
        ) { dt, expected ->
            dt.isCollection() shouldBe expected
        }
    }

    "verify is map type" {
        forall(
                row(ResultData.DataType.LIST, false),
                row(ResultData.DataType.SET, false),
                row(ResultData.DataType.MAP, true),
                row(ResultData.DataType.SCALAR, false),
                row(ResultData.DataType.NULL, false)
        ) { dt, expected ->
            dt.isMap() shouldBe expected
        }
    }
})