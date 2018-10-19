package com.akazlou.dynoman.function

import java.time.Clock

object Functions {
    fun getAvailableFunctions(): List<Function<*>> {
        return listOf(
                DateTimeFunction(),
                NowFunction(Clock.systemDefaultZone()),
                ParseUUIDFunction(),
                TimestampFunction()
        )
    }
}