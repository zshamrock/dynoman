package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.search.Search
import java.nio.file.Path

class AddQuerySaverService {
    companion object {
        @JvmField
        val SAVER_TYPE = SearchesSaverService.Type.QUERY

        private const val TABLE_NAME_SEPARATOR = "@"
    }

    private val service = SearchesSaverService()

    fun save(table: String, base: Path, name: String, search: Search) {
        service.save(SAVER_TYPE, base, nameWithPrefix(name, table), listOf(search))
    }

    fun listNames(table: String, path: Path): List<String> {
        val names = service.listNames(path)
        return names.filter { startsWithPrefix(it, table) }.map { removePrefix(it, table) }
    }

    fun restore(table: String, base: Path, name: String): Search {
        return service.restore(SAVER_TYPE, base, nameWithPrefix(name, table)).first()
    }

    private fun startsWithPrefix(name: String, table: String) = name.startsWith("$table$TABLE_NAME_SEPARATOR")

    private fun removePrefix(name: String, table: String) = name.removePrefix("$table$TABLE_NAME_SEPARATOR")

    private fun nameWithPrefix(name: String, table: String) = "$table$TABLE_NAME_SEPARATOR$name"
}