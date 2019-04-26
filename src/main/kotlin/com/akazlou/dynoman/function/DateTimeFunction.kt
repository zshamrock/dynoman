package com.akazlou.dynoman.function

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class DateTimeFunction : Function<String>() {
    companion object {
        @JvmField
        val DEFAULT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        @JvmField
        val UTC_ZONE: ZoneId = ZoneId.of("UTC")
    }

    override fun name(): String {
        return "datetime"
    }

    override fun desc(): String {
        return "Parse timestamp into the datetime"
    }

    override fun run(vararg args: Any): String {
        if ((args.isEmpty()) || ((args[0] as? String).isNullOrBlank())) {
            return ""
        }
        return ZonedDateTime
                .ofInstant(Instant.ofEpochMilli(args[0] as? Long ?: (args[0] as String).toLong()), UTC_ZONE)
                .format(DEFAULT_FORMATTER)
    }
}