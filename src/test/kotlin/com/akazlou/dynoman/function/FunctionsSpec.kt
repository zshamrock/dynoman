package com.akazlou.dynoman.function

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class FunctionsSpec : StringSpec({
    "should get all available functions" {
        val functions = Functions.getAvailableFunctions()
        functions.size shouldBe 3
    }
})