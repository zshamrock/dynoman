package com.akazlou.dynoman.domain.search

import com.akazlou.dynoman.view.SearchSource

/**
 * Class to encapsulate search (either scan or query) arguments.
 */
data class SearchCriteria(val type: SearchType,
                          val tableName: String,
                          val searchSource: SearchSource?,
                          val hashKeyValue: String?,
                          val sortKeyOperator: Operator?,
                          val sortKeyValues: List<String>,
                          val order: Order?,
                          val queryFilters: List<QueryFilter>) {
    fun isBetweenSortKeyOperator(): Boolean {
        return sortKeyOperator != null && sortKeyOperator.isBetween()
    }

    fun getSortKeyValueFrom(): String? {
        return sortKeyValues.getOrNull(0)
    }

    fun getSortKeyValueTo(): String? {
        return sortKeyValues.getOrNull(1)
    }

    fun forEachQueryFilter(action: (QueryFilter) -> Unit) {
        queryFilters.forEach(action)
    }
}