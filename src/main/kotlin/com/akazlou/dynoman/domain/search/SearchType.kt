package com.akazlou.dynoman.domain.search

import java.util.*

enum class SearchType {
    SCAN,
    QUERY;

    override fun toString(): String {
        return this.name.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase(Locale.ROOT) }
    }

    fun isScan(): Boolean {
        return this == SCAN
    }
}