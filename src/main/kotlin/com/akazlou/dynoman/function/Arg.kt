package com.akazlou.dynoman.function

data class Arg(
        val name: String, val type: ArgType, val desc: String, val optional: Boolean = false, val example: String = "")

enum class ArgType {
    NUMERIC,
    STRING,
    BOOLEAN,
    DATE_TIME
}