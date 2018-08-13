package com.akazlou.dynoman.view

import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec


class QueryTypeSpec : StringSpec({
    "should sort query types by name, although table comes first always" {
        val index1 = QueryType("nameA", listOf(KeySchemaElement("attr1", KeyType.HASH)), true)
        val index2 = QueryType("nameC", listOf(KeySchemaElement("attr2", KeyType.HASH)), true)
        val index3 = QueryType("nameB",
                listOf(KeySchemaElement("attr3", KeyType.HASH), KeySchemaElement("attr4", KeyType.RANGE)),
                true)
        val table = QueryType("nameD", listOf(KeySchemaElement("attr5", KeyType.HASH)), false)
        val queryTypes = listOf(index1, index2, table, index3)
        queryTypes.sorted() shouldBe listOf(table, index1, index3, index2)
    }
})