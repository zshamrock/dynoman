package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.SearchSource
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec


class SearchSourceSpec : StringSpec({
    "should sort query types by name, although table comes first always" {
        val index1 = SearchSource("nameA", listOf(KeySchemaElement("attr1", KeyType.HASH)), true)
        val index2 = SearchSource("nameC", listOf(KeySchemaElement("attr2", KeyType.HASH)), true)
        val index3 = SearchSource("nameB",
                listOf(KeySchemaElement("attr3", KeyType.HASH), KeySchemaElement("attr4", KeyType.RANGE)),
                true)
        val table = SearchSource("nameD", listOf(KeySchemaElement("attr5", KeyType.HASH)), false)
        val searchSources = listOf(index1, index2, table, index3)
        searchSources.sorted() shouldBe listOf(table, index1, index3, index2)
    }
})