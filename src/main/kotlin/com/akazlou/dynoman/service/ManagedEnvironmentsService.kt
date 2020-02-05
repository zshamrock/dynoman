package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.ManagedEnvironment
import java.io.StringWriter
import java.nio.file.Path

class ManagedEnvironmentsService : SaverService() {
    fun save(base: Path, environment: ManagedEnvironment) {
        val writer = StringWriter()
        environment.values.forEach {
            writer.appendln(it.name + "=" + it.value)
        }
        write(base, environment.name, ".env", writer)
    }
}