package com.akazlou.dynoman.function

import java.time.Clock

object Functions {
    fun getAvailableFunctions(): List<Function<*>> {
        return listOf(NowFunction(Clock.systemDefaultZone()), ParseUUIDFunction(), TimestampFunction())
    }
}