package com.akazlou.dynoman.function

interface Function<T> {
    fun name(): String
    fun parse(text: String): T
}