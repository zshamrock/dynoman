package com.akazlou.dynoman.domain

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row

class EnvironmentSpec : StringSpec({
    "strip env" {
        forall(
                row("dev.TableA", "dev", "TableA", false),
                row("prod.TableA.Description", "prod", "TableA.Description", false),
                row("TableA", "", "TableA", true)
        ) { table, name, envless, empty ->
            val env = Environment(table)
            env.name shouldBe name
            env.envlessTableOrIndex shouldBe envless
            env.isEmpty() shouldBe empty
            env.isNotEmpty() shouldNotBe empty
        }
    }
})