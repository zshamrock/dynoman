package com.akazlou.dynoman.service

import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.nio.file.Paths

class SessionSaverServiceSpec : StringSpec() {
    private val base = Paths.get("src", "test", "resources", "sessions")

    init {
        "save empty sessions" {
            val service = SessionSaverService()
            service.save(base, "test1_actual", listOf())
            "test1" shouldBe sameContent()
        }
    }

    private fun sameContent() = object : Matcher<String> {
        override fun test(value: String): Result {
            val actual = "${value}_actual.session"
            val expected = "${value}_expected.session"
            return Result(
                    base.resolve(actual).toFile().readText() == base.resolve(expected).toFile().readText(),
                    "Contents of the files $actual and $expected don't match",
                    "Contents of the files $actual and $expected match"
            )
        }
    }
}