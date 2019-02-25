package com.akazlou.dynoman.domain.search

import com.akazlou.dynoman.view.SearchSource

data class SearchCriteria(val type: SearchType,
                          val source: SearchSource,
                          val hashKeyValue: String?,
                          val sortKeyOperator: Operator?,
                          val sortKeyValues: List<String>,
                          val order: Order?,
                          val queryFilters: List<QueryFilter>)