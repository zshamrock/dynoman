package com.akazlou.dynoman.function

import java.time.Clock

class NowFunction(private val clock: Clock) : Function<Long>() {
    override fun name(): String {
        return "now"
    }

    override fun run(vararg args: Any): Long {
        return clock.millis()
    }

    override fun args(): List<Arg> = emptyList()
}