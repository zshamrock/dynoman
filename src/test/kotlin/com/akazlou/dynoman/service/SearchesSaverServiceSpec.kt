package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.search.Condition
import com.akazlou.dynoman.domain.search.Operator
import com.akazlou.dynoman.domain.search.Order
import com.akazlou.dynoman.domain.search.QuerySearch
import com.akazlou.dynoman.domain.search.ScanSearch
import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.domain.search.Type
import com.akazlou.dynoman.service.SearchesSaverService.Type.SESSION
import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.matchers.collections.shouldBeSorted
import io.kotlintest.matchers.collections.shouldContainInOrder
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.nio.file.Paths

class SearchesSaverServiceSpec : StringSpec() {
    private val base = Paths.get("src", "test", "resources", "sessions")

    private val singleSearch = QuerySearch(
            "Table1",
            null,
            Condition.hashKey("Id", Type.STRING, "abc"),
            null,
            listOf(),
            Order.ASC
    )

    private val multipleSearches = listOf(
            // Table without filters
            QuerySearch(
                    "Table1",
                    null,
                    Condition.hashKey("Id", Type.STRING, "abc"),
                    null,
                    listOf(),
                    Order.ASC),
            // Table with filters
            QuerySearch(
                    "Table2",
                    null,
                    Condition.hashKey("Id", Type.NUMBER, "123"),
                    Condition("Timestamp", Type.NUMBER, Operator.GE, listOf("456")),
                    listOf(Condition("Filter1", Type.STRING, Operator.CONTAINS, listOf("xyz"))),
                    Order.DESC),
            // Complex operators like BETWEEN and EXISTS, more than 1 filter and index
            QuerySearch(
                    "Table3",
                    "Table3.Index1",
                    Condition.hashKey("Id", Type.NUMBER, "123"),
                    Condition("Timestamp", Type.NUMBER, Operator.BETWEEN, listOf("5", "6")),
                    listOf(
                            Condition("Filter1", Type.NUMBER, Operator.BETWEEN, listOf("7", "8")),
                            Condition("Filter2", Type.STRING, Operator.EXISTS, listOf())),
                    Order.DESC),
            // Empty values
            QuerySearch(
                    "Table4",
                    "Table4.Index1",
                    Condition.hashKey("Id", Type.STRING, ""),
                    Condition("Timestamp", Type.NUMBER, Operator.EQ, listOf("")),
                    listOf(
                            Condition("Filter1", Type.NUMBER, Operator.BETWEEN, listOf("", "")),
                            Condition("Filter2", Type.STRING, Operator.EQ, listOf(""))),
                    Order.ASC),
            // Table scan
            ScanSearch("Table5", null, listOf()),
            // Index scan
            ScanSearch("Table6", "Table6.Index1", listOf()),
            // Index scan with filters
            ScanSearch(
                    "Table7",
                    "Table7.Index1",
                    listOf(
                            Condition("Filter1", Type.NUMBER, Operator.EQ, listOf("1")))),
            // Table scan with more complex and more than 1 filters
            ScanSearch(
                    "Table8",
                    null,
                    listOf(
                            Condition("Filter1", Type.NUMBER, Operator.EQ, listOf("1")),
                            Condition("Filter2", Type.NUMBER, Operator.BETWEEN, listOf("2", "3")),
                            Condition("Filter3", Type.STRING, Operator.EXISTS, listOf()),
                            Condition("Filter3", Type.STRING, Operator.NOT_CONTAINS, listOf("abc"))
                    )),
            // Table scan with empty values
            ScanSearch(
                    "Table9",
                    null,
                    listOf(
                            Condition("Filter1", Type.NUMBER, Operator.EQ, listOf("")),
                            Condition("Filter2", Type.NUMBER, Operator.BETWEEN, listOf("", "")),
                            Condition("Filter3", Type.STRING, Operator.EXISTS, listOf()),
                            Condition("Filter3", Type.STRING, Operator.NOT_CONTAINS, listOf(""))
                    ))

    )

    init {
        "save single sessions" {
            val service = SearchesSaverService()
            service.save(SESSION, base, "test1_actual", listOf(singleSearch))
            "test1" shouldBe sameContent()
        }

        "save different type of sessions" {
            val service = SearchesSaverService()

            service.save(SESSION, base, "test2_actual", multipleSearches)
            "test2" shouldBe sameContent()
        }

        "restore empty sessions" {
            val service = SearchesSaverService()
            val searches = service.restore(SESSION, base, "test1_expected")
            searches.shouldHaveSize(1)
            searches[0] shouldBe sameSearch(singleSearch)
        }

        "restore different type of sessions" {
            val service = SearchesSaverService()
            val searches = service.restore(SESSION, base, "test2_expected")
            searches.shouldHaveSize(multipleSearches.size)
            searches.forEachIndexed { index, search ->
                search shouldBe sameSearch(multipleSearches[index])
            }
        }

        "list names" {
            val service = SearchesSaverService()
            val names = service.listNames(base)
            names.shouldBeSorted()
            names.shouldContainInOrder("test1_expected", "test2_expected")
        }
    }

    private fun sameContent() = object : Matcher<String> {
        override fun test(value: String): Result {
            val actual = "${value}_actual.session"
            val expected = "${value}_expected.session"
            return Result(
                    base.resolve(actual).toFile().readText() == base.resolve(expected).toFile().readText(),
                    "Contents of the files $actual and $expected don't match",
                    "Contents of the files $actual and $expected match"
            )
        }
    }

    private fun sameSearch(search: Search) = object : Matcher<Search> {
        override fun test(value: Search): Result {
            if (search.type != value.type) {
                return Result(false, "Search types don't match", "")
            }
            if (search.table != value.table
                    || search.index != value.index
                    || search.filters != value.filters
                    || search.order != value.order) {
                return Result(false, "Base search properties don't match", "")
            }
            if (search is QuerySearch && value is QuerySearch) {
                if (search.getHashKeyName() != value.getHashKeyName()
                        || search.getHashKeyType() != value.getHashKeyType()
                        || search.getHashKeyValue() != value.getHashKeyValue()) {
                    return Result(false, "Query hash key properties don't match", "")
                }
                if (search.getRangeKeyName() != value.getRangeKeyName()
                        || search.getRangeKeyType() != value.getRangeKeyType()
                        || search.getRangeKeyOperator() != value.getRangeKeyOperator()
                        || search.getRangeKeyValues() != value.getRangeKeyValues()) {
                    return Result(false, "Query range key properties don't match", "")
                }
            }
            return Result(true, "", "")
        }
    }
}