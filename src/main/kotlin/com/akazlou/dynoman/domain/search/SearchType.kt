package com.akazlou.dynoman.domain.search

import java.util.Locale

enum class SearchType {
    SCAN,
    QUERY;

    override fun toString(): String {
        return this.name.toLowerCase(Locale.ROOT).capitalize()
    }

    fun isScan(): Boolean {
        return this == SCAN
    }
}