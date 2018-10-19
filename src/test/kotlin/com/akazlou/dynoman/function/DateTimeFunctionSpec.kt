package com.akazlou.dynoman.function

import io.kotlintest.inspectors.forAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class DateTimeFunctionSpec : StringSpec({
    "parse timestamp into datetime" {
        val function = DateTimeFunction()
        listOf(1530658280000L, "1530658289001").forAll {
            val dt = function.parse("datetime($it)")
            dt shouldBe "2018-07-03 22:51:20"
        }
    }
})