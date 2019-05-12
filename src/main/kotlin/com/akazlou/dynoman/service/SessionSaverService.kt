package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.search.Order
import com.akazlou.dynoman.domain.search.SearchCriteria
import com.akazlou.dynoman.domain.search.SearchType
import tornadofx.*
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import javax.json.Json
import javax.json.stream.JsonGenerator
import kotlin.streams.toList

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

    fun restore(base: Path, name: String): List<SearchCriteria> {
        val path = resolve(base, name)
        println("Parsing $path")
        val parser = pf.createParser(path.toFile().reader())
        parser.next()
        val sessions = parser.`object`.getJsonArray("sessions").stream().map { value ->
            val obj = value.asJsonObject()
            val searchType = SearchType.valueOf(obj.getString("type"))
            println("Parsing $searchType")
            when (searchType) {
                SearchType.QUERY -> {
                    ""
                }
                SearchType.SCAN -> {
                    ""
                }
            }
        }.toList()
        println(sessions)
        return listOf()
    }

    fun listNames(path: Path): List<String> {
        val dir = path.toFile()
        if (!dir.exists()) {
            return listOf()
        }
        return dir.listFiles().map { it.nameWithoutExtension }.toList()
    }
}