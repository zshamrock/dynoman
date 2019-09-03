package com.akazlou.dynoman.domain.search

/**
 * Holds foreign query user's provided search input.
 */
data class SearchInput(val name: String, val type: Type, val operator: Operator, val refs: List<String>)