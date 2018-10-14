package com.akazlou.dynoman.domain

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