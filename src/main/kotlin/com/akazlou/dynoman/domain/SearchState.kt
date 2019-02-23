package com.akazlou.dynoman.domain

import com.akazlou.dynoman.view.SearchSource

data class SearchState(val type: SearchType,
                       val source: SearchSource,
                       val hashKeyValue: String?,
                       val sortKeyOperator: Operator?,
                       val sortKeyValues: List<String>,
                       val order: Order?,
                       val queryFilters: List<QueryFilter>)