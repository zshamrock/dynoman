package com.akazlou.dynoman.domain

import java.util.Locale

enum class OperationType {
    SCAN,
    QUERY;

    override fun toString(): String {
        return this.name.toLowerCase(Locale.ROOT).capitalize()
    }
}