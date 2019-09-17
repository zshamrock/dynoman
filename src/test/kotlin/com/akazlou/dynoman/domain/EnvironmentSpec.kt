package com.akazlou.dynoman.domain

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row

class EnvironmentSpec : StringSpec({
    "verify env" {
        forall(
                row("dev.TableA", "dev", "TableA", false, "dev.TableA"),
                row("prod.TableA.Description", "prod", "TableA.Description", false, "prod.TableA.Description"),
                row("TableA", "", "TableA", true, "TableA")
        ) { table, name, value, empty, prefixed ->
            val env = Environment(table)
            env.name shouldBe name
            env.value shouldBe value
            env.isEmpty() shouldBe empty
            env.isNotEmpty() shouldNotBe empty
            env.prefix(table) == prefixed
        }
    }
})