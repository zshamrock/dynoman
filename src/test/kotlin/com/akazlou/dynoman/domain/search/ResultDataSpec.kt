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
                row(ResultData.DataType.MAP, false),
                row(ResultData.DataType.SCALAR, false),
                row(ResultData.DataType.NULL, false)
        ) { dt, expected ->
            dt.isCollection() shouldBe expected
        }
    }

    "verify is composite type" {
        forall(
                row(ResultData.DataType.LIST, true),
                row(ResultData.DataType.SET, true),
                row(ResultData.DataType.MAP, true),
                row(ResultData.DataType.SCALAR, false),
                row(ResultData.DataType.NULL, false)
        ) { dt, expected ->
            dt.isComposite() shouldBe expected
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

    "get values by path" {
        forall(
                row(mapOf("x" to 10), "x", listOf("10")),
                row(mapOf("x" to 10), "y", emptyList()),
                row(mapOf("x" to listOf(10, 20, 30)), "x", listOf("10", "20", "30")),
                row(mapOf("x" to mapOf("y" to 10)), "x.y", listOf("10")),
                row(mapOf("x" to mapOf("y" to mapOf("z" to 10))), "x.y.z", listOf("10")),
                row(mapOf("x" to mapOf("y" to listOf(10, 20, 30))), "x.y", listOf("10", "20", "30")),
                row(mapOf("x" to mapOf("y" to mapOf("z" to listOf(10, 20, 30)))), "x.y.z", listOf("10", "20", "30")),
                row(mapOf("x" to listOf(mapOf("y" to 10), mapOf("y" to 20), mapOf("y" to 30), mapOf("z" to 30))),
                        "x.y",
                        listOf("10", "20", "30")),
                row(mapOf("x" to listOf(
                        mapOf("y" to listOf(10, 20, 30)),
                        mapOf("y" to listOf(40, 50, 60)),
                        mapOf("y" to listOf(70, 80, 90)),
                        mapOf("z" to 30))),
                        "x.y",
                        listOf("10", "20", "30", "40", "50", "60", "70", "80", "90")),
                row(mapOf("x" to listOf(
                        mapOf("y" to listOf(mapOf("z" to 10), mapOf("z" to 20))),
                        mapOf("y" to listOf(mapOf("z" to 30), mapOf("z" to 40))),
                        mapOf("y" to listOf(mapOf("z" to 50), mapOf("z" to 60))),
                        mapOf("y" to listOf(mapOf("x" to 70))),
                        mapOf("z" to 80))),
                        "x.y.z",
                        listOf("10", "20", "30", "40", "50", "60")),
                row(mapOf("x" to listOf(
                        mapOf("y" to listOf(mapOf("z" to listOf(10)), mapOf("z" to listOf(20)))),
                        mapOf("y" to listOf(mapOf("z" to listOf(30)), mapOf("z" to listOf(40)))),
                        mapOf("y" to listOf(mapOf("z" to listOf(50)), mapOf("z" to listOf(60)))),
                        mapOf("y" to listOf(mapOf("x" to 70))),
                        mapOf("z" to 80))),
                        "x.y.z",
                        listOf("10", "20", "30", "40", "50", "60"))
        ) { data, path, values ->
            ResultData(data, KeySchemaElement(), null).getValues(path) shouldBe values
        }
    }
})