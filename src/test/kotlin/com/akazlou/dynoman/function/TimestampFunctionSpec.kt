package com.akazlou.dynoman.function

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row

class TimestampFunctionSpec : StringSpec({
    "should parse text without am/pm into the proper timestamp" {
        val function = TimestampFunction()
        val timestamp = function.parse("timestamp(\"2018-07-03 22:51:20\")")
        timestamp shouldBe 1530658280000L
    }

    "should parse text with am/pm into the proper timestamp" {
        val function = TimestampFunction()
        forall(
                row("2018-07-03 11:51:20pm", 1530661880000L),
                row("2018-07-03 9:51:20pm", 1530654680000L),
                row("2018-07-03 11:51:20PM", 1530661880000L),
                row("2018-07-03 9:51:20PM", 1530654680000L),
                row("2018-07-03 11:51:20am", 1530618680000L),
                row("2018-07-03 9:51:20am", 1530611480000L),
                row("2018-07-03 11:51:20AM", 1530618680000L),
                row("2018-07-03 9:51:20AM", 1530611480000L)
        ) { dt, timestamp ->
            function.parse("timestamp(\"$dt\")") shouldBe timestamp
        }
    }

    "should parse text without am/pm into the proper timestamp with the time zone" {
        val function = TimestampFunction()
        forall(
                row("2018-07-03 22:51:20", "+05:00", 1530640280000L),
                row("2018-07-03 22:51:20", "+05", 1530640280000L),
                row("2018-07-03 22:51:20", "+5", 1530640280000L),
                row("2018-07-03 22:51:20", "UTC+05:00", 1530640280000L),
                row("2018-07-03 22:51:20", "UTC+05", 1530640280000L),
                row("2018-07-03 22:51:20", "UTC+5", 1530640280000L),
                row("2018-07-03 22:51:20", "GMT+05:00", 1530640280000L),
                row("2018-07-03 22:51:20", "GMT+05", 1530640280000L),
                row("2018-07-03 22:51:20", "GMT+5", 1530640280000L),
                row("2018-07-03 22:51:20", "-05:10", 1530676880000L),
                row("2018-07-03 22:51:20", "UTC-05:10", 1530676880000L),
                row("2018-07-03 22:51:20", "GMT-05:10", 1530676880000L)
        ) { dt, zone, timestamp ->
            function.parse("timestamp(\"$dt\", '$zone')") shouldBe timestamp
        }
    }

    "should parse text without time section into the proper timestamp with time being all zeros" {
        val function = TimestampFunction()
        val timestamp = function.parse("timestamp(\"2018-07-03\")")
        timestamp shouldBe 1530576000000L
    }

    "args auto completion hint" {
        TimestampFunction().argsAutoCompletionHint() shouldBe "timestamp(<datetime>STRING)"
    }
})
