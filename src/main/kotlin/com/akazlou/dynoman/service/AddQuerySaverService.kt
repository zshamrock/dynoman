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
        service.save(SAVER_TYPE, base, "$table$TABLE_NAME_SEPARATOR$name", listOf(search))
    }

    fun listNames(table: String, path: Path): List<String> {
        val names = service.listNames(path)
        return names.filter { it.startsWith("$table$TABLE_NAME_SEPARATOR") }
                .map { it.removePrefix("$table$TABLE_NAME_SEPARATOR") }

    }

    fun restore(base: Path, name: String): List<Search> {
        return service.restore(SAVER_TYPE, base, name)
    }
}