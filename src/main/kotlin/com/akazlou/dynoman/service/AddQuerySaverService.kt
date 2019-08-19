package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.search.Condition
import com.akazlou.dynoman.domain.search.QuerySearch
import com.akazlou.dynoman.domain.search.ScanSearch
import com.akazlou.dynoman.domain.search.Search
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

class AddQuerySaverService {
    companion object {
        @JvmField
        val SAVER_TYPE = SearchesSaverService.Type.QUERY

        private const val TABLE_NAME_SEPARATOR = "@"
    }

    private val service = SearchesSaverService()

    fun save(table: String, base: Path, name: String, search: Search) {
        val questionIndex = AtomicInteger(1)
        val preprocessed = when (search) {
            is ScanSearch -> {
                ScanSearch(
                        search.table,
                        search.index,
                        search.filters.map { preprocess(it, questionIndex) })
            }
            is QuerySearch -> {
                QuerySearch(
                        search.table,
                        search.index,
                        preprocess(search.hashKey, questionIndex),
                        search.rangeKey?.let { preprocess(it, questionIndex) },
                        search.filters.map { preprocess(it, questionIndex) },
                        search.order)
            }
        }
        service.save(SAVER_TYPE, base, nameWithPrefix(name, table), listOf(preprocessed))
    }

    private fun preprocess(condition: Condition, index: AtomicInteger): Condition {
        return Condition(
                condition.name,
                condition.type,
                condition.operator,
                condition.values.map { value ->
                    if (value.startsWith(Search.USER_INPUT_MARK)) {
                        "$value${index.getAndIncrement()}"
                    } else {
                        value
                    }
                })
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