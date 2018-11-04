package com.akazlou.dynoman.function

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

class TimestampFunction : Function<Long>() {
    companion object {
        @JvmField
        val UTC_ZONE: ZoneId = ZoneId.of("UTC")

        @JvmField
        val DATE_ONLY_REGEX = Regex("^(?<year>\\d{4})-(?<month>\\d{1,2})-(?<day>\\d{1,2})$")
    }

    override fun name(): String {
        return "timestamp"
    }

    override fun desc(): String {
        return "Parse datetime into the timestamp"
    }

    override fun run(vararg args: Any): Long {
        val zone = if (args.size == 2) {
            parseZone(args[1].toString())
        } else {
            UTC_ZONE
        }
        return apply(args[0].toString(), zone)
    }

    private fun parseZone(text: String): ZoneId {
        return ZoneId.of(text)
    }

    private fun apply(text: String, zone: ZoneId = UTC_ZONE): Long {
        if (DATE_ONLY_REGEX.matches(text)) {
            val (year, month, day) = DATE_ONLY_REGEX.find(text)!!.destructured
            println("year: $year, month: $month, day: $day")
        }
        val dtf = if (text.contains("am", true) || text.contains("pm", true)) {
            DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("yyyy-MM-dd h:mm:ssa[O]")
                    .toFormatter(Locale.ROOT)
        } else {
            DateTimeFormatter.ofPattern("yyyy-MM-dd [HH:mm:ss][O]")
        }
        val dateTime = LocalDateTime.parse(text, dtf)
        return dateTime.atZone(zone).toInstant().toEpochMilli()
    }
}