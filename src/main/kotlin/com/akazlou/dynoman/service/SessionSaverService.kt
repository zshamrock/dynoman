package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.search.Operator
import com.akazlou.dynoman.domain.search.Order
import com.akazlou.dynoman.domain.search.QueryCondition
import com.akazlou.dynoman.domain.search.QuerySearch
import com.akazlou.dynoman.domain.search.ScanSearch
import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.domain.search.SearchCriteria
import com.akazlou.dynoman.domain.search.SearchType
import com.akazlou.dynoman.domain.search.Type
import tornadofx.*
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import javax.json.Json
import javax.json.stream.JsonGenerator

class SessionSaverService {
    companion object {
        private const val SUFFIX = ".session"
    }

    private val wf = Json.createWriterFactory(mapOf(
            JsonGenerator.PRETTY_PRINTING to true))
    private val pf = Json.createParserFactory(mutableMapOf<String, Any>())

    fun save(base: Path, name: String, searches: List<SearchCriteria>) {
        val json = JsonBuilder()
        val array = Json.createArrayBuilder()
        searches.forEach { search ->
            val builder = JsonBuilder()
            with(builder) {
                add("type", search.type.name)
                add("table", search.tableName)
                if (search.type == SearchType.QUERY) {
                    val source = search.searchSource!!
                    if (source.isIndex) {
                        add("index", source.name)
                    }
                    add("hash", search.hashKeyValue)
                    if (search.sortKeyOperator != null) {
                        add("sort", search.sortKeyValues)
                        add("operator", search.sortKeyOperator.name)
                    }
                }
                if (search.queryFilters.isNotEmpty()) {
                    val filters = Json.createArrayBuilder()
                    search.forEachQueryFilter {
                        val filter = JsonBuilder()
                        with(filter) {
                            add("name", it.name)
                            add("type", it.type.name)
                            add("operator", it.operator.name)
                            add("values", it.values.filterNotNull())
                        }
                        filters.add(filter.build())
                    }
                    add("filters", filters)
                }
                add("order", (search.order ?: Order.ASC).name)
            }
            array.add(builder.build())
        }
        json.add("sessions", array)
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
        val sessions = parser.`object`.getJsonArray("sessions").map { value ->
            val obj = value.asJsonObject()
            val searchType = SearchType.valueOf(obj.getString("type"))
            println("Parsing $searchType")
            val table = obj.getString("table")
            val order = Order.valueOf(obj.getString("order"))
            val index = obj.getString("index", null)
            val filters = obj.getJsonArray("filters").orEmpty().map { it.asJsonObject() }.map { filter ->
                QueryCondition(
                        filter.getString("name"),
                        Type.valueOf(filter.getString("type")),
                        Operator.valueOf(filter.getString("operator")),
                        filter.getJsonArray("values").orEmpty().map { it.toString() }
                )
            }
            when (searchType) {
                SearchType.QUERY -> {
                    // TODO: As we currently don't persist hash/parition and sort keys names and types below the
                    //  placeholders are used until this is fixed
                    val hash = obj.getString("hash")
                    val hashKey = QueryCondition("?", Type.STRING, Operator.EQ, listOf(hash))
                    val sort = obj.getJsonArray("sort").orEmpty().map { it.toString() }
                    val keys = if (sort.isEmpty()) {
                        listOf(hashKey)
                    } else {
                        listOf(hashKey,
                                QueryCondition("?", Type.NUMBER, Operator.valueOf(obj.getString("operator")), sort))
                    }
                    QuerySearch(
                            table,
                            index,
                            keys,
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