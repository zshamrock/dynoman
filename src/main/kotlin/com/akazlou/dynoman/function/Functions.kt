package com.akazlou.dynoman.function

import java.time.Clock

object Functions {
    private val functions = listOf(
            DateTimeFunction(),
            NowFunction(Clock.systemDefaultZone()),
            ParseUUIDFunction(),
            TimestampFunction()
    ).sortedBy { it.name() }

    fun getAvailableFunctions(): List<Function<*>> {
        return functions
    }

    fun getCompletions(value: String): List<Function<*>> {
        return functions.filter { it.name().startsWith(value) }
    }
}