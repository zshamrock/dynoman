package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.search.Condition
import com.akazlou.dynoman.domain.search.Operator
import com.akazlou.dynoman.domain.search.Order
import com.akazlou.dynoman.domain.search.QuerySearch
import com.akazlou.dynoman.domain.search.ScanSearch
import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.domain.search.SearchType
import com.akazlou.dynoman.domain.search.Type
import tornadofx.*
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import javax.json.Json
import javax.json.JsonString
import javax.json.stream.JsonGenerator

class SessionSaverService {
    companion object {
        private const val SUFFIX = ".session"
        private const val SEARCH_TYPE = "type"
        private const val TABLE = "table"
        private const val INDEX = "index"
        private const val HASH = "hash"
        private const val HASH_NAME = "name"
        private const val HASH_TYPE = "type"
        private const val HASH_VALUE = "value"
        private const val SORT = "sort"
        private const val SORT_NAME = "name"
        private const val SORT_TYPE = "type"
        private const val SORT_VALUE = "value"
        private const val SORT_OPERATOR = "operator"
        private const val FILTERS = "filters"
        private const val FILTER_NAME = "name"
        private const val FILTER_TYPE = "type"
        private const val FILTER_VALUES = "values"
        private const val FILTER_OPERATOR = "operator"
        private const val ORDER = "order"
        private const val SESSIONS = "sessions"
    }

    private val wf = Json.createWriterFactory(mapOf(
            JsonGenerator.PRETTY_PRINTING to true))
    private val pf = Json.createParserFactory(mutableMapOf<String, Any>())

    fun save(base: Path, name: String, searches: List<Search>) {
        val json = JsonBuilder()
        val array = Json.createArrayBuilder()
        searches.forEach { search ->
            val builder = JsonBuilder()
            with(builder) {
                add(SEARCH_TYPE, search.type.name)
                add(TABLE, search.table)
                if (search.index != null) {
                    add(INDEX, search.index)
                }
                if (search is QuerySearch) {
                    val hash = JsonBuilder()
                    with(hash) {
                        add(HASH_NAME, search.getHashKeyName())
                        add(HASH_TYPE, search.getHashKeyType().name)
                        add(HASH_VALUE, search.getHashKeyValue())
                    }
                    add(HASH, hash.build())
                    if (search.getRangeKeyName() != null) {
                        val sort = JsonBuilder()
                        with(sort) {
                            add(SORT_NAME, search.getRangeKeyName())
                            add(SORT_TYPE, search.getRangeKeyType().name)
                            add(SORT_OPERATOR, search.getRangeKeyOperator().name)
                            val values = Json.createArrayBuilder()
                            search.getRangeKeyValues().forEach {
                                values.add(it)
                            }
                            add(SORT_VALUE, values.build())
                        }
                        add(SORT, sort.build())
                    }
                }
                if (search.conditions.isNotEmpty()) {
                    val filters = Json.createArrayBuilder()
                    search.conditions.forEach {
                        val filter = JsonBuilder()
                        with(filter) {
                            add(FILTER_NAME, it.name)
                            add(FILTER_TYPE, it.type.name)
                            add(FILTER_OPERATOR, it.operator.name)
                            add(FILTER_VALUES, it.values)
                        }
                        filters.add(filter.build())
                    }
                    add(FILTERS, filters)
                }
                add(ORDER, search.order.name)
            }
            array.add(builder.build())
        }
        json.add(SESSIONS, array)
        val writer = StringWriter()
        wf.createWriter(writer).write(json.build())
        Files.createDirectories(base)
        Files.write(resolve(base, name),
                listOf(writer.toString()),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)
    }

    private fun resolve(base: Path, name: String) = base.resolve("$name$SUFFIX")

    fun restore(base: Path, name: String): List<Search> {
        val path = resolve(base, name)
        println("Parsing $path")
        val parser = pf.createParser(path.toFile().reader())
        parser.next()
        val sessions = parser.`object`.getJsonArray(SESSIONS).map { value ->
            val obj = value.asJsonObject()
            val searchType = SearchType.valueOf(obj.getString(SEARCH_TYPE))
            println("Parsing $searchType")
            val table = obj.getString(TABLE)
            val order = Order.valueOf(obj.getString(ORDER))
            val index = obj.getString(INDEX, null)
            val filters = obj.getJsonArray(FILTERS).orEmpty().map { it.asJsonObject() }.map { filter ->
                Condition(
                        filter.getString(FILTER_NAME),
                        Type.valueOf(filter.getString(FILTER_TYPE)),
                        Operator.valueOf(filter.getString(FILTER_OPERATOR)),
                        filter.getJsonArray(FILTER_VALUES).orEmpty().map { it.toString() }
                )
            }
            when (searchType) {
                SearchType.QUERY -> {
                    val hash = obj.getJsonObject(HASH)
                    val hashKey = Condition(
                            hash.getString(HASH_NAME),
                            Type.valueOf(hash.getString(HASH_TYPE)),
                            Operator.EQ,
                            listOf(hash.getString(HASH_VALUE)))
                    val sort = obj.getJsonObject(SORT)
                    val sortKey = if (sort == null) {
                        null
                    } else {
                        Condition(
                                sort.getString(SORT_NAME),
                                Type.valueOf(sort.getString(SORT_TYPE)),
                                Operator.valueOf(sort.getString(SORT_OPERATOR)),
                                sort.getJsonArray(SORT_VALUE).getValuesAs(JsonString::getString))
                    }
                    QuerySearch(
                            table,
                            index,
                            hashKey,
                            sortKey,
                            filters,
                            order)
                }
                SearchType.SCAN -> {
                    ScanSearch(table, index, filters)
                }
            }
        }.toList()
        println(sessions)
        return sessions
    }

    fun listNames(path: Path): List<String> {
        val dir = path.toFile()
        if (!dir.exists()) {
            return listOf()
        }
        return dir.listFiles().map { it.nameWithoutExtension }.toList()
    }
}