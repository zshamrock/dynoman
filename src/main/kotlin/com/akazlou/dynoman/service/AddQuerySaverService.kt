package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.Environment
import com.akazlou.dynoman.domain.ForeignSearchName
import com.akazlou.dynoman.domain.search.Condition
import com.akazlou.dynoman.domain.search.QuerySearch
import com.akazlou.dynoman.domain.search.ResultData
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

    fun save(table: String, base: Path, name: String, search: Search, data: List<ResultData>): ForeignSearchName {
        val env = Environment(search.table)
        val questionIndex = AtomicInteger(QUESTION_INDEX_INITIAL_VALUE)
        val dataTypes = mutableListOf<ResultData.DataType>()
        val preprocessed = when (search) {
            is ScanSearch -> {
                ScanSearch(
                        env.value,
                        search.index?.let { Environment(it).value },
                        search.filters.map { preprocess(it, questionIndex, data, dataTypes) })
            }
            is QuerySearch -> {
                QuerySearch(
                        env.value,
                        search.index?.let { Environment(it).value },
                        preprocess(search.hashKey, questionIndex, data, dataTypes),
                        search.rangeKey?.let { preprocess(it, questionIndex, data, dataTypes) },
                        search.filters.map { preprocess(it, questionIndex, data, dataTypes) },
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
        if (dataTypes.contains(ResultData.DataType.SET) || dataTypes.contains(ResultData.DataType.LIST)) {
            flags.add(ForeignSearchName.Flag.EXPAND_COLLECTION)
        }
        val fsn = ForeignSearchName(
                Environment(table).value,
                name,
                flags)
        service.save(SAVER_TYPE, base, fsn.getFullName(), listOf(preprocessed))
        return fsn
    }

    private fun preprocess(condition: Condition,
                           index: AtomicInteger,
                           data: List<ResultData>,
                           dataTypes: MutableList<ResultData.DataType>): Condition {
        return Condition(
                condition.name,
                condition.type,
                condition.operator,
                condition.values.map { value ->
                    if (value.startsWith(Search.USER_INPUT_MARK)) {
                        "$value${index.getAndIncrement()}"
                    } else {
                        dataTypes.add(findDataType(value, data))
                        value
                    }
                })
    }

    private fun findDataType(value: String, data: List<ResultData>): ResultData.DataType {
        return data.asSequence().map { it.getDataType(value) }
                .find { it != ResultData.DataType.NULL } ?: ResultData.DataType.NULL
    }

    fun listNames(table: String, path: Path): List<ForeignSearchName> {
        val names = service.listNames(path)
        val env = Environment(table)
        return names.map { ForeignSearchName.of(it) }.filter { it.matches(env.value) }
    }

    fun restore(table: String, base: Path, fsn: ForeignSearchName): Search {
        val search = service.restore(SAVER_TYPE, base, fsn.getFullName()).first()
        val env = Environment(table)
        if (env.isEmpty()) {
            return search
        }
        val prefixedTable = env.prefix(search.table)
        val prefixedIndex = search.index?.let { env.prefix(it) }
        return when (search) {
            is ScanSearch -> {
                ScanSearch(prefixedTable, prefixedIndex, search.filters)
            }
            is QuerySearch -> {
                QuerySearch(prefixedTable, prefixedIndex, search.hashKey, search.rangeKey, search.filters, search.order)
            }
        }
    }
}