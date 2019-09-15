package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.Environment
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
        private const val QUESTION_INDEX_INITIAL_VALUE = 1
        @JvmField
        val SAVER_TYPE = SearchesSaverService.Type.QUERY
    }

    private val service = SearchesSaverService()

    fun save(table: String, base: Path, name: String, search: Search) {
        val env = Environment(table)
        val questionIndex = AtomicInteger(QUESTION_INDEX_INITIAL_VALUE)
        val preprocessed = when (search) {
            is ScanSearch -> {
                ScanSearch(
                        env.envlessTableOrIndex,
                        search.index?.let { Environment(it).envlessTableOrIndex },
                        search.filters.map { preprocess(it, questionIndex) })
            }
            is QuerySearch -> {
                QuerySearch(
                        env.envlessTableOrIndex,
                        search.index?.let { Environment(it).envlessTableOrIndex },
                        preprocess(search.hashKey, questionIndex),
                        search.rangeKey?.let { preprocess(it, questionIndex) },
                        search.filters.map { preprocess(it, questionIndex) },
                        search.order)
            }
        }
        val flags = EnumSet.noneOf(ForeignSearchName.Flag::class.java)
        if (questionIndex.get() != QUESTION_INDEX_INITIAL_VALUE) {
            flags.add(ForeignSearchName.Flag.QUESTION)
        }
        if (env.isNotEmpty()) {
            flags.add(ForeignSearchName.Flag.ENVIRONMENT_STRIPPED)
        }
        val fsn = ForeignSearchName(
                env.envlessTableOrIndex,
                name,
                flags)
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
        val env = Environment(table)
        return names.map { ForeignSearchName.of(it) }.filter { it.matches(env.envlessTableOrIndex) }
    }

    fun restore(table: String, base: Path, fsn: ForeignSearchName): Search {
        return service.restore(SAVER_TYPE, base, fsn.getFullName()).first()
    }
}