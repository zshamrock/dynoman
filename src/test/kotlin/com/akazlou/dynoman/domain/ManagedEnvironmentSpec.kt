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

    "starts with prefix" {
        forall(
                row("value", false),
                row("{value", false),
                row("{{value", true),
                row("{{ value", true)
        ) { value, expected ->
            ManagedEnvironment.startsWithPrefix(value) shouldBe expected
        }
    }

    "get completions" {
        val environment = ManagedEnvironment(ManagedEnvironment.GLOBALS, listOf(
                EnvironmentValue("val1", ""),
                EnvironmentValue("val2", ""),
                EnvironmentValue("vol1", ""),
                EnvironmentValue("vol2", "")
        ))
        forall(
                row("", listOf("val1", "val2", "vol1", "vol2")),
                row("v", listOf("val1", "val2", "vol1", "vol2")),
                row("va", listOf("val1", "val2")),
                row("val", listOf("val1", "val2")),
                row("val1", listOf("val1")),
                row("val12", emptyList()),
                row("b", emptyList())
        ) { value, completions ->
            environment.getCompletions(value) shouldBe completions
        }
    }
})