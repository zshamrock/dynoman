package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.search.Condition
import com.akazlou.dynoman.domain.search.Operator
import com.akazlou.dynoman.domain.search.Order
import com.akazlou.dynoman.domain.search.QuerySearch
import com.akazlou.dynoman.domain.search.ScanSearch
import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.domain.search.SearchType
import tornadofx.*
import java.io.StringWriter
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import javax.json.Json
import javax.json.JsonString
import javax.json.stream.JsonGenerator
import com.akazlou.dynoman.domain.search.Type as AttributeType

class SearchesSaverService {
    enum class Type(val root: String, val suffix: String) {
        SESSION("sessions", ".session"),
        QUERY("query", ".query")
    }

    companion object {
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
    }

    private val wf = Json.createWriterFactory(mapOf(
            JsonGenerator.PRETTY_PRINTING to true))
    private val pf = Json.createParserFactory(mutableMapOf<String, Any>())

    private val namesCache: MutableMap<URI, List<String>> = mutableMapOf()

    // Allows to fetch the data when called for the first time, and even refresh is false
    private val cacheInitialized: MutableSet<URI> = mutableSetOf()

    fun save(type: Type, base: Path, name: String, searches: List<Search>) {
        // Clean up the cache on the save, so next call to the listNames will fetch the up to date values
        cacheInitialized.remove(base.toUri())
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
                if (search.filters.isNotEmpty()) {
                    val filters = Json.createArrayBuilder()
                    search.filters.forEach {
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
        json.add(type.root, array)
        val writer = StringWriter()
        wf.createWriter(writer).write(json.build())
        Files.createDirectories(base)
        Files.write(resolve(type, base, name),
                listOf(writer.toString()),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)
    }

    private fun resolve(type: Type, base: Path, name: String) = base.resolve("$name${type.suffix}")

    fun restore(type: Type, base: Path, name: String): List<Search> {
        val path = resolve(type, base, name)
        val parser = pf.createParser(path.toFile().reader())
        parser.next()
        val sessions = parser.`object`.getJsonArray(type.root).map { value ->
            val obj = value.asJsonObject()
            val searchType = SearchType.valueOf(obj.getString(SEARCH_TYPE))
            val table = obj.getString(TABLE)
            val order = Order.valueOf(obj.getString(ORDER))
            val index = obj.getString(INDEX, null)
            val filters = obj.getJsonArray(FILTERS).orEmpty().map { it.asJsonObject() }.map { filter ->
                Condition(
                        filter.getString(FILTER_NAME),
                        AttributeType.valueOf(filter.getString(FILTER_TYPE)),
                        Operator.valueOf(filter.getString(FILTER_OPERATOR)),
                        filter.getJsonArray(FILTER_VALUES)?.getValuesAs(JsonString::getString).orEmpty()
                )
            }
            when (searchType) {
                SearchType.QUERY -> {
                    val hash = obj.getJsonObject(HASH)
                    val hashKey = Condition(
                            hash.getString(HASH_NAME),
                            AttributeType.valueOf(hash.getString(HASH_TYPE)),
                            Operator.EQ,
                            listOf(hash.getString(HASH_VALUE, "")))
                    val sort = obj.getJsonObject(SORT)
                    val sortKey = if (sort == null) {
                        null
                    } else {
                        Condition(
                                sort.getString(SORT_NAME),
                                AttributeType.valueOf(sort.getString(SORT_TYPE)),
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
        return sessions
    }

    fun listNames(path: Path): List<String> {
        val uri = path.toUri()
        if (cacheInitialized.contains(uri)) {
            return namesCache.getOrElse(uri, { listOf() })
        }
        val dir = path.toFile()
        if (!dir.exists()) {
            return listOf()
        }
        val names = dir.listFiles().orEmpty()
                .map { it.nameWithoutExtension }
                .sortedWith(String.CASE_INSENSITIVE_ORDER)
                .toList()
        namesCache[uri] = names
        cacheInitialized.add(uri)
        return names
    }
}