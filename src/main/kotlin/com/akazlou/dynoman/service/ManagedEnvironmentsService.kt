package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.EnvironmentValue
import com.akazlou.dynoman.domain.ManagedEnvironment
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class ManagedEnvironmentsService : SaverService() {
    fun getGlobals(): ManagedEnvironment {
        return ManagedEnvironment("Globals",
                System.getProperty("globals", "").split(",").map { it.split("=") }.map { EnvironmentValue(it[0], it[1]) })
    }

    fun save(base: Path, environment: ManagedEnvironment) {
        val writer = StringWriter()
        environment.values.forEach {
            writer.appendln(it.name + "=" + it.value)
        }
        Files.createDirectories(base)
        Files.write(resolve(base, environment.name, ".env"),
                listOf(writer.toString()),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)
    }
}