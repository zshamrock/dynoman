package com.akazlou.dynoman.domain

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row

class ManagedEnvironmentSpec : StringSpec({
    "get name" {
        val environment = ManagedEnvironment(ManagedEnvironment.GLOBALS, listOf(
                EnvironmentValue("value2", "xyz"),
                EnvironmentValue("value3", "abc")
        ))
        forall(
                row("value1", ""),
                row("value2", "xyz"),
                row("{{value2}}", "xyz"),
                row("{{ value2 }}", "xyz"),
                row("value3", "abc"),
                row("{{value3}}", "abc"),
                row("{{ value3 }}", "abc")
        ) { key, expected ->
            environment.get(key) shouldBe expected
        }
    }

    "verify is env var" {
        forall(
                row("value", false),
                row("{{value", false),
                row("value}}", false),
                row("{value}", false),
                row("{{value}}", true),
                row("{{ value }}", true)
        ) { value, expected ->
            ManagedEnvironment.isEnvVar(value) shouldBe expected
        }
    }
})