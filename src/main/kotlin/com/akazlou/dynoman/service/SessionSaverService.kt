package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.search.SearchCriteria
import tornadofx.*
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import javax.json.Json

class SessionSaverService {
    fun save(path: Path, name: String, searches: List<SearchCriteria>) {
        val json = JsonBuilder()
        val array = Json.createArrayBuilder()
        searches.forEach { search ->
            val builder = JsonBuilder()
            with(builder) {
                add("type", search.type.name)
            }
            array.add(builder.build())
        }
        json.add("sessions", array)
        val writer = StringWriter()
        Json.createWriter(writer).write(json.build())
        Files.createDirectories(path)
        Files.write(path.resolve("$name.session"),
                listOf(writer.toString()),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)
    }

    fun restore(): List<SearchCriteria> {
        TODO("Implement")
    }
}