package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.EnvironmentValue
import com.akazlou.dynoman.domain.ManagedEnvironment
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class ManagedEnvironmentsService : SaverService() {
    fun save(base: Path, environment: ManagedEnvironment) {
        val writer = StringWriter()
        environment.values.forEach {
            writer.appendln(it.name + "=" + it.value)
        }
        write(base, environment.name, ".env", writer)
    }

    fun restore(base: Path, name: String): ManagedEnvironment {
        val path = resolve(base, name, ".env")
        val lines = Files.readAllLines(path, StandardCharsets.UTF_8)
        return ManagedEnvironment("Globals", lines.filter { it.isNotBlank() }.map { it.split("=") }.map { EnvironmentValue(it[0], it[1]) })
    }
}