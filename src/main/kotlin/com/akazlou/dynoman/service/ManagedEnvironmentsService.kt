package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.ManagedEnvironment
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class ManagedEnvironmentsService : SaverService() {
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

    fun restore(base: Path, name: String): ManagedEnvironment {
        return ManagedEnvironment(name, emptyList())
    }
}