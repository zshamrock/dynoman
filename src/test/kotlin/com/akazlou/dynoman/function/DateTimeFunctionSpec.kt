package com.akazlou.dynoman.function

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row

class DateTimeFunctionSpec : StringSpec({
    "parse timestamp into datetime" {
        val function = DateTimeFunction()
        forall(
                row(1530658280000L, "2018-07-03 22:51:20"),
                row("1530658289001", "2018-07-03 22:51:29")
        ) { timestamp, datetime ->
            function.parse("datetime($timestamp)") shouldBe datetime
        }
    }

    "args auto completion hint" {
        DateTimeFunction().argsAutoCompletionHint() shouldBe "datetime(<timestamp>NUMERIC)"
    }
})