package com.akazlou.dynoman.domain

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import java.util.EnumSet

class ForeignSearchNameSpec : StringSpec({
    "get full name" {
        forall(
                row("dev.TableA", "NameA", EnumSet.noneOf(ForeignSearchName.Flag::class.java), "dev.TableA@0@NameA"),
                row("dev.TableA", "NameA", EnumSet.of(ForeignSearchName.Flag.QUESTION), "dev.TableA@1@NameA")
        ) { table, name, flags, fullName ->
            ForeignSearchName(table, name, flags).getFullName() shouldBe fullName
        }
    }

    "get name with flags" {
        forall(
                row("dev.TableA", "NameA", EnumSet.noneOf(ForeignSearchName.Flag::class.java), "NameA"),
                row("dev.TableA", "NameA", EnumSet.of(ForeignSearchName.Flag.QUESTION), "NameA/?")
        ) { table, name, flags, nameWithFlags ->
            ForeignSearchName(table, name, flags).getNameWithFlags() shouldBe nameWithFlags
        }
    }

    "matches table" {
        forall(
                row("dev.TableA", "dev.TableA", true),
                row("dev.TableA", "TableA", false),
                row("TableA", "TableA", true),
                row("TableA", "dev.TableA", false)
        ) { tableA, tableB, matches ->
            ForeignSearchName(
                    tableA, "Name", EnumSet.of(ForeignSearchName.Flag.QUESTION)).matches(tableB) shouldBe matches
        }
    }

    "build instance from full name" {
        forall(
                row("dev.TableA@0@NameA", "dev.TableA", "NameA", EnumSet.noneOf(ForeignSearchName.Flag::class.java)),
                row("dev.TableA@1@NameA", "dev.TableA", "NameA", EnumSet.of(ForeignSearchName.Flag.QUESTION))
        ) { fullName, table, name, flags ->
            val fsn = ForeignSearchName.of(fullName)
            fsn.table shouldBe table
            fsn.name shouldBe name
            fsn.flags shouldBe flags
        }
    }

    "build instance from name with flags" {
        forall(
                row("dev.TableA", "NameA", "dev.TableA", "NameA", EnumSet.noneOf(ForeignSearchName.Flag::class.java)),
                row("dev.TableA", "NameA/?", "dev.TableA", "NameA", EnumSet.of(ForeignSearchName.Flag.QUESTION))
        ) { tableA, nameWithFlags, tableB, name, flags ->
            val fsn = ForeignSearchName.of(tableA, nameWithFlags)
            fsn.table shouldBe tableB
            fsn.name shouldBe name
            fsn.flags shouldBe flags
        }
    }

    "build flag from mnemonic" {
        ForeignSearchName.Flag.fromMnemonic('?') shouldBe ForeignSearchName.Flag.QUESTION
    }

    "throw exception on non existing mnemonic" {
        val exception = shouldThrow<UnsupportedOperationException> {
            ForeignSearchName.Flag.fromMnemonic('+')
        }
        exception.message shouldBe "No flag contains mnemonic '+'"
    }
})