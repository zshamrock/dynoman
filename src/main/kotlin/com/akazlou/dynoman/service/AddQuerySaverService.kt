package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.Environment
import com.akazlou.dynoman.domain.ForeignSearchName
import com.akazlou.dynoman.domain.UnsupportedForeignSearchUsageException
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
        const val MAP_KEY_NAME_SEPARATOR = "."
        @JvmField
        val SAVER_TYPE = SearchesSaverService.Type.QUERY
    }

    private val service = SearchesSaverService()

    fun save(table: String, base: Path, name: String, search: Search, data: List<ResultData>): ForeignSearchName {
        val env = Environment(search.table)
        val questionIndex = AtomicInteger(QUESTION_INDEX_INITIAL_VALUE)
        val dataTypes = mutableMapOf<String, ResultData.DataType>()
        val dataSeq = data.asSequence()
        val preprocessed = when (search) {
            is ScanSearch -> {
                ScanSearch(
                        env.value,
                        search.index?.let { Environment(it).value },
                        search.filters.map { preprocess(it, questionIndex, dataSeq, dataTypes) })
            }
            is QuerySearch -> {
                QuerySearch(
                        env.value,
                        search.index?.let { Environment(it).value },
                        preprocess(search.hashKey, questionIndex, dataSeq, dataTypes),
                        search.rangeKey?.let { preprocess(it, questionIndex, dataSeq, dataTypes) },
                        search.filters.map { preprocess(it, questionIndex, dataSeq, dataTypes) },
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
        val collectionTypesCount = dataTypes.values.count { it.isComposite() }
        if (collectionTypesCount > 1) {
            throw UnsupportedForeignSearchUsageException(
                    "Only single collection like mapping is currently supported by the foreign search set up, but " +
                            "found $collectionTypesCount: ${dataTypes.filterValues { it.isComposite() }}.")
        }
        if (collectionTypesCount != 0) {
            flags.add(ForeignSearchName.Flag.EXPAND)
        }
        val fsn = ForeignSearchName(
                Environment(table).value,
                name,
                flags)
        service.save(SAVER_TYPE, base, fsn.getFullName(), listOf(preprocessed))
        return fsn
    }

    /**
     * The result of this function also produces the side effect, i.e. modifies the passing _index_ and _dataTypes_
     * parameters. It is not necessary the good practice, but for now does the job well, unless the better solution
     * is designed.
     */
    private fun preprocess(condition: Condition,
                           index: AtomicInteger,
                           data: Sequence<ResultData>,
                           dataTypes: MutableMap<String, ResultData.DataType>): Condition {
        return Condition(
                condition.name,
                condition.type,
                condition.operator,
                condition.values.map { value ->
                    if (value.startsWith(Search.USER_INPUT_MARK)) {
                        "$value${index.getAndIncrement()}"
                    } else {
                        dataTypes[value] = findDataType(data, value)
                        value
                    }
                })
    }

    private fun findDataType(dataSeq: Sequence<ResultData>, value: String): ResultData.DataType {
        // In the case if value contains key name separator, i.e. whether the expansion should be on the map values,
        // either direct map or the map as the value of list/set
        val names = value.split(MAP_KEY_NAME_SEPARATOR)
        for (i in 0..names.size) {
            // Also if there are multiple maps involved or the attribute name has "." in its name, we iterate starting
            // from the whole value, and then reduce one name on every iteration, i.e. X.Y.Z -> X.Y -> X
            val name = names.slice(0 until names.size - i).joinToString(MAP_KEY_NAME_SEPARATOR)
            val dataType = getDataType(dataSeq, name)
            if (dataType != ResultData.DataType.NULL) {
                return dataType
            }
        }
        return ResultData.DataType.NULL
    }

    private fun getDataType(dataSeq: Sequence<ResultData>, value: String) =
            dataSeq.map { it.getDataType(value) }
                    .find { it != ResultData.DataType.NULL } ?: ResultData.DataType.NULL

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