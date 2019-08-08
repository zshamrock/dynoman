package com.akazlou.dynoman.ext

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row

class ExtSpec : StringSpec({
    "expand on string" {
        forall(
                row("", mapOf(), ""),
                row("val", mapOf(), "val"),
                row("", mapOf("val" to "a"), ""),
                row("val", mapOf("val" to "a"), "a"),
                row("val", mapOf("var" to "a", "val" to "b"), "b"),
                row("val", mapOf("foo" to "a", "baz" to "b"), "val"),
                // environment variable
                row("${'$'}val", mapOf("foo" to "a", "baz" to "b", "${'$'}val" to "c"), "c"),
                // context variable
                row("{{val}}", mapOf("foo" to "a", "baz" to "b", "{{val}}" to "c"), "c"),
                // user input
                row("#val", mapOf("foo" to "a", "baz" to "b", "#val" to "c"), "c")
        ) { value, mapping: Map<String, String>, expected ->
            value.expand(mapping) shouldBe expected
        }
    }
})