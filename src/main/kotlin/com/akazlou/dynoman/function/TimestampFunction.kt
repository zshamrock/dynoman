package com.akazlou.dynoman.function

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class TimestampFunction : Function<Long>() {
    override fun name(): String {
        return "timestamp"
    }

    override fun run(vararg args: Any): Long {
        val offset = if (args.size == 2) {
            parseOffset(args[1].toString())
        } else {
            ZoneOffset.UTC
        }
        return apply(args[0].toString(), offset)
    }

    private fun parseOffset(text: String): ZoneOffset {
        val parts = text.split(':')
        val hours = parts[0].toInt()
        val mins = parts[1].toInt()
        return ZoneOffset.ofHoursMinutes(hours, mins)
    }

    private fun apply(text: String, offset: ZoneOffset = ZoneOffset.UTC): Long {
        val dtf = if (text.contains("am", true) || text.contains("pm", true)) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
        } else {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        }
        val dateTime = LocalDateTime.parse(text, dtf)
        return dateTime.atOffset(offset).toInstant().toEpochMilli()
    }
}