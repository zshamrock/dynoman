package com.akazlou.dynoman.function

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class TimestampFunction {
    fun apply(text: String, offset: ZoneOffset = ZoneOffset.UTC): Long {
        val dtf = if (text.contains("am", true) || text.contains("pm", true)) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
        } else {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        }
        val dateTime = LocalDateTime.parse(text, dtf)
        return dateTime.atOffset(offset).toInstant().toEpochMilli()
    }
}