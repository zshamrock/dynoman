package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.ForeignSearchName
import com.akazlou.dynoman.domain.search.Condition
import com.akazlou.dynoman.domain.search.QuerySearch
import com.akazlou.dynoman.domain.search.ScanSearch
import com.akazlou.dynoman.domain.search.Search
import java.nio.file.Path
import java.util.EnumSet
import java.util.concurrent.atomic.AtomicInteger

class AddQuerySaverService {
    companion object {
        @JvmField
        val SAVER_TYPE = SearchesSaverService.Type.QUERY
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
        val fsn = ForeignSearchName(
                table,
                name,
                if (questionIndex.get() == 1) {
                    ForeignSearchName.EMPTY_FLAGS
                } else {
                    EnumSet.of(ForeignSearchName.Flag.QUESTION)
                })
        service.save(SAVER_TYPE, base, fsn.getFullName(), listOf(preprocessed))
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

    fun listNames(table: String, path: Path): List<ForeignSearchName> {
        val names = service.listNames(path)
        return names.map { ForeignSearchName.of(it) }.filter { it.matches(table) }
    }

    fun restore(table: String, base: Path, fsn: ForeignSearchName): Search {
        return service.restore(SAVER_TYPE, base, fsn.getFullName()).first()
    }
}